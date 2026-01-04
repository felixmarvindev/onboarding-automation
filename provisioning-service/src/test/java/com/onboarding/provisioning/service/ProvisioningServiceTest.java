package com.onboarding.provisioning.service;

import com.onboarding.provisioning.model.Account;
import com.onboarding.provisioning.model.AccountStatus;
import com.onboarding.provisioning.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ProvisioningService provisioningService;

    @Test
    void testProvisionAccount() {
        String requestId = "REQ-123";
        String customerId = "CUST-123";
        Map<String, Object> identityData = new HashMap<>();

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = provisioningService.provisionAccount(requestId, customerId, identityData);

        assertNotNull(result);
        assertNotNull(result.get("accountId"));
        assertEquals(customerId, result.get("customerId"));
        assertEquals("ACTIVE", result.get("status"));
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
