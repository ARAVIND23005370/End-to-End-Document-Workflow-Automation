package com.project.documentworkflow.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
