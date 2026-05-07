package com.filevault.functions.dto;

import java.time.OffsetDateTime;

public class ShareLinkPayload {
    private String downloadUrl;
    private OffsetDateTime expiresAt;

    public ShareLinkPayload() {
    }

    public ShareLinkPayload(String downloadUrl, OffsetDateTime expiresAt) {
        this.downloadUrl = downloadUrl;
        this.expiresAt = expiresAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
