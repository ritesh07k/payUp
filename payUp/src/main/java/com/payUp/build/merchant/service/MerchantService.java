package com.payUp.build.merchant.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.dto.MerchantProfileResponse;
import com.payUp.build.merchant.dto.UpdateProfileRequest;
import com.payUp.build.merchant.mapper.MerchantMapper;
import com.payUp.build.merchant.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    public MerchantProfileResponse getProfile(UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .map(merchantMapper::toProfileResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));
    }

    public MerchantProfileResponse updateProfile(UUID merchantId,
            UpdateProfileRequest request) {
        return merchantRepository.findById(merchantId)
                .map(merchant -> {
                    if (request.getBusinessName() != null
                            && !request.getBusinessName().isBlank()) {
                        merchant.setBusinessName(request.getBusinessName());
                    }
                    return merchantMapper.toProfileResponse(
                            merchantRepository.save(merchant));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));
    }
}