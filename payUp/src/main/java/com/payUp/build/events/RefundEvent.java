package com.payUp.build.events;

import com.payUp.build.refund.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundEvent {

    private UUID refundId;
    private UUID paymentId;
    private UUID merchantId;
    private Long amount;
    private String currency;
    private RefundStatus status;
    private String eventType;
    private LocalDateTime occurredAt;
}