package com.devtoolbox.DevToolBoxRevamp.events;

import com.devtoolbox.DevToolBoxRevamp.common.EmailService;
import com.devtoolbox.DevToolBoxRevamp.common.ReceiptPdfGenerator;
import com.devtoolbox.DevToolBoxRevamp.events.event.ReceiptData;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class PdfServiceEventListener {
    private final EmailService emailService;
    private final ReceiptPdfGenerator pdfGenerator;
    @Value("classpath:email_text/ReceiptMessage.st")
    Resource receiptTemplate;

    @EventListener
    @Async
    public void sendReceiptEmail(ReceiptData  receiptData) throws IOException {
        byte[] receipt=pdfGenerator.generate(receiptData);
        String body=new String(receiptTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        emailService.sendMessage(receiptData.customerEmail(),"Plan Purchased Receipt",body,receipt,"Plan Purchase receipt.pdf");
    }
}
