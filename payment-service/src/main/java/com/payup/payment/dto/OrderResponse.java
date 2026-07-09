package com.payup.payment.dto;

import com.payup.payment.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private Long amountPaise;
    private String currency;
    private String receipt;
    private OrderStatus status;
}
