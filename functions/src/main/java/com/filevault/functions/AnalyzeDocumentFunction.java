package com.filevault.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filevault.functions.analysis.DocumentIntelligenceRuntime;
import com.filevault.functions.analysis.GeminiAnalysisService;
import com.filevault.functions.dto.DocumentAnalysisPayload;
import com.filevault.functions.dto.JobFitAnalysisPayload;
import com.filevault.functions.storage.FileVaultStores;
import com.filevault.functions.util.HttpJson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * POST /api/documents/{blobName}/analyze
 * Optional JSON body: { "jobDescription": "..." }
 * If jobDescription is provided, Gemini analysis is performed.
 */
public class AnalyzeDocumentFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @FunctionName("documentsAnalyze")
    public HttpResponseMessage analyze(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST, HttpMethod.OPTIONS},
                    route = "documents/{blobName}/analyze",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("blobName") String blobName,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            String name = URLDecoder.decode(blobName, StandardCharsets.UTF_8);

            // Step 1: Run Doc AI extraction (existing logic unchanged)
            DocumentAnalysisPayload result = DocumentIntelligenceRuntime.getInstance()
                    .analyzeDocument(name, FileVaultStores.blobStore());

            // Step 2: Check if job description was provided in request body
            String jobDescription = extractJobDescription(request, context);

            // Step 3: If job description provided, call Gemini
            if (jobDescription != null && !jobDescription.isBlank()) {
                context.getLogger().info("Job description provided, calling Gemini API...");
                String geminiKey = System.getenv("GEMINI_API_KEY");

                if (geminiKey != null && !geminiKey.isBlank()) {
                    try {
                        GeminiAnalysisService gemini = new GeminiAnalysisService(geminiKey);
                        JobFitAnalysisPayload jobFit = gemini.analyze(
                                result.getExtractedText(), jobDescription);
                        result.setJobFitAnalysis(jobFit);
                        context.getLogger().info("Gemini analysis completed successfully.");
                    } catch (Exception e) {
                        // Gemini failure should NOT break the whole response
                        context.getLogger().severe("Gemini analysis failed: " + e.getMessage());
                    }
                } else {
                    context.getLogger().warning("GEMINI_API_KEY not configured, skipping job fit analysis.");
                }
            }

            return HttpJson.ok(request, result);

        } catch (IllegalStateException e) {
            return HttpJson.rawJson(request, HttpStatus.SERVICE_UNAVAILABLE,
                    "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("analyze failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Document analysis failed\"}");
        }
    }

    private String extractJobDescription(HttpRequestMessage<Optional<String>> request,
                                          ExecutionContext context) {
        try {
            String body = request.getBody().orElse(null);
            if (body == null || body.isBlank()) {
                return null;
            }
            JsonNode json = MAPPER.readTree(body);
            JsonNode jdNode = json.get("jobDescription");
            if (jdNode != null && !jdNode.isNull()) {
                return jdNode.asText().trim();
            }
        } catch (Exception e) {
            context.getLogger().warning("Could not parse request body for jobDescription: " + e.getMessage());
        }
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}