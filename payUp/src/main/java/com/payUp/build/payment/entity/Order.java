package com.payUp.build.payment.entity;

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
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column
    private String receipt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
}