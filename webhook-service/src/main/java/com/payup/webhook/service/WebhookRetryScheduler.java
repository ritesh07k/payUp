package com.payup.webhook.service;

import com.payup.webhook.entity.DeliveryStatus;
import com.payup.webhook.entity.WebhookDelivery;
import com.payup.webhook.entity.WebhookEndpoint;
import com.payup.webhook.repository.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookRetryScheduler {

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    @Scheduled(fixedRate = 60000)
    public void retryPendingDeliveries() {
        List<WebhookDelivery> dueDeliveries =
                webhookDeliveryRepository.findByStatusAndNextRetryAtBefore(DeliveryStatus.PENDING, Instant.now());

        if (dueDeliveries.isEmpty()) {
            return;
        }

        log.info("Found {} webhook deliveries due for retry", dueDeliveries.size());

        for (WebhookDelivery delivery : dueDeliveries) {
            webhookDeliveryService.findEndpoint(delivery.getWebhookEndpointId())
                    .ifPresentOrElse(
                            endpoint -> webhookDeliveryService.attemptDelivery(endpoint, delivery),
                            () -> log.warn("Endpoint {} not found for delivery {}, skipping",
                                    delivery.getWebhookEndpointId(), delivery.getId())
                    );
        }
    }
}
