package com.payUp.build.payment.dto;

import java.util.UUID;

import com.payUp.build.payment.entity.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}