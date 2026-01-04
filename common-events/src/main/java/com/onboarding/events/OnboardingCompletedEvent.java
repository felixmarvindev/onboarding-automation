package com.onboarding.events;

public class OnboardingCompletedEvent extends BaseEvent {
    private String finalStatus;

    public OnboardingCompletedEvent() {
        super();
    }

    public OnboardingCompletedEvent(String requestId, String finalStatus) {
        super(requestId);
        this.finalStatus = finalStatus;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}
