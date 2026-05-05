package com.filevault.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.filevault.dto.EmailRequest;
import com.filevault.dto.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailService {

    private final EmailClient emailClient;
    private final String senderEmail;

    public EmailService(EmailClient emailClient, @Qualifier("senderEmailAddress") String senderEmail) {
        this.emailClient = emailClient;
        this.senderEmail = senderEmail;
    }

    public EmailResponse sendEmail(EmailRequest request) {
        log.info("Sending email to: {} with subject: {}", request.getToEmail(), request.getSubject());

        EmailMessage emailMessage = buildEmailMessage(request);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);

        PollResponse<EmailSendResult> pollResponse = poller.waitForCompletion(Duration.ofMinutes(2));
        EmailSendResult result = pollResponse.getValue();

        if (pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            log.info("Email sent successfully. Message ID: {}", result.getId());
            return EmailResponse.builder()
                    .messageId(result.getId())
                    .status(result.getStatus().toString())
                    .sentAt(LocalDateTime.now())
                    .build();
        } else {
            log.error("Failed to send email. Status: {}", result.getStatus());
            throw new RuntimeException("Failed to send email: " + result.getStatus());
        }
    }

    public EmailResponse sendShareLinkEmail(String recipientEmail, String fileName, String downloadUrl, int expiryHours) {
        String subject = "FileVault: A file has been shared with you";
        String body = buildShareLinkEmailBody(fileName, downloadUrl, expiryHours);

        EmailRequest request = EmailRequest.builder()
                .toEmail(recipientEmail)
                .subject(subject)
                .body(body)
                .isHtml(true)
                .build();

        return sendEmail(request);
    }

    private EmailMessage buildEmailMessage(EmailRequest request) {
        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress(senderEmail)
                .setToRecipients(new EmailAddress(request.getToEmail()))
                .setSubject(request.getSubject());

        if (request.isHtml()) {
            emailMessage.setBodyHtml(request.getBody());
        } else {
            emailMessage.setBodyPlainText(request.getBody());
        }

        if (request.getCcEmails() != null && !request.getCcEmails().isEmpty()) {
            List<EmailAddress> ccAddresses = request.getCcEmails().stream()
                    .map(EmailAddress::new)
                    .collect(Collectors.toList());
            emailMessage.setCcRecipients(ccAddresses);
        }

        return emailMessage;
    }

    private String buildShareLinkEmailBody(String fileName, String downloadUrl, int expiryHours) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #0078d4; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;">
                        <h1 style="margin: 0; color: white;">FileVault</h1>
                    </div>
                    <div style="padding: 30px; background-color: #f9f9f9; border: 1px solid #e0e0e0;">
                        <h2 style="color: #333; margin-top: 0;">A file has been shared with you</h2>
                        <p style="color: #333;">Someone has shared the following file with you:</p>
                        <p style="color: #333;"><strong>File Name:</strong> %s</p>
                        <p style="color: #333;">Click the button below to download the file:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; padding: 14px 32px; background-color: #28a745; color: #ffffff !important; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px;">Download File</a>
                        </div>
                        <p style="color: #666; font-style: italic; font-size: 14px;">This link will expire in %d hour(s).</p>
                    </div>
                    <div style="padding: 20px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 8px 8px;">
                        <p style="margin: 5px 0;">This email was sent from FileVault Document Management System.</p>
                        <p style="margin: 5px 0;">If you didn't expect this email, please ignore it.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fileName, downloadUrl, expiryHours);
    }
}
