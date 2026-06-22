package com.payUp.build.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.payUp.build.payment.entity.PaymentMethod;
import com.payUp.build.payment.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String bankReferenceId;
    private String failureReason;
    private LocalDateTime createdAt;
}