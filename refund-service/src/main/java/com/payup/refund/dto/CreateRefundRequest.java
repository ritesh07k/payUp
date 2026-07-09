package com.payup.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateRefundRequest {

    @NotNull
    private UUID paymentId;

    @NotNull
    @Positive
    private Long amountPaise;

    private String reason;

    @NotBlank
    private String idempotencyKey;
}
