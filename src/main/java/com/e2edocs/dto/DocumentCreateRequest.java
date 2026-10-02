package com.e2edocs.dto;

import com.e2edocs.entity.enums.DocumentSource;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import java.util.Map;

public class DocumentCreateRequest {
    private String name;
    private String description;
    private String type;
    private Priority priority;
    private DocumentStatus status;
    private DocumentSource source;
    private String originalSenderEmail;
    private String originalSenderName;
    private String department;
    private String departmentId;
    private String assignedTo;
    private String assignedToId;
    private String tags;
    private Map<String, Object> metadata;

    public DocumentCreateRequest() {
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
