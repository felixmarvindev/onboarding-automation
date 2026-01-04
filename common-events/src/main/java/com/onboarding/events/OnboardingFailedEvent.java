package com.onboarding.events;

public class OnboardingFailedEvent extends BaseFailureEvent {
    private String failedStage;

    public OnboardingFailedEvent() {
        super();
    }

    public OnboardingFailedEvent(String requestId, String failedStage, String errorCode, String errorMessage, int retryCount) {
        super(requestId, errorCode, errorMessage, retryCount);
        this.failedStage = failedStage;
    }

    public String getFailedStage() {
        return failedStage;
    }

    public void setFailedStage(String failedStage) {
        this.failedStage = failedStage;
    }
}
