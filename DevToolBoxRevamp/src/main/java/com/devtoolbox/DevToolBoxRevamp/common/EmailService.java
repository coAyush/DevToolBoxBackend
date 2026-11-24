package com.devtoolbox.DevToolBoxRevamp.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EmailService{

    private final Resource otpTemplate;
    private final JavaMailSender mailSender;
    @Value("${brevo.api-key}")
    private String brevoKey;
    @Value("${brevo.sender-email}")
    private String senderEmail;
    private final RestClient restClient = RestClient.create();

    public EmailService(
            @Value("classpath:email_text/OtpMessage.st") Resource otpTemplate,
            JavaMailSender mailSender) {
        this.otpTemplate = otpTemplate;
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String subject, String content) {
        sendMessage(to, subject, content, null, null);
    }

    public void sendMessage(String to, String subject, String content,
                            byte[] attachment, String attachmentName) {
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "DevToolBox", "email", senderEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", content);
        if (attachment != null) {
            body.put("attachment", List.of(Map.of(
                    "name", attachmentName,        // jaise "DevToolBox-receipt-order_xxx.pdf"
                    "content", Base64.getEncoder().encodeToString(attachment))));
        }
        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", brevoKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Brevo failed for {}: {} {}", to, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("failed to send message");
        }
    }
}
