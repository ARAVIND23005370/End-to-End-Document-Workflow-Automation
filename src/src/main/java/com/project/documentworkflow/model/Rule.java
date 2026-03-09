package com.project.documentworkflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    private String ruleName;
    private String conditionDescription;
    private Double thresholdValue;

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }
}
