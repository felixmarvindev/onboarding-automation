package com.onboarding.completion.listener;

import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.NotificationSentEvent;
import com.onboarding.events.OnboardingCompletedEvent;
import com.onboarding.completion.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CompletionEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CompletionEventListener.class);

    private final RabbitTemplate rabbitTemplate;

    public CompletionEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleNotificationSent(NotificationSentEvent event) {
        logger.info("Received NotificationSentEvent for requestId: {}", event.getRequestId());

        try {
            OnboardingCompletedEvent completedEvent = new OnboardingCompletedEvent(
                    event.getRequestId(),
                    "COMPLETED"
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.ONBOARDING_COMPLETED,
                    completedEvent
            );

            logger.info("Published OnboardingCompletedEvent for requestId: {}. Onboarding marked complete.",
                    event.getRequestId());
        } catch (Exception e) {
            logger.error("Error completing onboarding for requestId: {}", event.getRequestId(), e);
            throw new RuntimeException("Onboarding completion failed", e);
        }
    }
}
