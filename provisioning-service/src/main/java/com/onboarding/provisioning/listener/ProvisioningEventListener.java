package com.onboarding.provisioning.listener;

import com.onboarding.events.AccountProvisionedEvent;
import com.onboarding.events.EventRoutingKeys;
import com.onboarding.events.IdentityVerifiedEvent;
import com.onboarding.provisioning.config.RabbitMQConfig;
import com.onboarding.provisioning.service.ProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProvisioningEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ProvisioningEventListener.class);

    private final ProvisioningService provisioningService;
    private final RabbitTemplate rabbitTemplate;

    public ProvisioningEventListener(ProvisioningService provisioningService, RabbitTemplate rabbitTemplate) {
        this.provisioningService = provisioningService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleIdentityVerified(IdentityVerifiedEvent event) {
        logger.info("Received IdentityVerifiedEvent for requestId: {}", event.getRequestId());

        try {
            String customerId = extractCustomerId(event.getRequestId(), event.getIdentityData());

            Map<String, Object> accountDetails = provisioningService.provisionAccount(
                    event.getRequestId(),
                    customerId,
                    event.getIdentityData()
            );

            AccountProvisionedEvent provisionedEvent = new AccountProvisionedEvent(
                    event.getRequestId(),
                    (String) accountDetails.get("accountId"),
                    accountDetails
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    EventRoutingKeys.ACCOUNT_PROVISIONED,
                    provisionedEvent
            );

            logger.info("Published AccountProvisionedEvent for requestId: {}", event.getRequestId());
        } catch (Exception e) {
            logger.error("Error provisioning account for requestId: {}", event.getRequestId(), e);
            throw new RuntimeException("Account provisioning failed", e);
        }
    }

    private String extractCustomerId(String requestId, Map<String, Object> identityData) {
        // Extract customer ID from identity data (it flows through from the original request)
        if (identityData != null && identityData.containsKey("customerId")) {
            return (String) identityData.get("customerId");
        }
        // Fallback: generate from requestId if not found
        return "CUST-" + requestId.substring(0, 8);
    }
}
