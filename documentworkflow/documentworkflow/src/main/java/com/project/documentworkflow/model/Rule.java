package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "RULES")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    private String ruleName;
    private String conditionDescription;
    private double thresholdValue;
    private int priority;
    private boolean active = true;

    // NEW: Keywords that must ALL be present in document to pass this rule
    // Stored as comma-separated string e.g. "patient,doctor,hospital"
    @Column(name = "required_keywords", length = 1000)
    private String requiredKeywords;

    // NEW: Which document type this rule applies to
    // e.g. "MEDICAL", "LOAN", "STUDENT", "ALL" (applies to all types)
    @Column(name = "document_type")
    private String documentType = "ALL";

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getConditionDescription() { return conditionDescription; }
    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRequiredKeywords() { return requiredKeywords; }
    public void setRequiredKeywords(String requiredKeywords) {
        this.requiredKeywords = requiredKeywords;
    }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
