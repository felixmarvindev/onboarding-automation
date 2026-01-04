package com.onboarding.notification.listener;

import com.onboarding.events.AccountProvisionedEvent;
import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.NotificationSentEvent;
import com.onboarding.notification.config.RabbitMQConfig;
import com.onboarding.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;

    public NotificationEventListener(NotificationService notificationService, RabbitTemplate rabbitTemplate) {
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAccountProvisioned(AccountProvisionedEvent event) {
        logger.info("Received AccountProvisionedEvent for requestId: {}, accountId: {}",
                event.getRequestId(), event.getAccountId());

        try {
            String deliveryStatus = notificationService.sendNotification(
                    event.getRequestId(),
                    event.getAccountId(),
                    event.getAccountDetails()
            );

            NotificationSentEvent sentEvent = new NotificationSentEvent(
                    event.getRequestId(),
                    "EMAIL",
                    deliveryStatus
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.NOTIFICATION_SENT,
                    sentEvent
            );

            logger.info("Published NotificationSentEvent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Error sending notification for requestId: {}", event.getRequestId(), e);
            throw new RuntimeException("Notification sending failed", e);
        }
    }
}
