package com.payUp.build.webhook.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.webhook.entity.WebhookDelivery;
import com.payUp.build.webhook.entity.WebhookDeliveryStatus;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(
            WebhookDeliveryStatus status, LocalDateTime time);
}