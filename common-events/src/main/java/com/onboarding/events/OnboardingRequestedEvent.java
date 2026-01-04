package com.onboarding.events;

import java.util.Map;

public class OnboardingRequestedEvent extends BaseEvent {
    private String customerId;
    private Map<String, Object> customerData;

    public OnboardingRequestedEvent() {
        super();
    }

    public OnboardingRequestedEvent(String requestId, String customerId, Map<String, Object> customerData) {
        super(requestId);
        this.customerId = customerId;
        this.customerData = customerData;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<String, Object> getCustomerData() {
        return customerData;
    }

    public void setCustomerData(Map<String, Object> customerData) {
        this.customerData = customerData;
    }
}
