package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ocr_data")
public class OCRData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocrDataId;

    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private Double confidenceScore;

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
