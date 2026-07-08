package com.payup.merchant.events;

import com.payup.merchant.entity.Merchant;
import com.payup.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantCreatedConsumer {

    private final MerchantRepository merchantRepository;

    @KafkaListener(topics = "merchant-created-events", groupId = "merchant-service-group")
    public void handleMerchantCreated(MerchantCreatedEvent event) {
        log.info("Consumed MerchantCreated event for merchantId: {}", event.getMerchantId());

        if (merchantRepository.existsById(event.getMerchantId())) {
            log.warn("Merchant {} already exists, skipping", event.getMerchantId());
            return;
        }

        Merchant merchant = Merchant.builder()
                .id(event.getMerchantId())
                .businessName(event.getBusinessName())
                .build();

        merchantRepository.save(merchant);
        log.info("Merchant profile created for merchantId: {}", event.getMerchantId());
    }
}
