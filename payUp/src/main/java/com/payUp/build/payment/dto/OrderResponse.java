package com.payUp.build.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.payUp.build.payment.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private Long amount;
    private String currency;
    private String receipt;
    private OrderStatus status;
    private LocalDateTime createdAt;
}