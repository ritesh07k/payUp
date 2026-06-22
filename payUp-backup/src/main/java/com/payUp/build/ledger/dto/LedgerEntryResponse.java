package com.payUp.build.ledger.dto;

import com.payUp.build.ledger.entity.EntryType;
import com.payUp.build.ledger.entity.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LedgerEntryResponse {

    private UUID id;
    private EntryType entryType;
    private Long amount;
    private String currency;
    private ReferenceType referenceType;
    private UUID referenceId;
    private String description;
    private LocalDateTime createdAt;
}