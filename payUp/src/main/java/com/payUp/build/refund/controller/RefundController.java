package com.payUp.build.refund.controller;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.refund.dto.CreateRefundRequest;
import com.payUp.build.refund.dto.RefundResponse;
import com.payUp.build.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreateRefundRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        refundService.initiateRefund(merchantId, request),
                        "Refund initiated"));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsByPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID paymentId) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        refundService.getRefundsByPayment(paymentId, merchantId),
                        "Refunds fetched"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}