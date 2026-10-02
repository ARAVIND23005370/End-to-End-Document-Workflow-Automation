package com.e2edocs.dto;

import java.util.ArrayList;
import java.util.List;

public class DocumentDetailResponse extends DocumentResponse {
    private String contentPreview;
    private List<DetectedInfoItem> detectedInfo = new ArrayList<>();
    private List<RuleEvaluationItem> ruleEvaluations = new ArrayList<>();
    private List<AuditHistoryItem> auditHistory = new ArrayList<>();
    private List<NotificationResponse> notifications = new ArrayList<>();

    public DocumentDetailResponse() {
        super();
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public List<DetectedInfoItem> getDetectedInfo() {
        return detectedInfo;
    }

    public void setDetectedInfo(List<DetectedInfoItem> detectedInfo) {
        this.detectedInfo = detectedInfo;
    }

    public List<RuleEvaluationItem> getRuleEvaluations() {
        return ruleEvaluations;
    }

    public void setRuleEvaluations(List<RuleEvaluationItem> ruleEvaluations) {
        this.ruleEvaluations = ruleEvaluations;
    }

    public List<AuditHistoryItem> getAuditHistory() {
        return auditHistory;
    }

    public void setAuditHistory(List<AuditHistoryItem> auditHistory) {
        this.auditHistory = auditHistory;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }
}
