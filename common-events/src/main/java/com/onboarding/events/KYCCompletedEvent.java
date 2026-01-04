package com.onboarding.events;

import java.util.Map;

public class KYCCompletedEvent extends BaseEvent {
    private String kycStatus;
    private Map<String, Object> kycData;

    public KYCCompletedEvent() {
        super();
    }

    public KYCCompletedEvent(String requestId, String kycStatus, Map<String, Object> kycData) {
        super(requestId);
        this.kycStatus = kycStatus;
        this.kycData = kycData;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public Map<String, Object> getKycData() {
        return kycData;
    }

    public void setKycData(Map<String, Object> kycData) {
        this.kycData = kycData;
    }
}
