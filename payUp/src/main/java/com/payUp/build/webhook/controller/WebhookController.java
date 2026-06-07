package com.payUp.build.webhook.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payUp.build.common.response.ApiResponse;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.webhook.dto.RegisterWebhookRequest;
import com.payUp.build.webhook.dto.WebhookEndpointResponse;
import com.payUp.build.webhook.service.WebhookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<WebhookEndpointResponse>> register(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RegisterWebhookRequest request) {

        UUID merchantId = getMerchantId(userDetails);
        WebhookEndpointResponse response = webhookService
                .registerEndpoint(merchantId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,
                        "Webhook registered. Store your secret safely."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WebhookEndpointResponse>>> getEndpoints(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID merchantId = getMerchantId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(
                        webhookService.getEndpoints(merchantId),
                        "Webhook endpoints fetched"));
    }

    private UUID getMerchantId(UserDetails userDetails) {
        return merchantRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"))
                .getId();
    }
}