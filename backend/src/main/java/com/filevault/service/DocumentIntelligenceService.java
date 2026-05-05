package com.filevault.service;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTableCell;
import com.azure.core.credential.AzureKeyCredential;
import com.filevault.dto.DocumentAnalysisResultDto;
import com.filevault.dto.DocumentDto;
import com.filevault.dto.ExtractedTableDto;
import com.filevault.dto.ShareLinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentIntelligenceService {

    private static final String PREBUILT_DOCUMENT_MODEL = "prebuilt-document";
    private static final int SAS_EXPIRY_HOURS_FOR_ANALYSIS = 2;

    private final AzureBlobService azureBlobService;
    private final Optional<DocumentAnalysisClient> analysisClient;
    private final ConcurrentHashMap<String, DocumentAnalysisResultDto> analysisByBlobName = new ConcurrentHashMap<>();

    public DocumentIntelligenceService(
            AzureBlobService azureBlobService,
            @Value("${azure.document-intelligence.endpoint:}") String endpoint,
            @Value("${azure.document-intelligence.key:}") String apiKey) {
        this.azureBlobService = azureBlobService;
        if (StringUtils.hasText(endpoint) && StringUtils.hasText(apiKey)) {
            this.analysisClient = Optional.of(new DocumentAnalysisClientBuilder()
                    .credential(new AzureKeyCredential(apiKey))
                    .endpoint(endpoint.trim())
                    .buildClient());
            log.info("Azure Document Intelligence client initialized");
        } else {
            this.analysisClient = Optional.empty();
            log.warn("Azure Document Intelligence is disabled: set azure.document-intelligence.endpoint and key");
        }
    }

    public DocumentAnalysisResultDto analyzeDocument(String blobName) throws Exception {
        DocumentAnalysisClient client = analysisClient.orElseThrow(() -> new IllegalStateException(
                "Azure Document Intelligence is not configured. Set AZURE_DOC_INTEL_ENDPOINT and AZURE_DOC_INTEL_KEY."));

        DocumentDto document = azureBlobService.getDocument(blobName);
        ShareLinkResponse sas = azureBlobService.generateSasUrl(blobName, SAS_EXPIRY_HOURS_FOR_ANALYSIS);

        log.info("Starting Document Intelligence analysis for blob {}", blobName);
        AnalyzeResult result = client
                .beginAnalyzeDocumentFromUrl(PREBUILT_DOCUMENT_MODEL, sas.getDownloadUrl())
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
                String key = kv.getKey() != null && kv.getKey().getContent() != null
                        ? kv.getKey().getContent()
                        : "";
                String value = kv.getValue() != null && kv.getValue().getContent() != null
                        ? kv.getValue().getContent()
                        : "";
                kvPairs.put(key, value);
            }
        }

        List<ExtractedTableDto> tables = new ArrayList<>();
        if (result.getTables() != null) {
            for (DocumentTable table : result.getTables()) {
                List<String> cells = table.getCells() == null
                        ? List.of()
                        : table.getCells().stream()
                                .map(DocumentTableCell::getContent)
                                .collect(Collectors.toList());
                tables.add(ExtractedTableDto.builder()
                        .rowCount(table.getRowCount())
                        .columnCount(table.getColumnCount())
                        .cells(cells)
                        .build());
            }
        }

        int pageCount = result.getPages() != null ? result.getPages().size() : 0;
        LocalDateTime analyzedAt = LocalDateTime.now();

        DocumentAnalysisResultDto dto = DocumentAnalysisResultDto.builder()
                .blobName(document.getBlobName())
                .fileName(document.getFileName())
                .extractedText(fullText.toString())
                .keyValuePairs(kvPairs)
                .extractedTables(tables)
                .pageCount(pageCount)
                .analyzedAt(analyzedAt)
                .build();

        analysisByBlobName.put(blobName, dto);
        log.info("Document Intelligence analysis completed for blob {} ({} pages)", blobName, pageCount);
        return dto;
    }

    public Optional<DocumentAnalysisResultDto> getAnalysis(String blobName) {
        return Optional.ofNullable(analysisByBlobName.get(blobName));
    }
}
