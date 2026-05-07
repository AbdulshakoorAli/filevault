package com.filevault.functions;

import com.filevault.functions.analysis.DocumentIntelligenceRuntime;
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
 */
public class AnalyzeDocumentFunction {

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
            var result = DocumentIntelligenceRuntime.getInstance()
                    .analyzeDocument(name, FileVaultStores.blobStore());
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

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
