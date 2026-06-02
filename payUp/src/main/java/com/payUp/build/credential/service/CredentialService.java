package com.payUp.build.credential.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payUp.build.credential.dto.CreateCredentialRequest;
import com.payUp.build.credential.dto.CredentialResponse;
import com.payUp.build.credential.entity.ApiCredential;
import com.payUp.build.credential.repository.ApiCredentialRepository;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final ApiCredentialRepository credentialRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialResponse generateCredentials(UUID merchantId,
            CreateCredentialRequest request) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        String prefix = "payup_" + request.getKeyType().name().toLowerCase() + "_";
        String apiKey = prefix + generateRandomString(24);
        String apiSecret = prefix + "secret_" + generateRandomString(32);
        String apiSecretHash = passwordEncoder.encode(apiSecret);

        ApiCredential credential = ApiCredential.builder()
                .merchant(merchant)
                .apiKey(apiKey)
                .apiSecretHash(apiSecretHash)
                .keyType(request.getKeyType())
                .isActive(true)
                .build();

        credentialRepository.save(credential);

        return new CredentialResponse(
                credential.getId(),
                apiKey,
                apiSecret,
                credential.getKeyType(),
                credential.isActive(),
                "Store this secret safely. It will not be shown again."
        );
    }

    public List<CredentialResponse> getCredentials(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        return credentialRepository.findByMerchant(merchant)
                .stream()
                .map(c -> new CredentialResponse(
                        c.getId(),
                        c.getApiKey(),
                        null,
                        c.getKeyType(),
                        c.isActive(),
                        null
                ))
                .toList();
    }

    public void revokeCredential(UUID credentialId, UUID merchantId) {
        ApiCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found"));

        if (!credential.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Credential not found");
        }

        credential.setActive(false);
        credentialRepository.save(credential);
    }

    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, length);
    }
}