package com.onboarding.failure.listener;

import com.onboarding.events.*;
import com.onboarding.failure.config.RabbitMQConfig;
import com.onboarding.failure.service.FailureHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FailureEventListener {
    private static final Logger logger = LoggerFactory.getLogger(FailureEventListener.class);

    private final FailureHandlerService failureHandlerService;

    public FailureEventListener(FailureHandlerService failureHandlerService) {
        this.failureHandlerService = failureHandlerService;
    }

    @RabbitListener(queues = "kyc.failed.queue")
    public void handleKycFailure(KYCFailedEvent event) {
        logger.info("Received KYCFailedEvent for requestId: {}", event.getRequestId());
        failureHandlerService.handleKycFailure(event);
    }

    @RabbitListener(queues = "identity.failed.queue")
    public void handleIdentityFailure(IdentityVerificationFailedEvent event) {
        logger.info("Received IdentityVerificationFailedEvent for requestId: {}", event.getRequestId());
        failureHandlerService.handleIdentityFailure(event);
    }

    @RabbitListener(queues = "provisioning.failed.queue")
    public void handleProvisioningFailure(ProvisioningFailedEvent event) {
        logger.info("Received ProvisioningFailedEvent for requestId: {}", event.getRequestId());
        failureHandlerService.handleProvisioningFailure(event);
    }

    @RabbitListener(queues = "notification.failed.queue")
    public void handleNotificationFailure(NotificationFailedEvent event) {
        logger.info("Received NotificationFailedEvent for requestId: {}", event.getRequestId());
        failureHandlerService.handleNotificationFailure(event);
    }
}
