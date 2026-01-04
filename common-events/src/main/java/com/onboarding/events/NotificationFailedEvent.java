package com.onboarding.events;

public class NotificationFailedEvent extends BaseFailureEvent {
    public NotificationFailedEvent() {
        super();
    }

    public NotificationFailedEvent(String requestId, String errorCode, String errorMessage, int retryCount) {
        super(requestId, errorCode, errorMessage, retryCount);
    }
}
