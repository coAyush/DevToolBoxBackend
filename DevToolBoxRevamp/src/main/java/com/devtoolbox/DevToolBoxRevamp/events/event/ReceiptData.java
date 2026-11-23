package com.devtoolbox.DevToolBoxRevamp.events.event;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptData(
        String customerEmail,
        String packageName,
        int durationDays,
        BigDecimal amount,
        String orderId,
        String paymentId,
        Instant paidAt,
        Instant validUntil,
        boolean testMode) {
}
