package com.payUp.build.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final String REFUND_EVENTS_TOPIC = "refund-events";
    public static final String WEBHOOK_EVENTS_TOPIC = "webhook-events";

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundEventsTopic() {
        return TopicBuilder.name(REFUND_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic webhookEventsTopic() {
        return TopicBuilder.name(WEBHOOK_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}