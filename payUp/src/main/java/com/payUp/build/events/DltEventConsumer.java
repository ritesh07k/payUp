package com.payUp.build.events;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DltEventConsumer {

    @KafkaListener(topics = {"payment-events.DLT", "refund-events.DLT"},
            groupId = "payup-dlt-group")
    public void handleDltEvent(ConsumerRecord<String, Object> record) {
        log.error("Dead letter event received — topic: {}, partition: {}, offset: {}, value: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());
    }
}
