package com.onboarding.notification.listener;

import com.onboarding.events.AccountProvisionedEvent;
import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.KYCCompletedEvent;
import com.onboarding.events.NotificationSentEvent;
import com.onboarding.events.OnboardingRequestedEvent;
import com.onboarding.notification.config.RabbitMQConfig;
import com.onboarding.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;

    public NotificationEventListener(NotificationService notificationService, RabbitTemplate rabbitTemplate) {
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "kyc.initiated.notification.queue")
    public void handleOnboardingRequested(OnboardingRequestedEvent event) {
        logger.info("Received OnboardingRequestedEvent for requestId: {}, customerId: {}",
                event.getRequestId(), event.getCustomerId());

        try {
            Map<String, Object> notificationData = new HashMap<>(event.getCustomerData());
            notificationData.put("customerId", event.getCustomerId());
            notificationData.put("customerEmail", event.getCustomerData().get("email"));
            notificationData.put("customerName", event.getCustomerData().get("name"));

            notificationService.sendNotification(
                    event.getRequestId(),
                    null, // No account ID yet
                    notificationData,
                    com.onboarding.notification.service.EmailType.KYC_INITIATED
            );

            logger.info("KYC initiated email sent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Error sending KYC initiated notification for requestId: {}", event.getRequestId(), e);
            // Don't throw - this is a notification, not critical to the workflow
        }
    }

    @RabbitListener(queues = "kyc.successful.notification.queue")
    public void handleKycCompleted(KYCCompletedEvent event) {
        logger.info("Received KYCCompletedEvent for requestId: {}", event.getRequestId());

        try {
            Map<String, Object> notificationData = new HashMap<>(event.getKycData());
            notificationData.put("customerEmail", event.getKycData().get("email"));
            notificationData.put("customerName", event.getKycData().get("name"));

            notificationService.sendNotification(
                    event.getRequestId(),
                    null, // No account ID yet
                    notificationData,
                    com.onboarding.notification.service.EmailType.KYC_SUCCESSFUL
            );

            logger.info("KYC successful email sent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Error sending KYC successful notification for requestId: {}", event.getRequestId(), e);
            // Don't throw - this is a notification, not critical to the workflow
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAccountProvisioned(AccountProvisionedEvent event) {
        logger.info("Received AccountProvisionedEvent for requestId: {}, accountId: {}",
                event.getRequestId(), event.getAccountId());

        try {
            // Extract customer email from account details or event
            String customerEmail = extractCustomerEmail(event.getAccountDetails());
            Map<String, Object> enhancedDetails = new HashMap<>(event.getAccountDetails());
            enhancedDetails.put("customerEmail", customerEmail);
            enhancedDetails.put("customerName", extractCustomerName(event.getAccountDetails()));

            String deliveryStatus = notificationService.sendNotification(
                    event.getRequestId(),
                    event.getAccountId(),
                    enhancedDetails,
                    com.onboarding.notification.service.EmailType.ACCOUNT_CREATED
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

    private String extractCustomerEmail(Map<String, Object> accountDetails) {
        // Try to get email from account details (it flows through from the original request)
        if (accountDetails != null && accountDetails.containsKey("customerEmail")) {
            return (String) accountDetails.get("customerEmail");
        }
        // Fallback: generate a test email (for development)
        String customerId = (String) accountDetails.getOrDefault("customerId", "unknown");
        return customerId + "@example.com";
    }

    private String extractCustomerName(Map<String, Object> accountDetails) {
        // Try to get name from account details
        if (accountDetails != null && accountDetails.containsKey("customerName")) {
            return (String) accountDetails.get("customerName");
        }
        return "Customer";
    }
}
