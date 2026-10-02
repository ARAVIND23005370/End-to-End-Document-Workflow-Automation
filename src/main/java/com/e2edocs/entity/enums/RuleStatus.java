package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuleStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    DRAFT("draft");

    private final String value;

    RuleStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RuleStatus fromValue(String value) {
        if (value == null) return null;
        for (RuleStatus s : RuleStatus.values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown RuleStatus: " + value);
    }
}
