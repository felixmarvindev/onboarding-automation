package com.onboarding.failure.listener;

import com.onboarding.events.*;
import com.onboarding.failure.config.RabbitMQConfig;
import com.onboarding.failure.service.FailureHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DlqEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DlqEventListener.class);

    private final FailureHandlerService failureHandlerService;
    private final RabbitTemplate rabbitTemplate;

    public DlqEventListener(FailureHandlerService failureHandlerService, RabbitTemplate rabbitTemplate) {
        this.failureHandlerService = failureHandlerService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "kyc.dlq")
    public void handleKycDlq(Message message) {
        logger.warn("Received message from KYC DLQ");
        processDlqMessage(message, "KYC");
    }

    @RabbitListener(queues = "identity.dlq")
    public void handleIdentityDlq(Message message) {
        logger.warn("Received message from Identity DLQ");
        processDlqMessage(message, "IDENTITY");
    }

    @RabbitListener(queues = "provisioning.dlq")
    public void handleProvisioningDlq(Message message) {
        logger.warn("Received message from Provisioning DLQ");
        processDlqMessage(message, "PROVISIONING");
    }

    @RabbitListener(queues = "notification.dlq")
    public void handleNotificationDlq(Message message) {
        logger.warn("Received message from Notification DLQ");
        processDlqMessage(message, "NOTIFICATION");
    }

    private void processDlqMessage(Message message, String serviceName) {
        try {
            // Extract original message and routing key
            String originalRoutingKey = (String) message.getMessageProperties().getHeaders().get("x-original-routing-key");
            String requestId = extractRequestId(message);

            logger.error("Processing failed message from {} service DLQ. RequestId: {}, OriginalRoutingKey: {}",
                    serviceName, requestId, originalRoutingKey);

            // Record failure in database
            failureHandlerService.recordFailure(requestId, serviceName, "Processing failed after retries", 3);

            // Publish appropriate failure event
            BaseFailureEvent failureEvent = createFailureEvent(serviceName, requestId, originalRoutingKey);
            if (failureEvent != null) {
                String routingKey = getFailureRoutingKey(serviceName);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, failureEvent);
                logger.info("Published {} failure event for requestId: {}", serviceName, requestId);
            }
        } catch (Exception e) {
            logger.error("Error processing DLQ message from {} service", serviceName, e);
        }
    }

    private String extractRequestId(Message message) {
        try {
            // Try to deserialize the message body to extract requestId
            Object payload = rabbitTemplate.getMessageConverter().fromMessage(message);
            
            // Check if it's a BaseEvent or event with requestId field
            if (payload != null) {
                try {
                    // Use reflection to get requestId from event object
                    java.lang.reflect.Method getRequestIdMethod = payload.getClass().getMethod("getRequestId");
                    Object requestId = getRequestIdMethod.invoke(payload);
                    if (requestId != null) {
                        return requestId.toString();
                    }
                } catch (Exception e) {
                    // Fallback to Map extraction
                    if (payload instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) payload;
                        Object requestId = map.get("requestId");
                        if (requestId != null) {
                            return requestId.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract requestId from message", e);
        }
        return "UNKNOWN";
    }

    private BaseFailureEvent createFailureEvent(String serviceName, String requestId, String originalRoutingKey) {
        switch (serviceName) {
            case "KYC":
                return new KYCFailedEvent(requestId, "KYC_FAILED", "KYC processing failed after retries", 3);
            case "IDENTITY":
                return new IdentityVerificationFailedEvent(requestId, "IDENTITY_VERIFICATION_FAILED", "Identity verification failed after retries", 3);
            case "PROVISIONING":
                return new ProvisioningFailedEvent(requestId, "PROVISIONING_FAILED", "Account provisioning failed after retries", 3);
            case "NOTIFICATION":
                return new NotificationFailedEvent(requestId, "NOTIFICATION_FAILED", "Notification sending failed after retries", 3);
            default:
                return null;
        }
    }

    private String getFailureRoutingKey(String serviceName) {
        switch (serviceName) {
            case "KYC":
                return EventRoutingKeys.KYC_FAILED;
            case "IDENTITY":
                return EventRoutingKeys.IDENTITY_FAILED;
            case "PROVISIONING":
                return EventRoutingKeys.PROVISIONING_FAILED;
            case "NOTIFICATION":
                return EventRoutingKeys.NOTIFICATION_FAILED;
            default:
                return EventRoutingKeys.ONBOARDING_FAILED;
        }
    }
}
