package com.payUp.build.payment.dto;

import java.util.UUID;

import com.payUp.build.payment.entity.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    private UUID orderId;
    private String idempotencyKey;
    private PaymentMethod paymentMethod;
}