package com.project.documentworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which document this audit is about
    private Long documentId;

    // What action happened: UPLOADED, APPROVED, REJECTED, FORWARDED, EMAIL_SENT, etc.
    private String action;

    // Who did this action (email or SYSTEM)
    private String performedBy;

    // Extra details about the action
    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime actionTime = LocalDateTime.now();

    // Getters
    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public String getAction() { return action; }
    public String getPerformedBy() { return performedBy; }
    public String getDetails() { return details; }
    public LocalDateTime getActionTime() { return actionTime; }

    // Setters
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public void setAction(String action) { this.action = action; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public void setDetails(String details) { this.details = details; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
