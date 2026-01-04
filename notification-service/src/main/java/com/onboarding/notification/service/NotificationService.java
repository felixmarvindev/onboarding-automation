package com.onboarding.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String sendNotification(String requestId, String accountId, Map<String, Object> accountDetails) {
        logger.info("Sending notification for requestId: {}, accountId: {}", requestId, accountId);

        // Simulate notification sending (email/SMS)
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Notification sent successfully for requestId: {}", requestId);
        return "DELIVERED";
    }
}
