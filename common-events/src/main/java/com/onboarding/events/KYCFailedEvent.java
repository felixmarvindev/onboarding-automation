package com.onboarding.events;

public class KYCFailedEvent extends BaseFailureEvent {
    public KYCFailedEvent() {
        super();
    }

    public KYCFailedEvent(String requestId, String errorCode, String errorMessage, int retryCount) {
        super(requestId, errorCode, errorMessage, retryCount);
    }
}
