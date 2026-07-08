package com.payup.merchant.controller;

import com.payup.common.response.ApiResponse;
import com.payup.merchant.dto.MerchantProfileResponse;
import com.payup.merchant.dto.UpdateProfileRequest;
import com.payup.merchant.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> getMyProfile(Authentication authentication) {
        UUID merchantId = UUID.fromString((String) authentication.getPrincipal());
        MerchantProfileResponse response = merchantService.getProfile(merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> updateMyProfile(
            Authentication authentication,
            @RequestBody @Valid UpdateProfileRequest request) {
        UUID merchantId = UUID.fromString((String) authentication.getPrincipal());
        MerchantProfileResponse response = merchantService.updateProfile(merchantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated"));
    }
}
