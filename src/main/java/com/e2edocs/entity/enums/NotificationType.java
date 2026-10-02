package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationType fromValue(String value) {
        if (value == null) return INFO;
        for (NotificationType t : NotificationType.values()) {
            if (t.value.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        return INFO;
    }
}
