package com.payUp.build.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    private Long amount;
    private String currency;
    private String receipt;
}