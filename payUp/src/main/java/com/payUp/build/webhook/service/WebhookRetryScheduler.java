package com.payUp.build.webhook.service;

import com.payUp.build.webhook.entity.WebhookDelivery;
import com.payUp.build.webhook.entity.WebhookDeliveryStatus;
import com.payUp.build.webhook.repository.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookService webhookService;

    @Scheduled(fixedDelay = 120000)
    public void retryPendingDeliveries() {
        List<WebhookDelivery> pending = deliveryRepository
                .findByStatusAndNextRetryAtBefore(
                        WebhookDeliveryStatus.PENDING,
                        LocalDateTime.now());

        if (!pending.isEmpty()) {
            log.info("Retrying {} pending webhook deliveries", pending.size());
            pending.forEach(webhookService::retryDelivery);
        }
    }
}