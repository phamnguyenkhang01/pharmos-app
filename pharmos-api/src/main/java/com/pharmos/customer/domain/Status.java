package com.pharmos.customer.domain;

public enum Status {
    UNVERIFIED,
    ACTIVE,
    DISABLED;

    public static Status fromCode(String code) {
        return valueOf(code.trim().toUpperCase());
    }
}
