package com.pharmos.product.domain;

public enum Status {
    DRAFT,
    PUBLISHED,
    UNPUBLISHED;

    public static Status fromCode(String code) {
        return valueOf(code.trim().toUpperCase());
    }
}
