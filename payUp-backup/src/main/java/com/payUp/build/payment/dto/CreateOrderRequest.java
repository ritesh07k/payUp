package com.payUp.build.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Long amount;

    @NotNull(message = "Currency is required")
    @Pattern(regexp = "INR|USD|EUR|GBP", message = "Unsupported currency")
    private String currency;

    @Size(max = 255, message = "Receipt cannot exceed 255 characters")
    private String receipt;
}