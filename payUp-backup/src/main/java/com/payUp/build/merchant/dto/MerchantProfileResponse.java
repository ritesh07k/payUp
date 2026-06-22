package com.payUp.build.merchant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.payUp.build.merchant.entity.MerchantStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MerchantProfileResponse {

    private UUID id;
    private String businessName;
    private String email;
    private MerchantStatus status;
    private LocalDateTime createdAt;
}