package com.e2edocs.entity;

import com.e2edocs.entity.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "rule_actions")
public class RuleAction {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    @JsonIgnore
    private Rule rule;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private ActionType type;

    @Column(name = "action_value")
    private String value;

    @Column(name = "email_config_json", columnDefinition = "TEXT")
    private String emailConfigJson;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public RuleAction() {
    }

    public RuleAction(String id, Rule rule, ActionType type, String value, String emailConfigJson, Integer sortOrder) {
        this.id = id;
        this.rule = rule;
        this.type = type;
        this.value = value;
        this.emailConfigJson = emailConfigJson;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
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

    public String getEmailConfigJson() {
        return emailConfigJson;
    }

    public void setEmailConfigJson(String emailConfigJson) {
        this.emailConfigJson = emailConfigJson;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
