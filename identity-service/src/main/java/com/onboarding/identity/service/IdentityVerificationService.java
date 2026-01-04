package com.onboarding.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IdentityVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(IdentityVerificationService.class);

    private final ErrorTriggerService errorTriggerService;

    public IdentityVerificationService(ErrorTriggerService errorTriggerService) {
        this.errorTriggerService = errorTriggerService;
    }

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> verifyIdentity(String requestId, Map<String, Object> kycData) {
        logger.info("Verifying identity for requestId: {}", requestId);

        // Check for error triggers
        String customerId = (String) kycData.get("customerId");
        if (customerId != null && errorTriggerService.shouldFail(customerId)) {
            logger.error("Identity verification failed for customer: {}", customerId);
            throw new RuntimeException("Identity verification failed: Biometric mismatch");
        }

        // Simulate identity verification (document validation)
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> identityData = new HashMap<>();
        identityData.put("verificationLevel", "LEVEL_2");
        identityData.put("biometricMatch", true);
        identityData.put("documentAuthenticity", "VERIFIED");
        identityData.put("livenessCheck", "PASSED");
        
        // Pass through customer data from KYC
        if (kycData != null) {
            identityData.putAll(kycData);
        }

        logger.info("Identity verification completed for requestId: {}", requestId);
        return identityData;
    }
}
