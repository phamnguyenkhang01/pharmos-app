package com.pharmos.customer.domain;

public enum PreferredLanguage {
    VI("Tiếng Việt"),
    EN("English");

    private final String label;

    PreferredLanguage (String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PreferredLanguage fromCode(String code) {
        return valueOf(code.trim().toUpperCase());
    } 
}
