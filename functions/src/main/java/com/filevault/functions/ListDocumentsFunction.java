package com.filevault.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.filevault.functions.dto.DocumentResponse;
import com.filevault.functions.storage.BlobDocumentLister;
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
 * GET /api/documents — matches Angular {@code DocumentService#getDocuments} and Spring {@code DocumentController#listDocuments}.
 */
public class ListDocumentsFunction {

    private static final String ENV_CONNECTION = "AZURE_STORAGE_CONNECTION_STRING";
    private static final String ENV_CONTAINER = "AZURE_STORAGE_CONTAINER_NAME";
    private static final String DEFAULT_CONTAINER = "filevault-documents";

    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

        String connectionString = System.getenv(ENV_CONNECTION);
        if (connectionString == null || connectionString.isBlank()) {
            context.getLogger().warning(ENV_CONNECTION + " is not set");
            return json(request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Server configuration missing: " + ENV_CONNECTION + "\"}");
        }

        String containerName = System.getenv(ENV_CONTAINER);
        if (containerName == null || containerName.isBlank()) {
            containerName = DEFAULT_CONTAINER;
        }

        try {
            BlobDocumentLister lister = new BlobDocumentLister(connectionString, containerName);
            List<DocumentResponse> documents = lister.listDocuments();
            String body = JSON.writeValueAsString(documents);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .build();
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
