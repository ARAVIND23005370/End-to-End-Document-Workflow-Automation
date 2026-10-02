package com.e2edocs.entity;

import com.e2edocs.entity.enums.ConditionOperator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "rule_conditions")
public class RuleCondition {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_group_id", nullable = false)
    @JsonIgnore
    private RuleConditionGroup conditionGroup;

    @Column(name = "field", nullable = false)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", length = 32, nullable = false)
    private ConditionOperator operator;

    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public RuleCondition() {
    }

    public RuleCondition(String id, RuleConditionGroup conditionGroup, String field, ConditionOperator operator, String value, Integer sortOrder) {
        this.id = id;
        this.conditionGroup = conditionGroup;
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RuleConditionGroup getConditionGroup() {
        return conditionGroup;
    }

    public void setConditionGroup(RuleConditionGroup conditionGroup) {
        this.conditionGroup = conditionGroup;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
