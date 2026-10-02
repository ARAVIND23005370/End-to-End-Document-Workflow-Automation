package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AuditStatus {
    SUCCESS("success"),
    FAILURE("failure"),
    WARNING("warning");

    private final String value;

    AuditStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AuditStatus fromValue(String value) {
        if (value == null) return SUCCESS;
        for (AuditStatus s : AuditStatus.values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        return SUCCESS;
    }
}
