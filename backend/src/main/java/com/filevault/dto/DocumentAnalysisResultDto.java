package com.filevault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysisResultDto {
    private String blobName;
    private String fileName;
    private String extractedText;
    private Map<String, String> keyValuePairs;
    private List<ExtractedTableDto> extractedTables;
    private int pageCount;
    private LocalDateTime analyzedAt;
}
