package com.e2edocs.dto;

public class SignupStatusResponse {

    private boolean available;

    public SignupStatusResponse() {
    }

    public SignupStatusResponse(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
