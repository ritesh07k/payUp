package com.payup.webhook.repository;

import com.payup.webhook.entity.DeliveryStatus;
import com.payup.webhook.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(DeliveryStatus status, Instant now);
}
