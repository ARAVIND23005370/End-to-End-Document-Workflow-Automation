package com.project.documentworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_forwards")
public class DocumentForward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    // Who forwarded it
    private String forwardedBy;

    // Where it was forwarded (department name or email)
    private String forwardedTo;

    // DEPARTMENT or EMAIL
    private String forwardType;

    private String note;

    private LocalDateTime forwardedAt = LocalDateTime.now();

    // Getters
    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public String getForwardedBy() { return forwardedBy; }
    public String getForwardedTo() { return forwardedTo; }
    public String getForwardType() { return forwardType; }
    public String getNote() { return note; }
    public LocalDateTime getForwardedAt() { return forwardedAt; }

    // Setters
    public void setDocument(Document document) { this.document = document; }
    public void setForwardedBy(String forwardedBy) { this.forwardedBy = forwardedBy; }
    public void setForwardedTo(String forwardedTo) { this.forwardedTo = forwardedTo; }
    public void setForwardType(String forwardType) { this.forwardType = forwardType; }
    public void setNote(String note) { this.note = note; }
    public void setForwardedAt(LocalDateTime forwardedAt) { this.forwardedAt = forwardedAt; }
}
