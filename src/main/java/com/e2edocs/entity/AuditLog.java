package com.e2edocs.entity;

import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "organization_id", length = 64, nullable = false)
    private String organizationId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 32, nullable = false)
    private AuditAction action;

    @Column(name = "resource", length = 64, nullable = false)
    private String resource;

    @Column(name = "resource_id", length = 64)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private AuditStatus status = AuditStatus.SUCCESS;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    public AuditLog() {
    }

    public AuditLog(String id, String organizationId, String userId, String userName, AuditAction action, String resource, String resourceId, AuditStatus status, String details, String ipAddress) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.status = status != null ? status : AuditStatus.SUCCESS;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
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
