package com.payUp.build.merchant.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MerchantStatusConverter implements AttributeConverter<MerchantStatus, String> {

    @Override
    public String convertToDatabaseColumn(MerchantStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public MerchantStatus convertToEntityAttribute(String value) {
        return value == null ? null : MerchantStatus.valueOf(value);
    }
}