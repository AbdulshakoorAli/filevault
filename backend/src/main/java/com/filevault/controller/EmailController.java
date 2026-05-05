package com.filevault.controller;

import com.filevault.dto.EmailRequest;
import com.filevault.dto.EmailResponse;
import com.filevault.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Received email request to: {}", request.getToEmail());
        EmailResponse response = emailService.sendEmail(request);
        return ResponseEntity.ok(response);
    }
}
