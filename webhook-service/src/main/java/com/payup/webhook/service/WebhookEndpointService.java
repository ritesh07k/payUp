package com.payup.webhook.service;

import com.payup.webhook.dto.RegisterWebhookRequest;
import com.payup.webhook.dto.WebhookEndpointResponse;
import com.payup.webhook.entity.WebhookEndpoint;
import com.payup.webhook.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookEndpointService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookEndpointResponse register(UUID merchantId, RegisterWebhookRequest request) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setMerchantId(merchantId);
        endpoint.setUrl(request.getUrl());
        endpoint.setSecret(generateSecret());
        endpoint.setActive(true);

        WebhookEndpoint saved = webhookEndpointRepository.save(endpoint);

        // Secret shown once at registration time, same pattern as monolith Phase 10
        return new WebhookEndpointResponse(saved.getId(), saved.getUrl(), saved.isActive(), saved.getSecret());
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
