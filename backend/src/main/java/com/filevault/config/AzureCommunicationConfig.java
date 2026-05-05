package com.filevault.config;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureCommunicationConfig {

    @Value("${azure.communication.connection-string}")
    private String connectionString;

    @Value("${azure.communication.sender-email}")
    private String senderEmail;

    @Bean
    public EmailClient emailClient() {
        return new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public String senderEmailAddress() {
        return senderEmail;
    }
}
