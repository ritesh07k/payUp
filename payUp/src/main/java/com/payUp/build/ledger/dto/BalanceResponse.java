package com.payUp.build.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceResponse {

    private Long availableBalance;
    private String currency;
}