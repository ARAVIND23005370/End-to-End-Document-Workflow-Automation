package com.e2edocs.dto;

import com.e2edocs.entity.enums.EmailRecipientType;

public class EmailConfigDto {
    private EmailRecipientType recipientType;
    private String customRecipient;
    private String subject;
    private String message;

    public EmailConfigDto() {
    }

    public EmailConfigDto(EmailRecipientType recipientType, String customRecipient, String subject, String message) {
        this.recipientType = recipientType;
        this.customRecipient = customRecipient;
        this.subject = subject;
        this.message = message;
    }

    public EmailRecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(EmailRecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public String getCustomRecipient() {
        return customRecipient;
    }

    public void setCustomRecipient(String customRecipient) {
        this.customRecipient = customRecipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
