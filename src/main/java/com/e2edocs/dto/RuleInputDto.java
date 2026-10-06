package com.e2edocs.dto;

import com.e2edocs.entity.enums.RuleStatus;
import com.e2edocs.entity.enums.RuleType;
import java.util.ArrayList;
import java.util.List;

public class RuleInputDto {
    private String name;
    private String description;
    private RuleStatus status = RuleStatus.ACTIVE;
    private RuleType ruleType = RuleType.DECISION;
    private Integer evaluationOrder = 1;
    private List<RuleConditionGroupDto> conditionGroups = new ArrayList<>();
    private List<RuleActionDto> actions = new ArrayList<>();

    public RuleInputDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Integer getEvaluationOrder() {
        return evaluationOrder;
    }

    public void setEvaluationOrder(Integer evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    public List<RuleConditionGroupDto> getConditionGroups() {
        return conditionGroups;
    }

    public void setConditionGroups(List<RuleConditionGroupDto> conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    public List<RuleActionDto> getActions() {
        return actions;
    }

    public void setActions(List<RuleActionDto> actions) {
        this.actions = actions;
    }
}
