package com.onboarding.kyc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KycProcessingServiceTest {

    private KycProcessingService kycProcessingService;

    @BeforeEach
    void setUp() {
        kycProcessingService = new KycProcessingService();
    }

    @Test
    void testProcessKyc() {
        String customerId = "CUST-123";
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("name", "Test User");
        customerData.put("email", "test@example.com");

        Map<String, Object> result = kycProcessingService.processKyc(customerId, customerData);

        assertNotNull(result);
        assertEquals(customerId, result.get("customerId"));
        assertEquals("BASIC", result.get("kycLevel"));
        assertEquals("APPROVED", result.get("status"));
        assertTrue((Boolean) result.get("documentVerified"));
    }
}
