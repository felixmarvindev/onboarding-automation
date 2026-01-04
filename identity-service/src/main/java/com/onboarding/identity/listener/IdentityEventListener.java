package com.onboarding.identity.listener;

import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.IdentityVerifiedEvent;
import com.onboarding.events.KYCCompletedEvent;
import com.onboarding.identity.config.RabbitMQConfig;
import com.onboarding.identity.service.IdentityVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdentityEventListener {
    private static final Logger logger = LoggerFactory.getLogger(IdentityEventListener.class);

    private final IdentityVerificationService identityVerificationService;
    private final RabbitTemplate rabbitTemplate;

    public IdentityEventListener(IdentityVerificationService identityVerificationService, RabbitTemplate rabbitTemplate) {
        this.identityVerificationService = identityVerificationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleKycCompleted(KYCCompletedEvent event) {
        logger.info("Received KYCCompletedEvent for requestId: {}", event.getRequestId());

        try {
            Map<String, Object> identityData = identityVerificationService.verifyIdentity(
                    event.getRequestId(),
                    event.getKycData()
            );

            IdentityVerifiedEvent verifiedEvent = new IdentityVerifiedEvent(
                    event.getRequestId(),
                    "VERIFIED",
                    identityData
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.IDENTITY_VERIFIED,
                    verifiedEvent
            );

            logger.info("Published IdentityVerifiedEvent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Error verifying identity for requestId: {}", event.getRequestId(), e);
            throw new RuntimeException("Identity verification failed", e);
        }
    }
}
