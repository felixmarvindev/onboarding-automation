package com.onboarding.notification.service;

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

    public boolean shouldFail(String email) {
        List<String> bounceEmails = errorTriggerProperties.getNotification().getBounceEmails();
        if (bounceEmails.contains(email)) {
            logger.warn("Notification failure triggered for email: {}", email);
            return true;
        }

        String pattern = errorTriggerProperties.getNotification().getFailurePattern();
        if (pattern != null && !pattern.isEmpty() && email != null) {
            String regex = pattern.replace("*", ".*");
            if (email.matches(regex)) {
                logger.warn("Email {} matches notification failure pattern: {}", email, pattern);
                return true;
            }
        }

        return false;
    }
}
