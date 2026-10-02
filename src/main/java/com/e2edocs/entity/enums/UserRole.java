package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    SUPER_ADMIN("super_admin"),
    ADMIN("admin"),
    MANAGER("manager"),
    REVIEWER("reviewer"),
    USER("user");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) return null;
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown UserRole: " + value);
    }
}
