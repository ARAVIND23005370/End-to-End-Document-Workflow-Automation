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

    private LocalDateTime decisionTime;

    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    // ✅ REQUIRED SETTERS

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public void setDecisionSource(String decisionSource) {
        this.decisionSource = decisionSource;
    }

    public void setDecisionTime(LocalDateTime decisionTime) {
        this.decisionTime = decisionTime;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    // (Getters optional for now)
}
