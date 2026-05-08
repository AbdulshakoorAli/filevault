package com.filevault.functions.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.filevault.functions.dto.JobFitAnalysisPayload;

public class DocumentAnalysisPayload {
    private String blobName;
    private String fileName;
    private String extractedText;
    private Map<String, String> keyValuePairs;
    private List<ExtractedTablePayload> extractedTables;
    private int pageCount;
    private LocalDateTime analyzedAt;
    private JobFitAnalysisPayload jobFitAnalysis;

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Map<String, String> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(Map<String, String> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    public List<ExtractedTablePayload> getExtractedTables() {
        return extractedTables;
    }

    public void setExtractedTables(List<ExtractedTablePayload> extractedTables) {
        this.extractedTables = extractedTables;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public JobFitAnalysisPayload getJobFitAnalysis() {
        return jobFitAnalysis;
    }

    public void setJobFitAnalysis(JobFitAnalysisPayload jobFitAnalysis) {
        this.jobFitAnalysis = jobFitAnalysis;
    }

}
