package com.e2edocs.service.extraction;

import java.util.HashMap;
import java.util.Map;

public class DocumentExtractionResult {
    private String extractedText;
    private String contentPreview;
    private String extractorUsed;
    private boolean ocrUsed;
    private int pageCount;
    private long characterCount;
    private long wordCount;
    private Map<String, Object> metadata = new HashMap<>();

    public DocumentExtractionResult() {
    }

    public DocumentExtractionResult(String extractedText, String extractorUsed, boolean ocrUsed, int pageCount) {
        this.extractedText = extractedText != null ? extractedText : "";
        this.extractorUsed = extractorUsed;
        this.ocrUsed = ocrUsed;
        this.pageCount = pageCount;
        this.characterCount = this.extractedText.length();
        this.wordCount = this.extractedText.isBlank() ? 0 : this.extractedText.trim().split("\\s+").length;
        this.contentPreview = generatePreview(this.extractedText, 500);
    }

    private static String generatePreview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "No text content preview available.";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength) + "...";
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
        if (extractedText != null) {
            this.characterCount = extractedText.length();
            this.wordCount = extractedText.isBlank() ? 0 : extractedText.trim().split("\\s+").length;
            this.contentPreview = generatePreview(extractedText, 500);
        }
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getExtractorUsed() {
        return extractorUsed;
    }

    public void setExtractorUsed(String extractorUsed) {
        this.extractorUsed = extractorUsed;
    }

    public boolean isOcrUsed() {
        return ocrUsed;
    }

    public void setOcrUsed(boolean ocrUsed) {
        this.ocrUsed = ocrUsed;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public long getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(long characterCount) {
        this.characterCount = characterCount;
    }

    public long getWordCount() {
        return wordCount;
    }

    public void setWordCount(long wordCount) {
        this.wordCount = wordCount;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
