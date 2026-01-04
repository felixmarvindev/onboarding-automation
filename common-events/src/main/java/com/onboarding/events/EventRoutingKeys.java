package com.onboarding.events;

public class EventRoutingKeys {
    public static final String ONBOARDING_REQUESTED = "onboarding.requested";
    public static final String KYC_COMPLETED = "kyc.completed";
    public static final String IDENTITY_VERIFIED = "identity.verified";
    public static final String ACCOUNT_PROVISIONED = "account.provisioned";
    public static final String NOTIFICATION_SENT = "notification.sent";
    public static final String ONBOARDING_COMPLETED = "onboarding.completed";

    // Failure event routing keys
    public static final String KYC_FAILED = "kyc.failed";
    public static final String IDENTITY_FAILED = "identity.failed";
    public static final String PROVISIONING_FAILED = "provisioning.failed";
    public static final String NOTIFICATION_FAILED = "notification.failed";
    public static final String ONBOARDING_FAILED = "onboarding.failed";

    private EventRoutingKeys() {
        // Utility class
    }
}
