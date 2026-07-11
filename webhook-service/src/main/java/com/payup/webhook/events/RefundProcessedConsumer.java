package com.payup.webhook.events;

import com.payup.webhook.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundProcessedConsumer {

    private final WebhookDeliveryService webhookDeliveryService;

    @KafkaListener(topics = "refund-events", groupId = "webhook-service")
    public void consume(RefundProcessedEvent event) {
        webhookDeliveryService.dispatch(event.merchantId(), "refund.processed", event);
    }
}
