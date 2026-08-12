package com.pharmos.product.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public Status convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : Status.fromCode(dbValue);
    }
}
