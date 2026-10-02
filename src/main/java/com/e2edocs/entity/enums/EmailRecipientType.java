package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailRecipientType {
    ORIGINAL_SENDER("original_sender"),
    ASSIGNED_USER("assigned_user"),
    DEPARTMENT("department"),
    CUSTOM("custom");

    private final String value;

    EmailRecipientType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EmailRecipientType fromValue(String value) {
        if (value == null) return null;
        for (EmailRecipientType r : EmailRecipientType.values()) {
            if (r.value.equalsIgnoreCase(value) || r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown EmailRecipientType: " + value);
    }
}
