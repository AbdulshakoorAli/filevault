package com.filevault.functions;

import com.filevault.functions.dto.DocumentResponse;
import com.filevault.functions.dto.EmailResultPayload;
import com.filevault.functions.dto.ShareViaEmailPayload;
import com.filevault.functions.email.EmailHelper;
import com.filevault.functions.storage.BlobDocumentStore;
import com.filevault.functions.storage.FileVaultStores;
import com.filevault.functions.util.HttpJson;
import com.filevault.functions.util.JsonUtil;
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
import java.util.List;
import java.util.Optional;

/**
 * POST /api/documents/{blobName}/share-via-email
 */
public class ShareViaEmailFunction {

    private static final int DEFAULT_EXPIRY_HOURS = 24;

    @FunctionName("documentsShareEmail")
    public HttpResponseMessage share(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST, HttpMethod.OPTIONS},
                    route = "documents/{blobName}/share-via-email",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @BindingName("blobName") String blobName,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        String acs = System.getenv("ACS_CONNECTION_STRING");
        String sender = System.getenv("ACS_SENDER_EMAIL");
        if (acs == null || acs.isBlank() || sender == null || sender.isBlank()) {
            return HttpJson.rawJson(request, HttpStatus.SERVICE_UNAVAILABLE,
                    "{\"error\":\"Email is not configured (ACS_CONNECTION_STRING, ACS_SENDER_EMAIL)\"}");
        }

        try {
            String name = URLDecoder.decode(blobName, StandardCharsets.UTF_8);
            String bodyJson = request.getBody().orElse("{}");
            ShareViaEmailPayload payload = JsonUtil.MAPPER.readValue(bodyJson, ShareViaEmailPayload.class);
            if (payload.getRecipientEmail() == null || payload.getRecipientEmail().isBlank()) {
                return HttpJson.rawJson(request, HttpStatus.BAD_REQUEST,
                        "{\"message\":\"Recipient email is required\"}");
            }

            int expiryHours = payload.getExpiryHours() > 0 ? payload.getExpiryHours() : DEFAULT_EXPIRY_HOURS;
            BlobDocumentStore blobs = FileVaultStores.blobStore();
            BlobDocumentStore.ShareLinkResult link = blobs.generateSasUrl(name, expiryHours);

            String fileName = resolveFileName(name, blobs);
            EmailHelper email = new EmailHelper(acs, sender);
            EmailResultPayload result = email.sendShareLinkEmail(
                    payload.getRecipientEmail().trim(),
                    fileName,
                    link.downloadUrl(),
                    expiryHours);

            return HttpJson.ok(request, result);
        } catch (IllegalArgumentException e) {
            return HttpJson.rawJson(request, HttpStatus.NOT_FOUND,
                    "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("share email failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Failed to send email\"}");
        }
    }

    private static String resolveFileName(String blobName, BlobDocumentStore blobs) {
        List<DocumentResponse> docs = blobs.listDocuments();
        for (DocumentResponse d : docs) {
            if (blobName.equals(d.getBlobName())) {
                return d.getFileName();
            }
        }
        return blobs.getDocument(blobName).getFileName();
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
