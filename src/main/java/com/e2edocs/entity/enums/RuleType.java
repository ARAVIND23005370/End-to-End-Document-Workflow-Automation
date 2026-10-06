package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuleType {
    DECISION("decision"),
    FOLDER("folder"),
    SORTING("sorting"),
    ROUTING("routing"),
    COMMUNICATION("communication");

    private final String value;

    RuleType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RuleType fromValue(String value) {
        if (value == null || value.isBlank()) return DECISION;
        for (RuleType r : RuleType.values()) {
            if (r.value.equalsIgnoreCase(value) || r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        return DECISION;
    }
}
