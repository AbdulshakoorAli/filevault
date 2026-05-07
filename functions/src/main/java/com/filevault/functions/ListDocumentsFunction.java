package com.filevault.functions;

import com.filevault.functions.dto.DocumentResponse;
import com.filevault.functions.storage.FileVaultStores;
import com.filevault.functions.util.JsonUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.List;
import java.util.Optional;

/**
 * GET /api/documents — matches Angular {@code DocumentService#getDocuments}.
 */
public class ListDocumentsFunction {

    @FunctionName("documentsList")
    public HttpResponseMessage list(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.OPTIONS},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "documents")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            List<DocumentResponse> documents = FileVaultStores.blobStore().listDocuments();
            String body = JsonUtil.MAPPER.writeValueAsString(documents);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .build();
        } catch (IllegalStateException e) {
            context.getLogger().warning(e.getMessage());
            return json(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("list documents failed: " + e.getMessage());
            return json(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Failed to list documents\"}");
        }
    }

    private static HttpResponseMessage json(
            HttpRequestMessage<?> request,
            HttpStatus status,
            String body) {
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(body)
                .build();
    }
}
