package com.payUp.build.events;

import com.payUp.build.merchant.repository.MerchantRepository;
import com.payUp.build.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventConsumer {

    private final WebhookService webhookService;
    private final MerchantRepository merchantRepository;

    @KafkaListener(topics = "refund-events",
            groupId = "payup-group")
    public void handleRefundEvent(RefundEvent event) {
        log.info("Consumed refund event: {} for refund: {}",
                event.getEventType(), event.getRefundId());

        merchantRepository.findById(event.getMerchantId())
                .ifPresent(merchant ->
                        webhookService.dispatchEvent(merchant,
                                event.getEventType(), event));
    }
}