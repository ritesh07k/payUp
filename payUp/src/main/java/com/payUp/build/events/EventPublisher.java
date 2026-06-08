package com.payUp.build.events;

import com.payUp.build.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send(KafkaConfig.PAYMENT_EVENTS_TOPIC,
                event.getPaymentId().toString(), event);
        log.info("Published payment event: {} for payment: {}",
                event.getEventType(), event.getPaymentId());
    }

    public void publishRefundEvent(RefundEvent event) {
        kafkaTemplate.send(KafkaConfig.REFUND_EVENTS_TOPIC,
                event.getRefundId().toString(), event);
        log.info("Published refund event: {} for refund: {}",
                event.getEventType(), event.getRefundId());
    }
}