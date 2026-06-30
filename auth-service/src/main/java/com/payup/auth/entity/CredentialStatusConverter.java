package com.payup.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CredentialStatusConverter implements AttributeConverter<CredentialStatus, String> {

    @Override
    public String convertToDatabaseColumn(CredentialStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public CredentialStatus convertToEntityAttribute(String value) {
        return value == null ? null : CredentialStatus.valueOf(value);
    }
}
