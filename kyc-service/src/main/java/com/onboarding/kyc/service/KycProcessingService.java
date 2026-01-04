package com.onboarding.kyc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KycProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(KycProcessingService.class);

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> processKyc(String customerId, Map<String, Object> customerData) {
        logger.info("Processing KYC for customer: {}", customerId);

        // Simulate external KYC service call with delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> kycData = new HashMap<>();
        kycData.put("customerId", customerId);
        kycData.put("kycLevel", "BASIC");
        kycData.put("status", "APPROVED");
        kycData.put("documentVerified", true);

        logger.info("KYC processing completed for customer: {}", customerId);
        return kycData;
    }
}
