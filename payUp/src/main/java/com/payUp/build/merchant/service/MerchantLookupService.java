package com.payUp.build.merchant.service;

import com.payUp.build.merchant.dto.MerchantCacheDto;
import com.payUp.build.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantLookupService {

    private final MerchantRepository merchantRepository;

    @Cacheable(value = "merchants", key = "#email", unless = "#result == null")
    public MerchantCacheDto findByEmail(String email) {
        return merchantRepository.findByEmail(email)
                .map(m -> new MerchantCacheDto(
                        m.getId(),
                        m.getBusinessName(),
                        m.getEmail(),
                        m.getPasswordHash(),
                        m.getStatus()))
                .orElse(null);
    }

    @CacheEvict(value = "merchants", key = "#email")
    public void evictMerchant(String email) {
    }
}