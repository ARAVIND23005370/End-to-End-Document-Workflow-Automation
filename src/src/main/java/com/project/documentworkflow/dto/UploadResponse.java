package com.project.documentworkflow.dto;

public class UploadResponse {

    private Long documentId;
    private String decision;
    private String status;

    public UploadResponse() {
    }

    public UploadResponse(Long documentId, String decision, String status) {
        this.documentId = documentId;
        this.decision = decision;
        this.status = status;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getDecision() {
        return decision;
    }

    public String getStatus() {
        return status;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
