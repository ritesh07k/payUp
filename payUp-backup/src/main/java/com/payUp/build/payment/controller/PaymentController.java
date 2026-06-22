package com.payUp.build.payment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.payment.dto.CreatePaymentRequest;
import com.payUp.build.payment.dto.PaymentResponse;
import com.payUp.build.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreatePaymentRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        paymentService.initiatePayment(merchantId, request),
                        "Payment initiated"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID paymentId) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        paymentService.getPayment(paymentId, merchantId),
                        "Payment fetched"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderId) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        paymentService.getPaymentsByOrder(orderId, merchantId),
                        "Payments fetched"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}