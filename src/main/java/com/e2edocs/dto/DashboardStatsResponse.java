package com.e2edocs.dto;

public class DashboardStatsResponse {
    private long totalDocuments;
    private long processing;
    private long approved;
    private long review;
    private long rejected;
    private long draft;
    private long criticalItems;
    private long activeRules;
    private long activeWorkflows;

    public DashboardStatsResponse() {
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getProcessing() {
        return processing;
    }

    public void setProcessing(long processing) {
        this.processing = processing;
    }

    public long getApproved() {
        return approved;
    }

    public void setApproved(long approved) {
        this.approved = approved;
    }

    public long getReview() {
        return review;
    }

    public void setReview(long review) {
        this.review = review;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getDraft() {
        return draft;
    }

    public void setDraft(long draft) {
        this.draft = draft;
    }

    public long getCriticalItems() {
        return criticalItems;
    }

    public void setCriticalItems(long criticalItems) {
        this.criticalItems = criticalItems;
    }

    public long getActiveRules() {
        return activeRules;
    }

    public void setActiveRules(long activeRules) {
        this.activeRules = activeRules;
    }

    public long getActiveWorkflows() {
        return activeWorkflows;
    }

    public void setActiveWorkflows(long activeWorkflows) {
        this.activeWorkflows = activeWorkflows;
    }
}
