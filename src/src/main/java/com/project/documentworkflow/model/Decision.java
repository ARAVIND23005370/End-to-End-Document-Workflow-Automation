package com.project.documentworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "decisions")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    private String decisionType;

    private String decisionSource;

    @Column(columnDefinition = "TEXT")
    private String decisionReason;

    private String decisionStatus;   // ACTIVE / SUPERSEDED

    private String evaluatedBy;      // SYSTEM / USER

    private Integer versionNumber;

    private LocalDateTime decisionTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    // ===== GETTERS =====

    public Long getDecisionId() { return decisionId; }

    public String getDecisionType() { return decisionType; }

    public String getDecisionSource() { return decisionSource; }

    public String getDecisionReason() { return decisionReason; }

    public String getDecisionStatus() { return decisionStatus; }

    public String getEvaluatedBy() { return evaluatedBy; }

    public Integer getVersionNumber() { return versionNumber; }

    public LocalDateTime getDecisionTime() { return decisionTime; }

    public Document getDocument() { return document; }

    // ===== SETTERS =====

    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }

    public void setDecisionSource(String decisionSource) { this.decisionSource = decisionSource; }

    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public void setDecisionStatus(String decisionStatus) { this.decisionStatus = decisionStatus; }

    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }

    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public void setDocument(Document document) { this.document = document; }
}
