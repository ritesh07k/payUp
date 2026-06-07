package com.payUp.build.refund.dto;

import com.payUp.build.refund.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RefundResponse {

    private UUID id;
    private UUID paymentId;
    private Long amount;
    private String currency;
    private RefundStatus status;
    private String reason;
    private String bankReferenceId;
    private String failureReason;
    private LocalDateTime createdAt;
}