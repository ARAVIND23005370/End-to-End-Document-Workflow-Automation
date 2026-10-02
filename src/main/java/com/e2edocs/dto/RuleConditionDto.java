package com.e2edocs.dto;

import com.e2edocs.entity.enums.ConditionOperator;

public class RuleConditionDto {
    private String id;
    private String field;
    private ConditionOperator operator;
    private String value;

    public RuleConditionDto() {
    }

    public RuleConditionDto(String id, String field, ConditionOperator operator, String value) {
        this.id = id;
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
