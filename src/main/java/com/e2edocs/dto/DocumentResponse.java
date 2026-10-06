package com.e2edocs.dto;

import com.e2edocs.entity.enums.DocumentSource;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentResponse {
    private String id;
    private String name;
    private String description;
    private OriginalSenderDto originalSender;
    private String type;
    private DocumentStatus status;
    private Priority priority;
    private DocumentSource source;
    private String department;
    private String departmentId;
    private String assignedTo;
    private String assignedToId;
    private String decisionReason;
    private String missingFields;
    private String folder;
    private Instant createdAt;
    private Instant updatedAt;
    private Long size;
    private List<String> tags = new ArrayList<>();
    private Integer ruleMatches = 0;
    private String workflowId;
    private Map<String, Object> metadata = new HashMap<>();

    public DocumentResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public OriginalSenderDto getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(OriginalSenderDto originalSender) {
        this.originalSender = originalSender;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
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

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(String missingFields) {
        this.missingFields = missingFields;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
