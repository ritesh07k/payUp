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
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMethod paymentMethod;

    @Column
    private String bankReferenceId;

    @Column
    private String failureReason;
}