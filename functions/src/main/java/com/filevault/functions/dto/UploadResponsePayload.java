package com.filevault.functions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponsePayload {
    private String message;
    private DocumentResponse document;

    public UploadResponsePayload() {
    }

    public UploadResponsePayload(String message, DocumentResponse document) {
        this.message = message;
        this.document = document;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DocumentResponse getDocument() {
        return document;
    }

    public void setDocument(DocumentResponse document) {
        this.document = document;
    }
}
