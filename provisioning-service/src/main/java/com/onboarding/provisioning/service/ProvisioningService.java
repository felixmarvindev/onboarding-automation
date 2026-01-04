package com.onboarding.provisioning.service;

import com.onboarding.provisioning.model.Account;
import com.onboarding.provisioning.model.AccountStatus;
import com.onboarding.provisioning.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProvisioningService {
    private static final Logger logger = LoggerFactory.getLogger(ProvisioningService.class);

    private final AccountRepository accountRepository;

    public ProvisioningService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private final ErrorTriggerService errorTriggerService;

    public ProvisioningService(AccountRepository accountRepository, ErrorTriggerService errorTriggerService) {
        this.accountRepository = accountRepository;
        this.errorTriggerService = errorTriggerService;
    }

    @Transactional
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> provisionAccount(String requestId, String customerId, Map<String, Object> identityData) {
        logger.info("Provisioning account for requestId: {}, customerId: {}", requestId, customerId);

        // Check for error triggers
        if (errorTriggerService.shouldFail(customerId)) {
            logger.error("Provisioning failed: Database constraint violation for customer: {}", customerId);
            throw new RuntimeException("Account provisioning failed: Database constraint violation");
        }

        // Simulate account provisioning
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String accountId = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Account account = new Account(requestId, customerId, accountId, AccountStatus.ACTIVE);
        accountRepository.save(account);

        logger.info("Account created: {} for requestId: {}", accountId, requestId);

        Map<String, Object> accountDetails = new HashMap<>();
        accountDetails.put("accountId", accountId);
        accountDetails.put("customerId", customerId);
        accountDetails.put("status", "ACTIVE");
        accountDetails.put("accountType", "STANDARD");
        
        // Pass through customer data from identity verification
        if (identityData != null) {
            accountDetails.put("customerEmail", identityData.get("email"));
            accountDetails.put("customerName", identityData.get("name"));
        }

        return accountDetails;
    }
}
