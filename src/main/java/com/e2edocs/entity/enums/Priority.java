package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    CRITICAL("critical"),
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    Priority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Priority fromValue(String value) {
        if (value == null) return null;
        for (Priority p : Priority.values()) {
            if (p.value.equalsIgnoreCase(value) || p.name().equalsIgnoreCase(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown Priority: " + value);
    }
}
