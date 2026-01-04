package com.onboarding.events;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "error-triggers")
public class ErrorTriggerProperties {
    private KycTriggers kyc = new KycTriggers();
    private IdentityTriggers identity = new IdentityTriggers();
    private ProvisioningTriggers provisioning = new ProvisioningTriggers();
    private NotificationTriggers notification = new NotificationTriggers();

    public KycTriggers getKyc() {
        return kyc;
    }

    public void setKyc(KycTriggers kyc) {
        this.kyc = kyc;
    }

    public IdentityTriggers getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityTriggers identity) {
        this.identity = identity;
    }

    public ProvisioningTriggers getProvisioning() {
        return provisioning;
    }

    public void setProvisioning(ProvisioningTriggers provisioning) {
        this.provisioning = provisioning;
    }

    public NotificationTriggers getNotification() {
        return notification;
    }

    public void setNotification(NotificationTriggers notification) {
        this.notification = notification;
    }

    public static class KycTriggers {
        private List<String> blacklistCustomerIds = new ArrayList<>();
        private String failurePattern = "";

        public List<String> getBlacklistCustomerIds() {
            return blacklistCustomerIds;
        }

        public void setBlacklistCustomerIds(List<String> blacklistCustomerIds) {
            this.blacklistCustomerIds = blacklistCustomerIds;
        }

        public String getFailurePattern() {
            return failurePattern;
        }

        public void setFailurePattern(String failurePattern) {
            this.failurePattern = failurePattern;
        }
    }

    public static class IdentityTriggers {
        private List<String> failureCustomerIds = new ArrayList<>();
        private String failurePattern = "";

        public List<String> getFailureCustomerIds() {
            return failureCustomerIds;
        }

        public void setFailureCustomerIds(List<String> failureCustomerIds) {
            this.failureCustomerIds = failureCustomerIds;
        }

        public String getFailurePattern() {
            return failurePattern;
        }

        public void setFailurePattern(String failurePattern) {
            this.failurePattern = failurePattern;
        }
    }

    public static class ProvisioningTriggers {
        private List<String> failureCustomerIds = new ArrayList<>();
        private String failurePattern = "";

        public List<String> getFailureCustomerIds() {
            return failureCustomerIds;
        }

        public void setFailureCustomerIds(List<String> failureCustomerIds) {
            this.failureCustomerIds = failureCustomerIds;
        }

        public String getFailurePattern() {
            return failurePattern;
        }

        public void setFailurePattern(String failurePattern) {
            this.failurePattern = failurePattern;
        }
    }

    public static class NotificationTriggers {
        private List<String> bounceEmails = new ArrayList<>();
        private String failurePattern = "";

        public List<String> getBounceEmails() {
            return bounceEmails;
        }

        public void setBounceEmails(List<String> bounceEmails) {
            this.bounceEmails = bounceEmails;
        }

        public String getFailurePattern() {
            return failurePattern;
        }

        public void setFailurePattern(String failurePattern) {
            this.failurePattern = failurePattern;
        }
    }
}
