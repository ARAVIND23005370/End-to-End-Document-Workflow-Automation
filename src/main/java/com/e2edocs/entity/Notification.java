package com.e2edocs.entity;

import com.e2edocs.entity.enums.NotificationType;
import com.e2edocs.entity.enums.Priority;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "organization_id", length = 64, nullable = false)
    private String organizationId;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private NotificationType type = NotificationType.INFO;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "document_id", length = 64)
    private String documentId;

    @Column(name = "document_name")
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 32)
    private Priority priority;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public Notification() {
    }

    public Notification(String id, String organizationId, String userId, String title, String message, NotificationType type) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : NotificationType.INFO;
        this.read = false;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
