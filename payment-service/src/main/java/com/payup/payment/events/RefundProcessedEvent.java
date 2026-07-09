package com.payup.payment.events;

import java.time.Instant;
import java.util.UUID;

public record RefundProcessedEvent(
        UUID refundId,
        UUID paymentId,
        UUID merchantId,
        Long amountPaise,
        String currency,
        Instant occurredAt
) {
}
