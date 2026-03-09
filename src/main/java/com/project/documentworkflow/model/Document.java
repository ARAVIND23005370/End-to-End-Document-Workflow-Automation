package com.project.documentworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String documentType;
    private String filePath;
    private String status;

    // FEATURE: Folder sorting — auto-set based on documentType
    private String folderPath;

    // FEATURE: Priority ordering — 1=HIGH, 2=MEDIUM, 3=LOW
    private Integer priority;

    // FEATURE: Department routing — which department this document belongs to
    private String department;

    // Who uploaded this document (email)
    private String uploadedByEmail;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    // --- GETTERS ---
    public Long getDocumentId() { return documentId; }
    public String getDocumentType() { return documentType; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }
    public String getFolderPath() { return folderPath; }
    public Integer getPriority() { return priority; }
    public String getDepartment() { return department; }
    public String getUploadedByEmail() { return uploadedByEmail; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    // --- SETTERS ---
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setStatus(String status) { this.status = status; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setDepartment(String department) { this.department = department; }
    public void setUploadedByEmail(String uploadedByEmail) { this.uploadedByEmail = uploadedByEmail; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
