package com.e2edocs.dto;

import java.time.Instant;

public class AuditHistoryItem {
    private String id;
    private Instant timestamp;
    private String userId;
    private String userName;
    private String action;
    private String resource;
    private String resourceId;
    private String status;
    private String details;

    public AuditHistoryItem() {
    }

    public AuditHistoryItem(String id, Instant timestamp, String userId, String userName, String action, String resource, String resourceId, String status, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.status = status;
        this.details = details;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
