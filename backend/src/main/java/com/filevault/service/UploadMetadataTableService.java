package com.filevault.service;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.filevault.dto.DocumentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Persists one row per uploaded blob: file name, content type, size, upload time.
 * PartitionKey is fixed; RowKey is the blob name (unique).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadMetadataTableService {

    static final String PARTITION_KEY = "uploads";

    private static final String PROP_FILE_NAME = "fileName";
    private static final String PROP_CONTENT_TYPE = "contentType";
    private static final String PROP_FILE_SIZE = "fileSize";
    private static final String PROP_UPLOADED_AT = "uploadedAt";

    private final TableClient uploadsTableClient;

    public void saveUploadRow(DocumentDto doc) {
        TableEntity entity = new TableEntity(PARTITION_KEY, doc.getBlobName());
        entity.addProperty(PROP_FILE_NAME, doc.getFileName());
        entity.addProperty(PROP_CONTENT_TYPE, doc.getContentType());
        entity.addProperty(PROP_FILE_SIZE, doc.getFileSize());
        entity.addProperty(PROP_UPLOADED_AT, doc.getUploadedAt().toString());
        uploadsTableClient.upsertEntity(entity);
        log.debug("Saved upload metadata row for blob {}", doc.getBlobName());
    }

    public void deleteUploadRow(String blobName) {
        uploadsTableClient.deleteEntity(PARTITION_KEY, blobName);
        log.debug("Deleted upload metadata row for blob {}", blobName);
    }
}
