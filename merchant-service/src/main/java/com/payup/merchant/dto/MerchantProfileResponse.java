package com.payup.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MerchantProfileResponse {
    private UUID id;
    private String businessName;
}
