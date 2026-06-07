package com.payUp.build.webhook.entity;

import com.payUp.build.common.entity.BaseEntity;
import com.payUp.build.merchant.entity.Merchant;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private String url;

    // TODO: Phase 10 - encrypt this field using AES-256 before production
    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    private boolean isActive;
}
