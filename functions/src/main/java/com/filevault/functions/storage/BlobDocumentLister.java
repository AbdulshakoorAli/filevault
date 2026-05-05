package com.filevault.functions.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.filevault.functions.dto.DocumentResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lists blobs from the configured container (same behaviour as Spring {@code AzureBlobService#listBlobs}).
 */
public final class BlobDocumentLister {

    private static final String METADATA_ORIGINAL_NAME = "originalName";
    private static final String METADATA_UPLOAD_TIME = "uploadTime";

    private final BlobContainerClient containerClient;

    public BlobDocumentLister(String connectionString, String containerName) {
        this.containerClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);
    }

    public List<DocumentResponse> listDocuments() {
        List<DocumentResponse> out = new ArrayList<>();
        for (BlobItem item : containerClient.listBlobs()) {
            out.add(mapBlobItem(item.getName()));
        }
        return out;
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
}
