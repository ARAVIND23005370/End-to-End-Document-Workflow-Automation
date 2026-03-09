package com.project.documentworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_data")
public class OCRData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ocr_data_id")
    private Long ocrDataId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "extracted_text", columnDefinition = "TEXT", nullable = false)
    private String extractedText;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "processed_at")
    private LocalDateTime processedAt = LocalDateTime.now();

    // ===== GETTERS =====

    public Long getOcrDataId() {
        return ocrDataId;
    }

    public Document getDocument() {
        return document;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    // ===== SETTERS =====

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
