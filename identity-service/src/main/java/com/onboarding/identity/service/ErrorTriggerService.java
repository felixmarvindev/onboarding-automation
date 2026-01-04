package com.onboarding.identity.service;

import com.onboarding.events.ErrorTriggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ErrorTriggerService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorTriggerService.class);

    private final ErrorTriggerProperties errorTriggerProperties;

    public ErrorTriggerService(ErrorTriggerProperties errorTriggerProperties) {
        this.errorTriggerProperties = errorTriggerProperties;
    }

    public boolean shouldFail(String customerId) {
        List<String> failureIds = errorTriggerProperties.getIdentity().getFailureCustomerIds();
        if (failureIds.contains(customerId)) {
            logger.warn("Identity verification failure triggered for customer: {}", customerId);
            return true;
        }

        String pattern = errorTriggerProperties.getIdentity().getFailurePattern();
        if (pattern != null && !pattern.isEmpty() && customerId != null) {
            String regex = pattern.replace("*", ".*");
            if (customerId.matches(regex)) {
                logger.warn("Customer {} matches identity failure pattern: {}", customerId, pattern);
                return true;
            }
        }

        return false;
    }
}
