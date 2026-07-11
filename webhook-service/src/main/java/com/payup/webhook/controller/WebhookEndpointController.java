package com.payup.webhook.controller;

import com.payup.common.response.ApiResponse;
import com.payup.webhook.dto.RegisterWebhookRequest;
import com.payup.webhook.dto.WebhookEndpointResponse;
import com.payup.webhook.service.WebhookEndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookEndpointController {

    private final WebhookEndpointService webhookEndpointService;

    @PostMapping
    public ApiResponse<WebhookEndpointResponse> register(
            Authentication authentication,
            @Valid @RequestBody RegisterWebhookRequest request) {
        UUID merchantId = UUID.fromString(authentication.getName());
        return ApiResponse.success(webhookEndpointService.register(merchantId, request));
    }
}
