package com.filevault.functions;

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
 * DELETE /api/documents/{blobName}
 */
public class DeleteDocumentFunction {

    @FunctionName("documentsDelete")
    public HttpResponseMessage delete(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.DELETE, HttpMethod.OPTIONS},
                    route = "documents/{blobName}",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("blobName") String blobName,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            String name = URLDecoder.decode(blobName, StandardCharsets.UTF_8);
            FileVaultStores.blobStore().deleteBlob(name);
            return HttpJson.noContent(request);
        } catch (IllegalArgumentException e) {
            return HttpJson.rawJson(request, HttpStatus.NOT_FOUND,
                    "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("delete failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Delete failed\"}");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
