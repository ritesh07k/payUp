package com.payUp.build.merchant.mapper;

import org.springframework.stereotype.Component;

import com.payUp.build.merchant.dto.MerchantProfileResponse;
import com.payUp.build.merchant.entity.Merchant;

@Component
public class MerchantMapper {

    public MerchantProfileResponse toProfileResponse(Merchant merchant) {
        return new MerchantProfileResponse(
                merchant.getId(),
                merchant.getBusinessName(),
                merchant.getEmail(),
                merchant.getStatus(),
                merchant.getCreatedAt()
        );
    }
}