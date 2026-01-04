package com.onboarding.events;

public class IdentityVerificationFailedEvent extends BaseFailureEvent {
    public IdentityVerificationFailedEvent() {
        super();
    }

    public IdentityVerificationFailedEvent(String requestId, String errorCode, String errorMessage, int retryCount) {
        super(requestId, errorCode, errorMessage, retryCount);
    }
}
