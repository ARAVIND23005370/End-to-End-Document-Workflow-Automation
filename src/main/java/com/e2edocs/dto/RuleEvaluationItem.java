package com.e2edocs.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RuleEvaluationItem {
    private String ruleId;
    private String ruleName;
    private boolean matched;
    private Instant evaluatedAt;
    private int conditionsChecked;
    private List<String> actionsTriggered = new ArrayList<>();

    public RuleEvaluationItem() {
    }

    public RuleEvaluationItem(String ruleId, String ruleName, boolean matched, Instant evaluatedAt, int conditionsChecked, List<String> actionsTriggered) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.matched = matched;
        this.evaluatedAt = evaluatedAt;
        this.conditionsChecked = conditionsChecked;
        this.actionsTriggered = actionsTriggered != null ? actionsTriggered : new ArrayList<>();
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public int getConditionsChecked() {
        return conditionsChecked;
    }

    public void setConditionsChecked(int conditionsChecked) {
        this.conditionsChecked = conditionsChecked;
    }

    public List<String> getActionsTriggered() {
        return actionsTriggered;
    }

    public void setActionsTriggered(List<String> actionsTriggered) {
        this.actionsTriggered = actionsTriggered;
    }
}
