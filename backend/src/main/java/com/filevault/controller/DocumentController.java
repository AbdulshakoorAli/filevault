package com.filevault.controller;

import com.filevault.dto.DocumentAnalysisResultDto;
import com.filevault.dto.DocumentDto;
import com.filevault.dto.EmailResponse;
import com.filevault.dto.ShareLinkResponse;
import com.filevault.dto.ShareViaEmailRequest;
import com.filevault.dto.UploadResponse;
import com.filevault.service.AzureBlobService;
import com.filevault.service.DocumentIntelligenceService;
import com.filevault.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final AzureBlobService azureBlobService;
    private final EmailService emailService;
    private final DocumentIntelligenceService documentIntelligenceService;

    private static final int DEFAULT_EXPIRY_HOURS = 24;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("File is empty")
                            .build());
        }

        log.info("Uploading file: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());
        DocumentDto document = azureBlobService.uploadBlob(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UploadResponse.builder()
                        .message("File uploaded successfully")
                        .document(document)
                        .build());
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> listDocuments() {
        log.info("Listing all documents");
        List<DocumentDto> documents = azureBlobService.listBlobs();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{blobName}/analysis")
    public ResponseEntity<DocumentAnalysisResultDto> getDocumentAnalysis(@PathVariable String blobName) {
        return documentIntelligenceService.getAnalysis(blobName)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No analysis found for this document"));
    }

    @PostMapping("/{blobName}/analyze")
    public ResponseEntity<DocumentAnalysisResultDto> analyzeDocument(@PathVariable String blobName) {
        try {
            DocumentAnalysisResultDto result = documentIntelligenceService.analyzeDocument(blobName);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Document analysis failed for {}", blobName, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Document analysis failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{blobName}/download-link")
    public ResponseEntity<ShareLinkResponse> getDownloadLink(
            @PathVariable String blobName,
            @RequestParam(defaultValue = "24") int expiryHours) {

        log.info("Generating download link for blob: {} (expiry: {} hours)", blobName, expiryHours);
        ShareLinkResponse response = azureBlobService.generateSasUrl(blobName, expiryHours);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{blobName}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String blobName) {
        log.info("Deleting document: {}", blobName);
        azureBlobService.deleteBlob(blobName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blobName}/share-via-email")
    public ResponseEntity<EmailResponse> shareViaEmail(
            @PathVariable String blobName,
            @Valid @RequestBody ShareViaEmailRequest request) {

        log.info("Sharing document {} via email to: {}", blobName, request.getRecipientEmail());

        int expiryHours = request.getExpiryHours() > 0 ? request.getExpiryHours() : DEFAULT_EXPIRY_HOURS;
        ShareLinkResponse shareLink = azureBlobService.generateSasUrl(blobName, expiryHours);

        List<DocumentDto> documents = azureBlobService.listBlobs();
        String fileName = documents.stream()
                .filter(doc -> doc.getBlobName().equals(blobName))
                .findFirst()
                .map(DocumentDto::getFileName)
                .orElseGet(() -> azureBlobService.getDocument(blobName).getFileName());

        EmailResponse emailResponse = emailService.sendShareLinkEmail(
                request.getRecipientEmail(),
                fileName,
                shareLink.getDownloadUrl(),
                expiryHours
        );

        return ResponseEntity.ok(emailResponse);
    }
}
