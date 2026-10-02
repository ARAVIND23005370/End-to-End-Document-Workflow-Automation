package com.e2edocs.entity;

import com.e2edocs.entity.enums.ConditionLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rule_condition_groups")
public class RuleConditionGroup {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    @JsonIgnore
    private Rule rule;

    @Enumerated(EnumType.STRING)
    @Column(name = "logic", length = 16, nullable = false)
    private ConditionLogic logic = ConditionLogic.AND;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "conditionGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<RuleCondition> conditions = new ArrayList<>();

    public RuleConditionGroup() {
    }

    public RuleConditionGroup(String id, Rule rule, ConditionLogic logic, Integer sortOrder) {
        this.id = id;
        this.rule = rule;
        this.logic = logic != null ? logic : ConditionLogic.AND;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void addCondition(RuleCondition condition) {
        condition.setConditionGroup(this);
        this.conditions.add(condition);
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

    public ConditionLogic getLogic() {
        return logic;
    }

    public void setLogic(ConditionLogic logic) {
        this.logic = logic;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<RuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
}
