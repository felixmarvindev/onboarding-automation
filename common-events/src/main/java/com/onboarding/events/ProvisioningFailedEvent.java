package com.onboarding.events;

public class ProvisioningFailedEvent extends BaseFailureEvent {
    public ProvisioningFailedEvent() {
        super();
    }

    public ProvisioningFailedEvent(String requestId, String errorCode, String errorMessage, int retryCount) {
        super(requestId, errorCode, errorMessage, retryCount);
    }
}
