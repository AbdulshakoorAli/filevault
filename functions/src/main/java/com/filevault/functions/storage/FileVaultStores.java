package com.filevault.functions.storage;

/**
 * Lazily builds blob + table clients (one pair per JVM / cold start).
 */
public final class FileVaultStores {

    private static final String ENV_CONNECTION = "AZURE_STORAGE_CONNECTION_STRING";
    private static final String ENV_CONTAINER = "AZURE_STORAGE_CONTAINER_NAME";
    private static final String ENV_TABLE = "AZURE_UPLOADS_TABLE_NAME";
    private static final String DEFAULT_CONTAINER = "filevault-documents";
    private static final String DEFAULT_TABLE = "filevaultuploads";

    private static final Object LOCK = new Object();
    private static volatile BlobDocumentStore blobStore;

    private FileVaultStores() {
    }

    public static BlobDocumentStore blobStore() {
        BlobDocumentStore local = blobStore;
        if (local != null) {
            return local;
        }
        synchronized (LOCK) {
            if (blobStore == null) {
                String cs = System.getenv(ENV_CONNECTION);
                if (cs == null || cs.isBlank()) {
                    throw new IllegalStateException(ENV_CONNECTION + " is not set");
                }
                String container = System.getenv(ENV_CONTAINER);
                if (container == null || container.isBlank()) {
                    container = DEFAULT_CONTAINER;
                }
                String table = System.getenv(ENV_TABLE);
                if (table == null || table.isBlank()) {
                    table = DEFAULT_TABLE;
                }
                UploadMetadataStore meta = new UploadMetadataStore(cs, table);
                blobStore = new BlobDocumentStore(cs, container, meta);
            }
            return blobStore;
        }
    }
}
