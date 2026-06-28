package com.payUp.build.webhook.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.webhook.dto.RegisterWebhookRequest;
import com.payUp.build.webhook.dto.WebhookEndpointResponse;
import com.payUp.build.webhook.entity.WebhookDelivery;
import com.payUp.build.webhook.entity.WebhookDeliveryStatus;
import com.payUp.build.webhook.entity.WebhookEndpoint;
import com.payUp.build.webhook.repository.WebhookDeliveryRepository;
import com.payUp.build.webhook.repository.WebhookEndpointRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final MerchantRepository merchantRepository;
    private final ObjectMapper objectMapper;
    private final WebhookEncryptionService encryptionService;

    private static final int MAX_ATTEMPTS = 5;
    private static final long[] RETRY_DELAYS_MINUTES = {1, 5, 30, 120, 480};

    public WebhookEndpointResponse registerEndpoint(UUID merchantId,
            RegisterWebhookRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        String plainSecret = generateSecret();
        String encryptedSecret = encryptionService.encrypt(plainSecret);

        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .merchant(merchant)
                .url(request.getUrl())
                .secret(encryptedSecret)
                .isActive(true)
                .build();

        endpointRepository.save(endpoint);

        log.info("Webhook endpoint registered for merchant: {}", merchantId);
        return new WebhookEndpointResponse(
            endpoint.getId(),
            endpoint.getUrl(),
            endpoint.isActive(),
            plainSecret,
            "Store this secret safely. Use it to verify webhook signatures. It will not be shown again."
            );
    }

    public List<WebhookEndpointResponse> getEndpoints(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        return endpointRepository.findByMerchantAndIsActiveTrue(merchant)
            .stream()
            .map(e -> new WebhookEndpointResponse(
                    e.getId(),
                    e.getUrl(),
                    e.isActive(),
                    null,
                    null
            ))
            .toList();
    }

    @Async
    public void dispatchEvent(Merchant merchant, String eventType, Object eventData) {
        List<WebhookEndpoint> endpoints =
                endpointRepository.findByMerchantAndIsActiveTrue(merchant);

        if (endpoints.isEmpty()) {
            log.debug("No webhook endpoints for merchant: {}", merchant.getId());
            return;
        }

        String payload = serializePayload(eventType, eventData);

        for (WebhookEndpoint endpoint : endpoints) {
            WebhookDelivery delivery = WebhookDelivery.builder()
                    .webhookEndpoint(endpoint)
                    .eventType(eventType)
                    .payload(payload)
                    .status(WebhookDeliveryStatus.PENDING)
                    .attemptCount(0)
                    .nextRetryAt(LocalDateTime.now())
                    .build();

            deliveryRepository.save(delivery);
            attemptDelivery(delivery);
        }
    }

    public void retryDelivery(WebhookDelivery delivery) {
        log.info("Retrying webhook delivery: {}", delivery.getId());
        attemptDelivery(delivery);
    }

    private void attemptDelivery(WebhookDelivery delivery) {
        WebhookEndpoint endpoint = delivery.getWebhookEndpoint();

        try {
            // decrypt before computing HMAC — DB stores encrypted secret
            String plainSecret = encryptionService.decrypt(endpoint.getSecret());
            String signature = computeSignature(delivery.getPayload(), plainSecret);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.getUrl()))
                    .header("Content-Type", "application/json")
                    .header("X-PayUp-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayload()))
                    .build();

            HttpResponse<String> response = client.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setLastResponseCode(response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.setStatus(WebhookDeliveryStatus.SUCCESS);
                log.info("Webhook delivered successfully to: {}", endpoint.getUrl());
            } else {
                scheduleRetry(delivery);
            }

        } catch (Exception e) {
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setLastError(e.getMessage());
            scheduleRetry(delivery);
            log.warn("Webhook delivery failed: {}", e.getMessage());
        }

        deliveryRepository.save(delivery);
    }

    private void scheduleRetry(WebhookDelivery delivery) {
        int attempt = delivery.getAttemptCount();

        if (attempt >= MAX_ATTEMPTS) {
            delivery.setStatus(WebhookDeliveryStatus.EXHAUSTED);
            log.warn("Webhook exhausted after {} attempts", MAX_ATTEMPTS);
        } else {
            delivery.setStatus(WebhookDeliveryStatus.PENDING);
            long delayMinutes = RETRY_DELAYS_MINUTES[Math.min(attempt,
                    RETRY_DELAYS_MINUTES.length - 1)];
            delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            log.info("Webhook retry scheduled in {} minutes", delayMinutes);
        }
    }

    private String computeSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(payload.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    private String serializePayload(String eventType, Object eventData) {
        try {
            return objectMapper.writeValueAsString(
                    Map.of("event", eventType, "data", eventData,
                            "timestamp", LocalDateTime.now().toString()));
        } catch (Exception e) {
            return "{\"event\":\"" + eventType + "\"}";
        }
    }

    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "whsec_" + Base64.getEncoder().encodeToString(bytes);
    }
}
