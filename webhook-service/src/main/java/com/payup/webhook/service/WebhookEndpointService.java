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
    private final WebhookEncryptionService webhookEncryptionService;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookEndpointResponse register(UUID merchantId, RegisterWebhookRequest request) {
        String plainSecret = generateSecret();

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setMerchantId(merchantId);
        endpoint.setUrl(request.getUrl());
        endpoint.setSecret(webhookEncryptionService.encrypt(plainSecret));
        endpoint.setActive(true);

        WebhookEndpoint saved = webhookEndpointRepository.save(endpoint);

        // Plaintext secret shown once at registration time, never stored or logged again
        return new WebhookEndpointResponse(saved.getId(), saved.getUrl(), saved.isActive(), plainSecret);
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
