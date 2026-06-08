package com.payUp.build.events;

import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final WebhookService webhookService;
    private final MerchantRepository merchantRepository;

    @KafkaListener(topics = "payment-events",
            groupId = "payup-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Consumed payment event: {} for payment: {}",
                event.getEventType(), event.getPaymentId());

        merchantRepository.findById(event.getMerchantId())
                .ifPresent(merchant ->
                        webhookService.dispatchEvent(merchant,
                                event.getEventType(), event));
    }
}
