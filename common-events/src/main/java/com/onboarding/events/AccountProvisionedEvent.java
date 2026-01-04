package com.onboarding.events;

import java.util.Map;

public class AccountProvisionedEvent extends BaseEvent {
    private String accountId;
    private Map<String, Object> accountDetails;

    public AccountProvisionedEvent() {
        super();
    }

    public AccountProvisionedEvent(String requestId, String accountId, Map<String, Object> accountDetails) {
        super(requestId);
        this.accountId = accountId;
        this.accountDetails = accountDetails;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Map<String, Object> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(Map<String, Object> accountDetails) {
        this.accountDetails = accountDetails;
    }
}
