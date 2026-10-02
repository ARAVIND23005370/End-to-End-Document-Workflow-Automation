package com.e2edocs.entity;

import com.e2edocs.entity.enums.DocumentSource;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "organization_id", length = 64, nullable = false)
    private String organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type")
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32, nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 32, nullable = false)
    private DocumentSource source = DocumentSource.MANUAL_UPLOAD;

    @Column(name = "original_sender_email")
    private String originalSenderEmail;

    @Column(name = "original_sender_name")
    private String originalSenderName;

    @Column(name = "department")
    private String department;

    @Column(name = "department_id", length = 64)
    private String departmentId;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_to_id", length = 64)
    private String assignedToId;

    @Column(name = "size")
    private Long size = 0L;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "rule_matches")
    private Integer ruleMatches = 0;

    @Column(name = "workflow_id", length = 64)
    private String workflowId;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "content_preview", columnDefinition = "TEXT")
    private String contentPreview;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_extension", length = 32)
    private String fileExtension;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Document() {
    }

    public Document(String id, String organizationId, String name, String type) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.status = DocumentStatus.DRAFT;
        this.priority = Priority.MEDIUM;
        this.source = DocumentSource.MANUAL_UPLOAD;
        this.ruleMatches = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.ruleMatches == null) {
            this.ruleMatches = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public DocumentSource getSource() {
        return source;
    }

    public void setSource(DocumentSource source) {
        this.source = source;
    }

    public String getOriginalSenderEmail() {
        return originalSenderEmail;
    }

    public void setOriginalSenderEmail(String originalSenderEmail) {
        this.originalSenderEmail = originalSenderEmail;
    }

    public String getOriginalSenderName() {
        return originalSenderName;
    }

    public void setOriginalSenderName(String originalSenderName) {
        this.originalSenderName = originalSenderName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(String assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getRuleMatches() {
        return ruleMatches;
    }

    public void setRuleMatches(Integer ruleMatches) {
        this.ruleMatches = ruleMatches;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
