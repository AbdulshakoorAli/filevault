package com.filevault.functions;

import com.filevault.functions.dto.DocumentResponse;
import com.filevault.functions.dto.UploadResponsePayload;
import com.filevault.functions.storage.FileVaultStores;
import com.filevault.functions.upload.MultipartFileParser;
import com.filevault.functions.util.HttpJson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

/**
 * POST /api/documents/upload — multipart field {@code file}.
 */
public class UploadDocumentFunction {

    @FunctionName("documentsUpload")
    public HttpResponseMessage upload(
            @HttpTrigger(
                    name = "req",
                    dataType = "binary",
                    methods = {HttpMethod.POST, HttpMethod.OPTIONS},
                    route = "documents/upload",
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<byte[]>> request,
            final ExecutionContext context) {

        if (request.getHttpMethod() == HttpMethod.OPTIONS) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        }

        try {
            byte[] raw = request.getBody().orElse(null);
            if (raw == null || raw.length == 0) {
                return HttpJson.rawJson(request, HttpStatus.BAD_REQUEST,
                        "{\"message\":\"File is empty\"}");
            }
            String contentType = firstHeader(request, "Content-Type");
            if (contentType == null) {
                return HttpJson.rawJson(request, HttpStatus.BAD_REQUEST,
                        "{\"message\":\"Missing Content-Type\"}");
            }

            MultipartFileParser.ParsedFile parsed = MultipartFileParser.parse(raw, contentType);
            if (parsed.data().length == 0) {
                UploadResponsePayload empty = new UploadResponsePayload("File is empty", null);
                return HttpJson.json(request, HttpStatus.BAD_REQUEST, empty);
            }

            DocumentResponse doc = FileVaultStores.blobStore().uploadDocument(
                    parsed.filename(),
                    parsed.contentType(),
                    parsed.data());

            UploadResponsePayload ok = new UploadResponsePayload("File uploaded successfully", doc);
            return HttpJson.created(request, ok);
        } catch (IllegalArgumentException e) {
            return HttpJson.rawJson(request, HttpStatus.BAD_REQUEST,
                    "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            context.getLogger().severe("upload failed: " + e.getMessage());
            return HttpJson.rawJson(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Upload failed\"}");
        }
    }

    private static String firstHeader(HttpRequestMessage<?> request, String name) {
        var headers = request.getHeaders();
        if (headers == null) {
            return null;
        }
        for (var e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
