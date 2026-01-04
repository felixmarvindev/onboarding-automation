package com.onboarding.failure.service;

import com.onboarding.events.*;
import com.onboarding.failure.model.OnboardingFailure;
import com.onboarding.failure.repository.OnboardingFailureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailureHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(FailureHandlerService.class);

    private final OnboardingFailureRepository failureRepository;
    private final RabbitTemplate rabbitTemplate;

    public FailureHandlerService(OnboardingFailureRepository failureRepository, RabbitTemplate rabbitTemplate) {
        this.failureRepository = failureRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void handleKycFailure(KYCFailedEvent event) {
        logger.error("Handling KYC failure for requestId: {}, error: {}", event.getRequestId(), event.getErrorMessage());
        saveFailure(event.getRequestId(), "KYC", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
        publishOnboardingFailed(event.getRequestId(), "KYC", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
    }

    @Transactional
    public void handleIdentityFailure(IdentityVerificationFailedEvent event) {
        logger.error("Handling Identity verification failure for requestId: {}, error: {}", event.getRequestId(), event.getErrorMessage());
        saveFailure(event.getRequestId(), "IDENTITY", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
        publishOnboardingFailed(event.getRequestId(), "IDENTITY", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
    }

    @Transactional
    public void handleProvisioningFailure(ProvisioningFailedEvent event) {
        logger.error("Handling Provisioning failure for requestId: {}, error: {}", event.getRequestId(), event.getErrorMessage());
        saveFailure(event.getRequestId(), "PROVISIONING", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
        publishOnboardingFailed(event.getRequestId(), "PROVISIONING", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
    }

    @Transactional
    public void handleNotificationFailure(NotificationFailedEvent event) {
        logger.error("Handling Notification failure for requestId: {}, error: {}", event.getRequestId(), event.getErrorMessage());
        saveFailure(event.getRequestId(), "NOTIFICATION", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
        publishOnboardingFailed(event.getRequestId(), "NOTIFICATION", event.getErrorCode(), event.getErrorMessage(), event.getRetryCount());
    }

    private void saveFailure(String requestId, String stage, String errorCode, String errorMessage, int retryCount) {
        OnboardingFailure failure = new OnboardingFailure(requestId, stage, errorCode, errorMessage, retryCount);
        failureRepository.save(failure);
        logger.info("Saved failure record for requestId: {}, stage: {}", requestId, stage);
    }

    private void publishOnboardingFailed(String requestId, String failedStage, String errorCode, String errorMessage, int retryCount) {
        OnboardingFailedEvent failedEvent = new OnboardingFailedEvent(requestId, failedStage, errorCode, errorMessage, retryCount);
        rabbitTemplate.convertAndSend("onboarding.exchange", EventRoutingKeys.ONBOARDING_FAILED, failedEvent);
        logger.info("Published OnboardingFailedEvent for requestId: {}", requestId);
    }
}
