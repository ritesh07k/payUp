package com.payUp.build.ledger.service;

import com.payUp.build.exception.ResourceNotFoundException;
import com.payUp.build.ledger.dto.BalanceResponse;
import com.payUp.build.ledger.dto.LedgerEntryResponse;
import com.payUp.build.ledger.entity.EntryType;
import com.payUp.build.ledger.entity.LedgerEntry;
import com.payUp.build.ledger.entity.ReferenceType;
import com.payUp.build.ledger.repository.LedgerEntryRepository;
import com.payUp.build.merchant.entity.Merchant;
import com.payUp.build.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    @CacheEvict(value = "balances", key = "#merchant.id")
    public void recordEntry(Merchant merchant, EntryType entryType, Long amount,
            String currency, ReferenceType referenceType, UUID referenceId,
            String description) {

        LedgerEntry entry = LedgerEntry.builder()
                .merchant(merchant)
                .entryType(entryType)
                .amount(amount)
                .currency(currency)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .build();

        ledgerEntryRepository.save(entry);
    }

    @Cacheable(value = "balances", key = "#merchantId")
    public BalanceResponse getBalance(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        Long balance = ledgerEntryRepository.calculateBalance(merchant);
        return new BalanceResponse(balance, "INR");
    }

    public List<LedgerEntryResponse> getEntries(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        return ledgerEntryRepository.findByMerchantOrderByCreatedAtDesc(merchant)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LedgerEntryResponse toResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getEntryType(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getReferenceType(),
                entry.getReferenceId(),
                entry.getDescription(),
                entry.getCreatedAt()
        );
    }
}