package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentStatus {
    DRAFT("draft"),
    PROCESSING("processing"),
    REVIEW("review"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    DocumentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DocumentStatus fromValue(String value) {
        if (value == null) return null;
        for (DocumentStatus s : DocumentStatus.values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown DocumentStatus: " + value);
    }
}
