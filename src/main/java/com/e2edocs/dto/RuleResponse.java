package com.e2edocs.dto;

import com.e2edocs.entity.enums.RuleStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RuleResponse {
    private String id;
    private String name;
    private String description;
    private RuleStatus status;
    private Integer evaluationOrder;
    private List<RuleConditionGroupDto> conditionGroups = new ArrayList<>();
    private List<RuleActionDto> actions = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private Integer matchCount = 0;
    private Instant lastTriggered;

    public RuleResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(Integer matchCount) {
        this.matchCount = matchCount;
    }

    public Instant getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(Instant lastTriggered) {
        this.lastTriggered = lastTriggered;
    }
}
