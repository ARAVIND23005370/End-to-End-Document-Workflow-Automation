package com.e2edocs.dto;

import com.e2edocs.entity.enums.NotificationType;
import com.e2edocs.entity.enums.Priority;
import java.time.Instant;

public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private String documentId;
    private String documentName;
    private Instant timestamp;
    private Priority priority;

    public NotificationResponse() {
    }

    public NotificationResponse(String id, String title, String message, NotificationType type, boolean read, String documentId, String documentName, Instant timestamp, Priority priority) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.documentId = documentId;
        this.documentName = documentName;
        this.timestamp = timestamp;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
