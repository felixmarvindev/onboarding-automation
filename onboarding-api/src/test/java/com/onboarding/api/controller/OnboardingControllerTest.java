package com.onboarding.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.api.dto.OnboardingRequest;
import com.onboarding.api.service.OnboardingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnboardingService onboardingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRequestOnboarding() throws Exception {
        String requestId = UUID.randomUUID().toString();
        OnboardingRequest request = new OnboardingRequest();
        request.setCustomerId("CUST-123");
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setDocumentType("PASSPORT");
        request.setDocumentNumber("DOC123456");

        when(onboardingService.initiateOnboarding(any(OnboardingRequest.class))).thenReturn(requestId);

        mockMvc.perform(post("/api/onboarding/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    void testRequestOnboardingWithInvalidData() throws Exception {
        OnboardingRequest request = new OnboardingRequest();
        // Missing required fields

        mockMvc.perform(post("/api/onboarding/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
