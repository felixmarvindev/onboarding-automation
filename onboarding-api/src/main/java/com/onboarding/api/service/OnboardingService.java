package com.onboarding.api.service;

import com.onboarding.api.config.RabbitMQConfig;
import com.onboarding.api.dto.OnboardingRequest;
import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.OnboardingFailedEvent;
import com.onboarding.events.OnboardingRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OnboardingService {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingService.class);

    private final RabbitTemplate rabbitTemplate;
    private final OnboardingValidationService validationService;

    public OnboardingService(RabbitTemplate rabbitTemplate, OnboardingValidationService validationService) {
        this.rabbitTemplate = rabbitTemplate;
        this.validationService = validationService;
    }

    public String initiateOnboarding(OnboardingRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Initiating onboarding request with ID: {}", requestId);

        try {
            // Validate business rules
            validationService.validateBusinessRules(request);

            Map<String, Object> customerData = new HashMap<>();
            customerData.put("name", request.getName());
            customerData.put("email", request.getEmail());
            customerData.put("documentType", request.getDocumentType());
            customerData.put("documentNumber", request.getDocumentNumber());

            OnboardingRequestedEvent event = new OnboardingRequestedEvent(requestId, request.getCustomerId(), customerData);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.ONBOARDING_REQUESTED,
                    event
            );

            logger.info("Published OnboardingRequestedEvent for requestId: {}", requestId);
            return requestId;
        } catch (com.onboarding.api.exception.BusinessRuleException e) {
            logger.warn("Business rule violation for requestId: {} - {}", requestId, e.getMessage());
            // Publish failure event for business rule violations
            OnboardingFailedEvent failedEvent = new OnboardingFailedEvent(
                    requestId,
                    "VALIDATION",
                    e.getErrorCode() != null ? e.getErrorCode() : "BUSINESS_RULE_VIOLATION",
                    e.getMessage(),
                    0
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.ONBOARDING_FAILED,
                    failedEvent
            );
            throw e; // Re-throw to return 400 to client
        }
    }
}
