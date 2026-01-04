package com.onboarding.kyc.listener;

import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.KYCCompletedEvent;
import com.onboarding.events.KYCFailedEvent;
import com.onboarding.events.OnboardingRequestedEvent;
import com.onboarding.kyc.config.RabbitMQConfig;
import com.onboarding.kyc.service.KycProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KycEventListener {
    private static final Logger logger = LoggerFactory.getLogger(KycEventListener.class);

    private final KycProcessingService kycProcessingService;
    private final RabbitTemplate rabbitTemplate;

    public KycEventListener(KycProcessingService kycProcessingService, RabbitTemplate rabbitTemplate) {
        this.kycProcessingService = kycProcessingService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOnboardingRequested(OnboardingRequestedEvent event) {
        logger.info("Received OnboardingRequestedEvent for requestId: {}, customerId: {}",
                event.getRequestId(), event.getCustomerId());

        try {
            Map<String, Object> kycData = kycProcessingService.processKyc(
                    event.getCustomerId(),
                    event.getCustomerData()
            );

            KYCCompletedEvent completedEvent = new KYCCompletedEvent(
                    event.getRequestId(),
                    "COMPLETED",
                    kycData
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.KYC_COMPLETED,
                    completedEvent
            );

            logger.info("Published KYCCompletedEvent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("KYC processing failed after retries for requestId: {}", event.getRequestId(), e);
            // Publish failure event (will be sent to DLQ if listener fails)
            // Failure Handler will process from DLQ
            throw new RuntimeException("KYC processing failed", e);
        }
    }
}
