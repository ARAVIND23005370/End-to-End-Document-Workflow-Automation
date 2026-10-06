package com.e2edocs.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchUploadResponse {
    private int totalFiles;
    private int successful;
    private int failed;
    private List<DocumentResponse> documents = new ArrayList<>();
    private Map<String, String> errors = new HashMap<>();

    public BatchUploadResponse() {
    }

    public BatchUploadResponse(int totalFiles, int successful, int failed, List<DocumentResponse> documents, Map<String, String> errors) {
        this.totalFiles = totalFiles;
        this.successful = successful;
        this.failed = failed;
        this.documents = documents != null ? documents : new ArrayList<>();
        this.errors = errors != null ? errors : new HashMap<>();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<DocumentResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentResponse> documents) {
        this.documents = documents;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
