package com.payup.refund.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${payment-service.base-url}")
    private String paymentServiceBaseUrl;

    @Bean
    public RestClient paymentServiceRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        return RestClient.builder()
                .baseUrl(paymentServiceBaseUrl)
                .requestFactory(factory)
                .build();
    }
}
