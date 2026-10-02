package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConditionOperator {
    EQUALS("equals"),
    NOT_EQUALS("not_equals"),
    CONTAINS("contains"),
    NOT_CONTAINS("not_contains"),
    STARTS_WITH("starts_with"),
    ENDS_WITH("ends_with"),
    MATCHES("matches"),
    GREATER_THAN("greater_than"),
    LESS_THAN("less_than"),
    IN("in"),
    NOT_IN("not_in");

    private final String value;

    ConditionOperator(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConditionOperator fromValue(String value) {
        if (value == null) return null;
        for (ConditionOperator op : ConditionOperator.values()) {
            if (op.value.equalsIgnoreCase(value) || op.name().equalsIgnoreCase(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown ConditionOperator: " + value);
    }
}
