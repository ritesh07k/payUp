package com.payUp.build.ledger.entity;

import com.payUp.build.common.entity.ImmutableEntity;
import com.payUp.build.merchant.entity.Merchant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends ImmutableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private java.util.UUID referenceId;

    @Column
    private String description;
}