package com.e2edocs.dto;

public class DetectedInfoItem {
    private String label;
    private String value;
    private Double confidence;

    public DetectedInfoItem() {
    }

    public DetectedInfoItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public DetectedInfoItem(String label, String value, Double confidence) {
        this.label = label;
        this.value = value;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
