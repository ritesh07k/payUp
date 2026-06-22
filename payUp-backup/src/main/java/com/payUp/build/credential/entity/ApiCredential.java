package com.payUp.build.credential.entity;

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
@Table(name = "api_credentials")
public class ApiCredential extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String apiSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeyType keyType;

    @Column(nullable = false)
    private boolean isActive;
}