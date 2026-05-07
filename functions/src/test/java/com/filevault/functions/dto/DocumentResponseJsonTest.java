package com.filevault.functions.dto;

import com.filevault.functions.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentResponseJsonTest {

    @Test
    void serializesDocumentForAngularShape() throws Exception {
        DocumentResponse doc = new DocumentResponse();
        doc.setBlobName("b1");
        doc.setFileName("resume.pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setUploadedAt(LocalDateTime.of(2026, 5, 6, 12, 0, 0));

        String json = JsonUtil.MAPPER.writeValueAsString(doc);

        assertTrue(json.contains("\"blobName\":\"b1\""));
        assertTrue(json.contains("\"fileName\":\"resume.pdf\""));
        assertTrue(json.contains("\"fileSize\":1024"));
        assertTrue(json.contains("\"uploadedAt\":\"2026-05-06T12:00:00\""));
    }
}
