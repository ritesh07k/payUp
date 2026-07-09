package com.payup.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CapturePaymentRequest {

    @NotNull
    private UUID orderId;

    @NotBlank
    private String method;

    @NotBlank
    private String idempotencyKey;
}
