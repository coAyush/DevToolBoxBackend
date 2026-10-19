package com.devtoolbox.DevToolBoxRevamp.common;

import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class EmailService{

    private final Resource otpTemplate;
    private final JavaMailSender mailSender;

    public EmailService(
            @Value("classpath:email_text/OtpMessage.st") Resource otpTemplate,
            JavaMailSender mailSender) {
        this.otpTemplate = otpTemplate;
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String subject, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String template=new String(otpTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String content=template.replace("<otp>",otp);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        }catch(IOException e){
            log.error("Internal Server Error");
            throw new RuntimeException("failed to send message ");
        }
        catch (MailException e){
            log.error("failed to send message to mail {} error {}", to, e.getMessage());
            throw new RuntimeException("failed to send message ");
        }
    }

    public void sendMessage(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        }
        catch (MailException e){
            log.error("failed to send message to mail {} error {}", to, e.getMessage());
            throw new RuntimeException("failed to send message ");
        }
    }
}
