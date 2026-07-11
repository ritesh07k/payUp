package com.payup.webhook.events;

import com.payup.webhook.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCapturedConsumer {

    private final WebhookDeliveryService webhookDeliveryService;

    @KafkaListener(topics = "payment-events", groupId = "webhook-service")
    public void consume(PaymentCapturedEvent event) {
        webhookDeliveryService.dispatch(event.merchantId(), "payment.captured", event);
    }
}
