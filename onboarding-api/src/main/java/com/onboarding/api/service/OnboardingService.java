package com.onboarding.api.service;

import com.onboarding.api.config.RabbitMQConfig;
import com.onboarding.api.dto.OnboardingRequest;
import com.onboarding.events.EventRoutingKeys;
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

    public OnboardingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String initiateOnboarding(OnboardingRequest request) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Initiating onboarding request with ID: {}", requestId);

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
    }
}
