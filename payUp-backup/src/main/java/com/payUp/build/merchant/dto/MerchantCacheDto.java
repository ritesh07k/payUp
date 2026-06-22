package com.payUp.build.merchant.dto;

import com.payUp.build.merchant.entity.MerchantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCacheDto {
    private UUID id;
    private String businessName;
    private String email;
    private String passwordHash;
    private MerchantStatus status;
}