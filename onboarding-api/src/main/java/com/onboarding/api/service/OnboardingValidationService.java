package com.onboarding.api.service;

import com.onboarding.api.dto.OnboardingRequest;
import com.onboarding.api.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class OnboardingValidationService {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingValidationService.class);

    private static final List<String> SUPPORTED_DOCUMENT_TYPES = Arrays.asList("PASSPORT", "ID_CARD");
    private static final String PASSPORT_NUMBER_PATTERN = "^[A-Z0-9]{6,12}$";

    public void validateBusinessRules(OnboardingRequest request) {
        logger.debug("Validating business rules for customer: {}", request.getCustomerId());

        // Validate document type
        if (!SUPPORTED_DOCUMENT_TYPES.contains(request.getDocumentType())) {
            throw new BusinessRuleException("UNSUPPORTED_DOCUMENT_TYPE",
                    "Document type '" + request.getDocumentType() + "' is not supported. Supported types: " + SUPPORTED_DOCUMENT_TYPES);
        }

        // Validate document number format for passport
        if ("PASSPORT".equals(request.getDocumentType())) {
            if (!request.getDocumentNumber().matches(PASSPORT_NUMBER_PATTERN)) {
                throw new BusinessRuleException("INVALID_DOCUMENT_NUMBER_FORMAT",
                        "Passport number format is invalid. Expected format: 6-12 alphanumeric characters");
            }
        }
    }
}
