package com.onboarding.kyc.service;

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

    public boolean isBlacklisted(String customerId) {
        List<String> blacklist = errorTriggerProperties.getKyc().getBlacklistCustomerIds();
        if (blacklist.contains(customerId)) {
            logger.warn("Customer {} is blacklisted", customerId);
            return true;
        }

        String pattern = errorTriggerProperties.getKyc().getFailurePattern();
        if (pattern != null && !pattern.isEmpty() && customerId != null) {
            // Simple pattern matching (support * wildcard)
            String regex = pattern.replace("*", ".*");
            if (customerId.matches(regex)) {
                logger.warn("Customer {} matches failure pattern: {}", customerId, pattern);
                return true;
            }
        }

        return false;
    }
}
