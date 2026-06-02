package com.payUp.build.credential.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.credential.dto.CreateCredentialRequest;
import com.payUp.build.credential.dto.CredentialResponse;
import com.payUp.build.credential.service.CredentialService;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final CredentialService credentialService;
    private final MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CredentialResponse>> generateCredentials(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateCredentialRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        CredentialResponse response = credentialService.generateCredentials(merchantId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "API credentials generated"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getCredentials(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        List<CredentialResponse> credentials = credentialService.getCredentials(merchantId);
        return ResponseEntity.ok(ApiResponse.success(credentials, "Credentials fetched"));
    }

    @DeleteMapping("/{credentialId}")
    public ResponseEntity<ApiResponse<Void>> revokeCredential(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID credentialId) {

        UUID merchantId = getMerchantId(userDetails);
        credentialService.revokeCredential(credentialId, merchantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Credential revoked"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}