package com.filevault.functions.analysis;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTableCell;
import com.azure.core.credential.AzureKeyCredential;
import com.filevault.functions.dto.DocumentAnalysisPayload;
import com.filevault.functions.dto.DocumentResponse;
import com.filevault.functions.dto.ExtractedTablePayload;
import com.filevault.functions.storage.BlobDocumentStore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory analysis cache + Document Intelligence calls (cache is per Function instance).
 */
public final class DocumentIntelligenceRuntime {
    private static final String PREBUILT_DOCUMENT_MODEL = "prebuilt-document";
    private static final int SAS_EXPIRY_HOURS_FOR_ANALYSIS = 2;

    private static final Object LOCK = new Object();
    private static volatile DocumentIntelligenceRuntime INSTANCE;

    private final Optional<DocumentAnalysisClient> analysisClient;
    private final ConcurrentHashMap<String, DocumentAnalysisPayload> analysisByBlobName = new ConcurrentHashMap<>();

    private DocumentIntelligenceRuntime(Optional<DocumentAnalysisClient> analysisClient) {
        this.analysisClient = analysisClient;
    }

    public static DocumentIntelligenceRuntime getInstance() {
        DocumentIntelligenceRuntime local = INSTANCE;
        if (local != null) {
            return local;
        }
        synchronized (LOCK) {
            if (INSTANCE == null) {
                String endpoint = trimToNull(System.getenv("AZURE_DOC_INTEL_ENDPOINT"));
                String key = trimToNull(System.getenv("AZURE_DOC_INTEL_KEY"));
                Optional<DocumentAnalysisClient> client = Optional.empty();
                if (endpoint != null && key != null) {
                    client = Optional.of(new DocumentAnalysisClientBuilder()
                            .credential(new AzureKeyCredential(key))
                            .endpoint(endpoint)
                            .buildClient());
                }
                INSTANCE = new DocumentIntelligenceRuntime(client);
            }
            return INSTANCE;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public DocumentAnalysisPayload analyzeDocument(String blobName, BlobDocumentStore blobs) throws Exception {
        DocumentAnalysisClient client = analysisClient.orElseThrow(() -> new IllegalStateException(
                "Azure Document Intelligence is not configured. Set AZURE_DOC_INTEL_ENDPOINT and AZURE_DOC_INTEL_KEY."));

        DocumentResponse document = blobs.getDocument(blobName);
        BlobDocumentStore.ShareLinkResult sas = blobs.generateSasUrl(blobName, SAS_EXPIRY_HOURS_FOR_ANALYSIS);

        AnalyzeResult result = client
                .beginAnalyzeDocumentFromUrl(PREBUILT_DOCUMENT_MODEL, sas.downloadUrl())
                .getFinalResult();

        StringBuilder fullText = new StringBuilder();
        if (result.getPages() != null) {
            for (DocumentPage page : result.getPages()) {
                if (page.getLines() == null) {
                    continue;
                }
                for (DocumentLine line : page.getLines()) {
                    if (line.getContent() != null) {
                        fullText.append(line.getContent()).append('\n');
                    }
                }
            }
        }

        Map<String, String> kvPairs = new LinkedHashMap<>();
        if (result.getKeyValuePairs() != null) {
            for (DocumentKeyValuePair kv : result.getKeyValuePairs()) {
                String k = kv.getKey() != null && kv.getKey().getContent() != null ? kv.getKey().getContent() : "";
                String v = kv.getValue() != null && kv.getValue().getContent() != null ? kv.getValue().getContent() : "";
                kvPairs.put(k, v);
            }
        }

        List<ExtractedTablePayload> tables = new ArrayList<>();
        if (result.getTables() != null) {
            for (DocumentTable table : result.getTables()) {
                List<String> cells = table.getCells() == null
                        ? List.of()
                        : table.getCells().stream()
                                .map(DocumentTableCell::getContent)
                                .collect(Collectors.toList());
                ExtractedTablePayload t = new ExtractedTablePayload();
                t.setRowCount(table.getRowCount());
                t.setColumnCount(table.getColumnCount());
                t.setCells(cells);
                tables.add(t);
            }
        }

        int pageCount = result.getPages() != null ? result.getPages().size() : 0;
        LocalDateTime analyzedAt = LocalDateTime.now();

        DocumentAnalysisPayload dto = new DocumentAnalysisPayload();
        dto.setBlobName(document.getBlobName());
        dto.setFileName(document.getFileName());
        dto.setExtractedText(fullText.toString());
        dto.setKeyValuePairs(kvPairs);
        dto.setExtractedTables(tables);
        dto.setPageCount(pageCount);
        dto.setAnalyzedAt(analyzedAt);

        analysisByBlobName.put(blobName, dto);
        return dto;
    }

    public Optional<DocumentAnalysisPayload> getAnalysis(String blobName) {
        return Optional.ofNullable(analysisByBlobName.get(blobName));
    }
}
