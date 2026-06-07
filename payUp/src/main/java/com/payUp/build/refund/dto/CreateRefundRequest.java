package com.payUp.build.refund.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateRefundRequest {

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Refund amount must be greater than 0")
    private Long amount;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private String reason;
}