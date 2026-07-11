package com.payup.webhook.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentCapturedEvent(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Long amountPaise,
        String currency,
        Instant occurredAt
) {
}
