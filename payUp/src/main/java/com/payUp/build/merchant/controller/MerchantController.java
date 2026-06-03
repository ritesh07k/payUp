package com.payUp.build.merchant.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.dto.MerchantProfileResponse;
import com.payUp.build.merchant.dto.UpdateProfileRequest;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.merchant.service.MerchantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantRepository merchantRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        merchantService.getProfile(merchantId),
                        "Profile fetched"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        merchantService.updateProfile(merchantId, request),
                        "Profile updated"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}