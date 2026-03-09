package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    private String ruleName;
    private String conditionDescription;
    private Double thresholdValue;

    // FEATURE: Priority — lower number = checked first
    // e.g. priority 1 is checked before priority 2
    private Integer priority = 1;

    // Is this rule active or disabled?
    private Boolean active = true;

    public Long getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public String getConditionDescription() { return conditionDescription; }
    public Double getThresholdValue() { return thresholdValue; }
    public Integer getPriority() { return priority; }
    public Boolean getActive() { return active; }

    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public void setConditionDescription(String conditionDescription) { this.conditionDescription = conditionDescription; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setActive(Boolean active) { this.active = active; }
}
