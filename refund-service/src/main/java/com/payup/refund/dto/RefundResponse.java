package com.payup.refund.dto;

import com.payup.refund.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RefundResponse {
    private UUID id;
    private UUID paymentId;
    private Long amountPaise;
    private String currency;
    private RefundStatus status;
}
