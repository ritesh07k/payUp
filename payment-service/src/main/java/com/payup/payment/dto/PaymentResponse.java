package com.payup.payment.dto;

import com.payup.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private Long amountPaise;
    private String currency;
    private String method;
    private PaymentStatus status;
}
