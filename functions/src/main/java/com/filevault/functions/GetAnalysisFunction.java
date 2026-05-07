package com.filevault.functions;

import com.filevault.functions.analysis.DocumentIntelligenceRuntime;
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
 * GET /api/documents/{blobName}/analysis
 */
public class GetAnalysisFunction {

    @FunctionName("documentsGetAnalysis")
    public HttpResponseMessage getAnalysis(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.OPTIONS},
                    route = "documents/{blobName}/analysis",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("blobName") String blobName,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            String name = URLDecoder.decode(blobName, StandardCharsets.UTF_8);
            return DocumentIntelligenceRuntime.getInstance()
                    .getAnalysis(name)
                    .map(p -> {
                        try {
                            return HttpJson.ok(request, p);
                        } catch (Exception e) {
                            context.getLogger().severe(e.getMessage());
                            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"Serialization failed\"}");
                        }
                    })
                    .orElseGet(() -> HttpJson.rawJson(request, HttpStatus.NOT_FOUND,
                            "{\"message\":\"No analysis found for this document\"}"));
        } catch (Exception e) {
            context.getLogger().severe("get analysis failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Failed to load analysis\"}");
        }
    }
}
