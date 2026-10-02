package com.e2edocs.dto;

import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import java.time.Instant;

public class AuditEntryResponse {
    private String id;
    private Instant timestamp;
    private String userId;
    private String userName;
    private AuditAction action;
    private String resource;
    private String resourceId;
    private AuditStatus status;
    private String details;
    private String ipAddress;

    public AuditEntryResponse() {
    }

    public AuditEntryResponse(String id, Instant timestamp, String userId, String userName, AuditAction action, String resource, String resourceId, AuditStatus status, String details, String ipAddress) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.status = status;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditStatus status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
