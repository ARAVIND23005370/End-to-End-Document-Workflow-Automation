package com.project.documentworkflow.dto;

public class DecisionRequest {

    private Long documentId;
    private Long ocrDataId;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getOcrDataId() {
        return ocrDataId;
    }

    public void setOcrDataId(Long ocrDataId) {
        this.ocrDataId = ocrDataId;
    }
}
