package com.onboarding.events;

import java.util.Map;

public class IdentityVerifiedEvent extends BaseEvent {
    private String verificationStatus;
    private Map<String, Object> identityData;

    public IdentityVerifiedEvent() {
        super();
    }

    public IdentityVerifiedEvent(String requestId, String verificationStatus, Map<String, Object> identityData) {
        super(requestId);
        this.verificationStatus = verificationStatus;
        this.identityData = identityData;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Map<String, Object> getIdentityData() {
        return identityData;
    }

    public void setIdentityData(Map<String, Object> identityData) {
        this.identityData = identityData;
    }
}
