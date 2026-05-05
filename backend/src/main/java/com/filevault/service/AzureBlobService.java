package com.filevault.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.filevault.dto.DocumentDto;
import com.filevault.dto.ShareLinkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureBlobService {

    private final BlobContainerClient blobContainerClient;
    private final UploadMetadataTableService uploadMetadataTableService;

    private static final String METADATA_ORIGINAL_NAME = "originalName";
    private static final String METADATA_UPLOAD_TIME = "uploadTime";

    public DocumentDto uploadBlob(MultipartFile file) throws IOException {
        String blobName = generateBlobName(file.getOriginalFilename());
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(METADATA_ORIGINAL_NAME, file.getOriginalFilename());
        metadata.put(METADATA_UPLOAD_TIME, LocalDateTime.now().toString());

        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setMetadata(metadata);

        log.info("Uploaded blob: {} (original: {})", blobName, file.getOriginalFilename());

        DocumentDto document = DocumentDto.builder()
                .blobName(blobName)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        try {
            uploadMetadataTableService.saveUploadRow(document);
        } catch (Exception e) {
            log.warn("Blob uploaded but Azure Table metadata row failed (blob is still stored): {}", e.getMessage());
        }

        return document;
    }

    public List<DocumentDto> listBlobs() {
        return blobContainerClient.listBlobs()
                .stream()
                .map(this::mapBlobItemToDto)
                .collect(Collectors.toList());
    }

    public ShareLinkResponse generateSasUrl(String blobName, int expiryHours) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }

        OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(expiryHours);

        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);

        String sasToken = blobClient.generateSas(sasValues);
        String downloadUrl = blobClient.getBlobUrl() + "?" + sasToken;

        log.info("Generated SAS URL for blob: {} (expires: {})", blobName, expiryTime);

        return ShareLinkResponse.builder()
                .downloadUrl(downloadUrl)
                .expiresAt(expiryTime)
                .build();
    }

    public void deleteBlob(String blobName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }

        blobClient.delete();
        log.info("Deleted blob: {}", blobName);

        try {
            uploadMetadataTableService.deleteUploadRow(blobName);
        } catch (Exception e) {
            log.warn("Blob deleted but Azure Table metadata row cleanup failed: {}", e.getMessage());
        }
    }

    public boolean blobExists(String blobName) {
        return blobContainerClient.getBlobClient(blobName).exists();
    }

    /**
     * Loads metadata for a single blob (throws if the blob does not exist).
     */
    public DocumentDto getDocument(String blobName) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }
        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();
        String originalName = metadata != null ? metadata.get(METADATA_ORIGINAL_NAME) : blobName;
        LocalDateTime uploadTime = parseUploadTime(metadata);
        return DocumentDto.builder()
                .blobName(blobName)
                .fileName(originalName != null ? originalName : blobName)
                .contentType(properties.getContentType())
                .fileSize(properties.getBlobSize())
                .uploadedAt(uploadTime != null ? uploadTime
                        : properties.getCreationTime() != null
                                ? properties.getCreationTime().toLocalDateTime()
                                : LocalDateTime.now())
                .build();
    }

    private String generateBlobName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private DocumentDto mapBlobItemToDto(BlobItem blobItem) {
        BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();

        String originalName = metadata != null ? metadata.get(METADATA_ORIGINAL_NAME) : blobItem.getName();
        LocalDateTime uploadTime = parseUploadTime(metadata);

        return DocumentDto.builder()
                .blobName(blobItem.getName())
                .fileName(originalName != null ? originalName : blobItem.getName())
                .contentType(properties.getContentType())
                .fileSize(properties.getBlobSize())
                .uploadedAt(uploadTime != null ? uploadTime :
                        properties.getCreationTime().toLocalDateTime())
                .build();
    }

    private LocalDateTime parseUploadTime(Map<String, String> metadata) {
        if (metadata == null || !metadata.containsKey(METADATA_UPLOAD_TIME)) {
            return null;
        }
        try {
            return LocalDateTime.parse(metadata.get(METADATA_UPLOAD_TIME));
        } catch (Exception e) {
            return null;
        }
    }
}
