package com.payup.payment.events;

import com.payup.payment.entity.LedgerEntry;
import com.payup.payment.entity.LedgerEntryType;
import com.payup.payment.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundProcessedConsumer {

    private final LedgerEntryRepository ledgerEntryRepository;

    @KafkaListener(topics = "refund-events", groupId = "payment-service")
    @Transactional
    public void consume(RefundProcessedEvent event) {
        boolean alreadyProcessed = ledgerEntryRepository
                .existsByReferenceIdAndReferenceType(event.refundId(), "REFUND");

        if (alreadyProcessed) {
            log.info("Refund {} already recorded in ledger, skipping", event.refundId());
            return;
        }

        LedgerEntry entry = new LedgerEntry();
        entry.setMerchantId(event.merchantId());
        entry.setReferenceId(event.refundId());
        entry.setReferenceType("REFUND");
        entry.setEntryType(LedgerEntryType.DEBIT);
        entry.setAmountPaise(event.amountPaise());
        ledgerEntryRepository.save(entry);

        log.info("Recorded ledger DEBIT for refund {}", event.refundId());
    }
}
