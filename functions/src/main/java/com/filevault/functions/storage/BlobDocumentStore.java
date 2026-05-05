package com.filevault.functions.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.filevault.functions.dto.DocumentResponse;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Blob + metadata operations for the FileVault documents API.
 */
public final class BlobDocumentStore {

    private static final String METADATA_ORIGINAL_NAME = "originalName";
    private static final String METADATA_UPLOAD_TIME = "uploadTime";

    private final BlobContainerClient containerClient;
    private final UploadMetadataStore metadataStore;

    public BlobDocumentStore(String connectionString, String containerName, UploadMetadataStore metadataStore) {
        this.containerClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);
        this.metadataStore = metadataStore;
    }

    public List<DocumentResponse> listDocuments() {
        List<DocumentResponse> out = new ArrayList<>();
        for (BlobItem item : containerClient.listBlobs()) {
            out.add(mapBlobItem(item.getName()));
        }
        return out;
    }

    public DocumentResponse uploadDocument(String originalFilename, String contentType, byte[] fileBytes) {
        String blobName = generateBlobName(originalFilename);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(METADATA_ORIGINAL_NAME, originalFilename);
        metadata.put(METADATA_UPLOAD_TIME, LocalDateTime.now().toString());

        blobClient.upload(new ByteArrayInputStream(fileBytes), fileBytes.length, true);
        blobClient.setMetadata(metadata);

        DocumentResponse document = new DocumentResponse();
        document.setBlobName(blobName);
        document.setFileName(originalFilename);
        document.setContentType(contentType);
        document.setFileSize((long) fileBytes.length);
        document.setUploadedAt(LocalDateTime.now());

        try {
            metadataStore.saveUploadRow(document);
        } catch (Exception e) {
            // blob is stored; table row is optional
        }
        return document;
    }

    public ShareLinkResult generateSasUrl(String blobName, int expiryHours) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }
        OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(expiryHours);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);
        String sasToken = blobClient.generateSas(sasValues);
        String downloadUrl = blobClient.getBlobUrl() + "?" + sasToken;
        return new ShareLinkResult(downloadUrl, expiryTime);
    }

    public void deleteBlob(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }
        blobClient.delete();
        try {
            metadataStore.deleteUploadRow(blobName);
        } catch (Exception ignored) {
        }
    }

    public DocumentResponse getDocument(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }
        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();
        String originalName = metadata != null ? metadata.get(METADATA_ORIGINAL_NAME) : blobName;
        LocalDateTime uploadTime = parseUploadTime(metadata);
        LocalDateTime uploadedAt = uploadTime != null
                ? uploadTime
                : properties.getCreationTime() != null
                        ? properties.getCreationTime().toLocalDateTime()
                        : LocalDateTime.now();

        DocumentResponse dto = new DocumentResponse();
        dto.setBlobName(blobName);
        dto.setFileName(originalName != null ? originalName : blobName);
        dto.setContentType(properties.getContentType());
        dto.setFileSize(properties.getBlobSize());
        dto.setUploadedAt(uploadedAt);
        return dto;
    }

    private DocumentResponse mapBlobItem(String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();

        String originalName = metadata != null ? metadata.get(METADATA_ORIGINAL_NAME) : blobName;
        LocalDateTime uploadTime = parseUploadTime(metadata);
        LocalDateTime uploadedAt = uploadTime != null
                ? uploadTime
                : properties.getCreationTime() != null
                        ? properties.getCreationTime().toLocalDateTime()
                        : LocalDateTime.now();

        DocumentResponse dto = new DocumentResponse();
        dto.setBlobName(blobName);
        dto.setFileName(originalName != null ? originalName : blobName);
        dto.setContentType(properties.getContentType());
        dto.setFileSize(properties.getBlobSize());
        dto.setUploadedAt(uploadedAt);
        return dto;
    }

    private static String generateBlobName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extension;
    }

    private static LocalDateTime parseUploadTime(Map<String, String> metadata) {
        if (metadata == null || !metadata.containsKey(METADATA_UPLOAD_TIME)) {
            return null;
        }
        try {
            return LocalDateTime.parse(metadata.get(METADATA_UPLOAD_TIME));
        } catch (Exception e) {
            return null;
        }
    }

    public record ShareLinkResult(String downloadUrl, OffsetDateTime expiresAt) {
    }
}
