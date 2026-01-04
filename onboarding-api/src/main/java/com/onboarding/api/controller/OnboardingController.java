package com.onboarding.api.controller;

import com.onboarding.api.dto.OnboardingRequest;
import com.onboarding.api.dto.OnboardingResponse;
import com.onboarding.api.service.OnboardingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingController.class);

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/request")
    public ResponseEntity<OnboardingResponse> requestOnboarding(@Valid @RequestBody OnboardingRequest request) {
        logger.info("Received onboarding request for customer: {}", request.getCustomerId());

        String requestId = onboardingService.initiateOnboarding(request);

        OnboardingResponse response = new OnboardingResponse(
                requestId,
                "INITIATED",
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}
