package com.filevault.functions.storage;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.filevault.functions.dto.DocumentResponse;

/**
 * Upload index in Azure Table Storage.
 */
public final class UploadMetadataStore {

    static final String PARTITION_KEY = "uploads";

    private static final String PROP_FILE_NAME = "fileName";
    private static final String PROP_CONTENT_TYPE = "contentType";
    private static final String PROP_FILE_SIZE = "fileSize";
    private static final String PROP_UPLOADED_AT = "uploadedAt";

    private final TableClient uploadsTableClient;

    public UploadMetadataStore(String connectionString, String tableName) {
        var service = new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        service.createTableIfNotExists(tableName);
        this.uploadsTableClient = service.getTableClient(tableName);
    }

    public void saveUploadRow(DocumentResponse doc) {
        TableEntity entity = new TableEntity(PARTITION_KEY, doc.getBlobName());
        entity.addProperty(PROP_FILE_NAME, doc.getFileName());
        entity.addProperty(PROP_CONTENT_TYPE, doc.getContentType());
        entity.addProperty(PROP_FILE_SIZE, doc.getFileSize());
        entity.addProperty(PROP_UPLOADED_AT, doc.getUploadedAt().toString());
        uploadsTableClient.upsertEntity(entity);
    }

    public void deleteUploadRow(String blobName) {
        uploadsTableClient.deleteEntity(PARTITION_KEY, blobName);
    }
}
