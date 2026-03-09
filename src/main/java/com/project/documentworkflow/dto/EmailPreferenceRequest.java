package com.project.documentworkflow.dto;

public class EmailPreferenceRequest {
    private Boolean emailNotifyOnReject;

    public Boolean getEmailNotifyOnReject() { return emailNotifyOnReject; }
    public void setEmailNotifyOnReject(Boolean emailNotifyOnReject) {
        this.emailNotifyOnReject = emailNotifyOnReject;
    }
}
