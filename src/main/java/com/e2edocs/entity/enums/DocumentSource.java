package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentSource {
    MANUAL_UPLOAD("manual_upload"),
    EMAIL("email"),
    API("api"),
    INTEGRATION("integration"),
    SCANNED("scanned");

    private final String value;

    DocumentSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DocumentSource fromValue(String value) {
        if (value == null) return null;
        for (DocumentSource s : DocumentSource.values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown DocumentSource: " + value);
    }
}
