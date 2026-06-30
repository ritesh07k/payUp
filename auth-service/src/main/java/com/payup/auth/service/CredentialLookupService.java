package com.payup.auth.service;

import com.payup.auth.dto.CredentialCacheDto;
import com.payup.auth.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialLookupService {

    private final CredentialRepository credentialRepository;

    @Cacheable(value = "credentials", key = "#email", unless = "#result == null")
    public CredentialCacheDto findByEmail(String email) {
        return credentialRepository.findByEmail(email)
                .map(c -> new CredentialCacheDto(
                        c.getId(),
                        c.getEmail(),
                        c.getPasswordHash(),
                        c.getStatus()))
                .orElse(null);
    }

    @CacheEvict(value = "credentials", key = "#email")
    public void evictCredential(String email) {
    }
}
