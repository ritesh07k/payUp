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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestClient webhookDeliveryRestClient;
    private final ObjectMapper objectMapper;

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
            deliverToEndpoint(endpoint, eventType, payloadJson);
        }
    }

    private void deliverToEndpoint(WebhookEndpoint endpoint, String eventType, String payloadJson) {
        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setWebhookEndpointId(endpoint.getId());
        delivery.setEventType(eventType);
        delivery.setPayload(payloadJson);
        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setAttemptCount(1);

        try {
            var response = webhookDeliveryRestClient.post()
                    .uri(endpoint.getUrl())
                    .header("Content-Type", "application/json")
                    .body(payloadJson)
                    .retrieve()
                    .toBodilessEntity();

            delivery.setResponseCode(response.getStatusCode().value());
            delivery.setStatus(response.getStatusCode().is2xxSuccessful()
                    ? DeliveryStatus.DELIVERED : DeliveryStatus.FAILED);

        } catch (Exception e) {
            log.warn("Webhook delivery failed for endpoint {}: {}", endpoint.getId(), e.getMessage());
            delivery.setStatus(DeliveryStatus.FAILED);
        }

        webhookDeliveryRepository.save(delivery);
    }
}
