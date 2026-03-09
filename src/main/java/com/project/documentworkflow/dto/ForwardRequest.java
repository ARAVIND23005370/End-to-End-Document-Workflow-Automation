package com.project.documentworkflow.dto;

public class ForwardRequest {
    private Long documentId;
    private String forwardedTo;  // department name or email
    private String forwardType;  // DEPARTMENT or EMAIL
    private String note;

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getForwardedTo() { return forwardedTo; }
    public void setForwardedTo(String forwardedTo) { this.forwardedTo = forwardedTo; }

    public String getForwardType() { return forwardType; }
    public void setForwardType(String forwardType) { this.forwardType = forwardType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
