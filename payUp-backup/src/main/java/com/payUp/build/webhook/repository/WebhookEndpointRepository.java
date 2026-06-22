package com.payUp.build.webhook.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.webhook.entity.WebhookEndpoint;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    List<WebhookEndpoint> findByMerchantAndIsActiveTrue(Merchant merchant);
}