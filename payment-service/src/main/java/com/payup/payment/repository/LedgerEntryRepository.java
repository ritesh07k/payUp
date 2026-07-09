package com.payup.payment.repository;

import com.payup.payment.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    boolean existsByReferenceIdAndReferenceType(UUID referenceId, String referenceType);
}
