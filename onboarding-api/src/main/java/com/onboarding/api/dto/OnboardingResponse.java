package com.onboarding.api.dto;

import java.time.LocalDateTime;

public class OnboardingResponse {
    private String requestId;
    private String status;
    private LocalDateTime timestamp;

    public OnboardingResponse() {
    }

    public OnboardingResponse(String requestId, String status, LocalDateTime timestamp) {
        this.requestId = requestId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
