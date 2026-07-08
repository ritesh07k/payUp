package com.payup.auth.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String MERCHANT_CREATED_TOPIC = "merchant-created-events";

    @Bean
    public NewTopic merchantCreatedTopic() {
        return TopicBuilder.name(MERCHANT_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
