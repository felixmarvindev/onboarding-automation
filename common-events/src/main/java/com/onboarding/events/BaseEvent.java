package com.onboarding.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class BaseEvent implements Serializable {
    private String requestId;
    private LocalDateTime timestamp;

    protected BaseEvent() {
        this.timestamp = LocalDateTime.now();
    }

    protected BaseEvent(String requestId) {
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
