package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AuditAction {
    CREATED("created"),
    UPDATED("updated"),
    DELETED("deleted"),
    APPROVED("approved"),
    REJECTED("rejected"),
    ASSIGNED("assigned"),
    ROUTED("routed"),
    REVIEWED("reviewed"),
    EXPORTED("exported"),
    UPLOADED("uploaded"),
    SENT("sent");

    private final String value;

    AuditAction(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AuditAction fromValue(String value) {
        if (value == null) return null;
        for (AuditAction a : AuditAction.values()) {
            if (a.value.equalsIgnoreCase(value) || a.name().equalsIgnoreCase(value)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown AuditAction: " + value);
    }
}
