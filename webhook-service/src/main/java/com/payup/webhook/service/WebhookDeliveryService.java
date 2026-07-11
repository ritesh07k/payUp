package com.payup.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payup.webhook.entity.DeliveryStatus;
import com.payup.webhook.entity.WebhookDelivery;
import com.payup.webhook.entity.WebhookEndpoint;
import com.payup.webhook.repository.WebhookDeliveryRepository;
import com.payup.webhook.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookEncryptionService webhookEncryptionService;
    private final HmacSigningService hmacSigningService;
    private final RestClient webhookDeliveryRestClient;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 4;
    // attempt 1 -> retry in 1 min, attempt 2 -> 5 min, attempt 3 -> 30 min, attempt 4 -> DLQ
    private static final int[] BACKOFF_MINUTES = {1, 5, 30};

    public void dispatch(UUID merchantId, String eventType, Object eventPayload) {
        List<WebhookEndpoint> endpoints =
                webhookEndpointRepository.findByMerchantIdAndActiveTrue(merchantId);

        if (endpoints.isEmpty()) {
            log.info("No active webhook endpoints for merchant {}, skipping dispatch", merchantId);
            return;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(eventPayload);
        } catch (Exception e) {
            log.error("Failed to serialize event payload for merchant {}", merchantId, e);
            return;
        }

        for (WebhookEndpoint endpoint : endpoints) {
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setWebhookEndpointId(endpoint.getId());
            delivery.setEventType(eventType);
            delivery.setPayload(payloadJson);
            delivery.setStatus(DeliveryStatus.PENDING);
            delivery.setAttemptCount(0);
            delivery = webhookDeliveryRepository.save(delivery);

            attemptDelivery(endpoint, delivery);
        }
    }

    public void attemptDelivery(WebhookEndpoint endpoint, WebhookDelivery delivery) {
        String plainSecret = webhookEncryptionService.decrypt(endpoint.getSecret());
        String signature = hmacSigningService.sign(delivery.getPayload(), plainSecret);

        delivery.setAttemptCount(delivery.getAttemptCount() + 1);

        try {
            var response = webhookDeliveryRestClient.post()
                    .uri(endpoint.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Payup-Signature", signature)
                    .body(delivery.getPayload())
                    .retrieve()
                    .toBodilessEntity();

            delivery.setResponseCode(response.getStatusCode().value());

            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus(DeliveryStatus.DELIVERED);
                delivery.setNextRetryAt(null);
            } else {
                scheduleRetryOrFail(delivery);
            }

        } catch (Exception e) {
            log.warn("Webhook delivery attempt {} failed for endpoint {}: {}",
                    delivery.getAttemptCount(), endpoint.getId(), e.getMessage());
            scheduleRetryOrFail(delivery);
        }

        webhookDeliveryRepository.save(delivery);
    }

    private void scheduleRetryOrFail(WebhookDelivery delivery) {
        int attempt = delivery.getAttemptCount();

        if (attempt >= MAX_ATTEMPTS) {
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setNextRetryAt(null);
            log.warn("Webhook delivery {} exhausted {} attempts, marking FAILED (DLQ)",
                    delivery.getId(), MAX_ATTEMPTS);
        } else {
            int backoffMinutes = BACKOFF_MINUTES[attempt - 1];
            delivery.setStatus(DeliveryStatus.PENDING);
            delivery.setNextRetryAt(Instant.now().plus(backoffMinutes, ChronoUnit.MINUTES));
        }
    }

    public Optional<WebhookEndpoint> findEndpoint(UUID endpointId) {
        return webhookEndpointRepository.findById(endpointId);
    }
}
