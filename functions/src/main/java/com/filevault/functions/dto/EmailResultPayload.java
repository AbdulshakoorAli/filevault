package com.filevault.functions.dto;

import java.time.LocalDateTime;

public class EmailResultPayload {
    private String messageId;
    private String status;
    private LocalDateTime sentAt;

    public EmailResultPayload() {
    }

    public EmailResultPayload(String messageId, String status, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.status = status;
        this.sentAt = sentAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
