package com.payup.webhook.repository;

import com.payup.webhook.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {
    List<WebhookEndpoint> findByMerchantIdAndActiveTrue(UUID merchantId);
}
