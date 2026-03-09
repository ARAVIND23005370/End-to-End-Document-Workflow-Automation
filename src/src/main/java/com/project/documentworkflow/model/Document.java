package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String documentType;
    private String filePath;
    private String status;

    // --- GETTERS ---
    public Long getDocumentId() {
        return documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
    }

    // --- SETTERS ---
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
