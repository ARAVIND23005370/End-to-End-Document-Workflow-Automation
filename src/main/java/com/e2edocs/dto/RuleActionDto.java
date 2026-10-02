package com.e2edocs.dto;

import com.e2edocs.entity.enums.ActionType;

public class RuleActionDto {
    private String id;
    private ActionType type;
    private String value;
    private EmailConfigDto emailConfig;

    public RuleActionDto() {
    }

    public RuleActionDto(String id, ActionType type, String value, EmailConfigDto emailConfig) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.emailConfig = emailConfig;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public EmailConfigDto getEmailConfig() {
        return emailConfig;
    }

    public void setEmailConfig(EmailConfigDto emailConfig) {
        this.emailConfig = emailConfig;
    }
}
