package com.filevault.functions;

import com.filevault.functions.dto.ShareLinkPayload;
import com.filevault.functions.storage.BlobDocumentStore;
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
 * GET /api/documents/{blobName}/download-link?expiryHours=
 */
public class DownloadLinkFunction {

    @FunctionName("documentsDownloadLink")
    public HttpResponseMessage downloadLink(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.OPTIONS},
                    route = "documents/{blobName}/download-link",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("blobName") String blobName,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            String name = URLDecoder.decode(blobName, StandardCharsets.UTF_8);
            int expiryHours = 24;
            String exp = request.getQueryParameters().get("expiryHours");
            if (exp != null && !exp.isBlank()) {
                try {
                    expiryHours = Integer.parseInt(exp);
                } catch (NumberFormatException ignored) {
                    expiryHours = 24;
                }
            }

            BlobDocumentStore.ShareLinkResult sas = FileVaultStores.blobStore().generateSasUrl(name, expiryHours);
            ShareLinkPayload payload = new ShareLinkPayload(sas.downloadUrl(), sas.expiresAt());
            return HttpJson.ok(request, payload);
        } catch (IllegalArgumentException e) {
            return HttpJson.rawJson(request, HttpStatus.NOT_FOUND,
                    "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("download link failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Failed to generate link\"}");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
