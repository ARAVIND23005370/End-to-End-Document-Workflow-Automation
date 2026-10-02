package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConditionLogic {
    AND("AND"),
    OR("OR");

    private final String value;

    ConditionLogic(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConditionLogic fromValue(String value) {
        if (value == null) return AND;
        for (ConditionLogic l : ConditionLogic.values()) {
            if (l.value.equalsIgnoreCase(value) || l.name().equalsIgnoreCase(value)) {
                return l;
            }
        }
        return AND;
    }
}
