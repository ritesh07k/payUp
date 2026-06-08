package com.payUp.build.events;

import com.payUp.build.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private UUID paymentId;
    private UUID orderId;
    private UUID merchantId;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private String eventType;
    private LocalDateTime occurredAt;
}