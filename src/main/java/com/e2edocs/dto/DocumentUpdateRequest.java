package com.e2edocs.dto;

import com.e2edocs.entity.enums.DocumentSource;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import java.util.List;
import java.util.Map;

public class DocumentUpdateRequest {
    private String name;
    private String description;
    private String type;
    private DocumentStatus status;
    private Priority priority;
    private DocumentSource source;
    private OriginalSenderDto originalSender;
    private String department;
    private String departmentId;
    private String assignedTo;
    private String assignedToId;
    private List<String> tags;
    private String workflowId;
    private Map<String, Object> metadata;

    public DocumentUpdateRequest() {
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

    public OriginalSenderDto getOriginalSender() {
        return originalSender;
    }

    public void setOriginalSender(OriginalSenderDto originalSender) {
        this.originalSender = originalSender;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
