package com.payup.merchant.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCreatedEvent {
    private UUID merchantId;
    private String businessName;
    private LocalDateTime occurredAt;
}
