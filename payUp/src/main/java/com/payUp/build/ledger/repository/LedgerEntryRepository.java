package com.payUp.build.ledger.repository;

import com.payUp.build.ledger.entity.LedgerEntry;
import com.payUp.build.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByMerchantOrderByCreatedAtDesc(Merchant merchant);

    @Query("SELECT COALESCE(SUM(CASE WHEN l.entryType = 'CREDIT' THEN l.amount " +
           "ELSE -l.amount END), 0) " +
           "FROM LedgerEntry l WHERE l.merchant = :merchant")
    Long calculateBalance(@Param("merchant") Merchant merchant);
}