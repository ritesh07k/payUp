package com.payup.merchant.service;

import com.payup.common.exception.ResourceNotFoundException;
import com.payup.merchant.dto.MerchantProfileResponse;
import com.payup.merchant.dto.UpdateProfileRequest;
import com.payup.merchant.entity.Merchant;
import com.payup.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantProfileResponse getProfile(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant profile not found"));
        return new MerchantProfileResponse(merchant.getId(), merchant.getBusinessName());
    }

    public MerchantProfileResponse updateProfile(UUID merchantId, UpdateProfileRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant profile not found"));
        merchant.setBusinessName(request.getBusinessName());
        merchantRepository.save(merchant);
        return new MerchantProfileResponse(merchant.getId(), merchant.getBusinessName());
    }
}
