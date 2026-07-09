package com.payup.refund.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic refundEventsTopic() {
        return TopicBuilder.name("refund-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundEventsDltTopic() {
        return TopicBuilder.name("refund-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
