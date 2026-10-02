package com.e2edocs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class DocumentSendEmailRequest {

    @NotBlank(message = "Recipient email address is required")
    @Email(message = "Recipient email must be a valid email address")
    private String recipient;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String message;

    public DocumentSendEmailRequest() {
    }

    public DocumentSendEmailRequest(String recipient, String subject, String message) {
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
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
