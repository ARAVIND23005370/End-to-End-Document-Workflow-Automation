package com.e2edocs.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ActionType {
    SET_PRIORITY("set_priority"),
    ASSIGN_USER("assign_user"),
    ASSIGN_DEPARTMENT("assign_department"),
    ASSIGN_TEAM("assign_team"),
    ASSIGN_QUEUE("assign_queue"),
    ASSIGN_FOLDER("assign_folder"),
    START_WORKFLOW("start_workflow"),
    SEND_EMAIL("send_email"),
    SEND_NOTIFICATION("send_notification"),
    FORWARD_DOCUMENT("forward_document"),
    SET_DECISION("set_decision"),
    ADD_TAG("add_tag");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ActionType fromValue(String value) {
        if (value == null) return null;
        for (ActionType a : ActionType.values()) {
            if (a.value.equalsIgnoreCase(value) || a.name().equalsIgnoreCase(value)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown ActionType: " + value);
    }
}
