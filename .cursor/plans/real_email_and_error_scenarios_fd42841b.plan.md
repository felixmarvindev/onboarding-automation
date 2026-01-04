---
name: Real Email and Error Scenarios
overview: Add real SMTP email sending functionality and implement comprehensive error scenarios with custom handling strategies per error type. This will make the system more realistic and demonstrate proper error handling.
todos:
  - id: phase1-email-config
    content: Add Spring Mail dependency and configure SMTP settings in notification-service
    status: completed
  - id: phase1-email-service
    content: Implement real SMTP email sending in NotificationService with email templates
    status: completed
    dependencies:
      - phase1-email-config
  - id: phase2-error-triggers
    content: Add error trigger patterns and logic to each service for specific payload scenarios
    status: completed
  - id: phase2-error-classification
    content: Implement error classification (validation, business rules, service errors) with custom handling
    status: completed
    dependencies:
      - phase2-error-triggers
  - id: phase2-api-validation
    content: Enhance API validation to catch validation errors and business rule violations immediately
    status: completed
    dependencies:
      - phase2-error-classification
  - id: phase3-dlq-processing
    content: Implement DLQ message processing in Failure Handler service
    status: completed
    dependencies:
      - phase2-error-triggers
  - id: phase3-failure-tracking
    content: Enhance failure tracking to store all failures with proper error details
    status: completed
    dependencies:
      - phase3-dlq-processing
  - id: testing-error-scenarios
    content: Test all error scenarios with specific payloads and verify handling strategies
    status: pending
    dependencies:
      - phase3-failure-tracking
---

# Real Email and Error Scenarios Enhancement Plan

## Overview

This plan adds real email sending functionality via SMTP and implements comprehensive error scenarios with custom error handling strategies. The system will demonstrate proper error handling for different types of failures.

## Phase 1: Real Email Functionality (SMTP)

### Components to Implement

1. **Email Configuration** (`notification-service`)

   - Add Spring Mail dependency (`spring-boot-starter-mail`)
   - Configure SMTP settings in `application.yml`
   - Support for Gmail, Outlook, or custom SMTP servers
   - Environment variables for sensitive credentials

2. **Email Service Enhancement**

   - Replace mock notification with real SMTP email sending
   - Use JavaMailSender from Spring Mail
   - Create email templates (HTML/text)
   - Include account details in email
   - Error handling for email failures

3. **Configuration**

   - SMTP host, port, username, password
   - Email templates (welcome email with account details)
   - Configurable sender email and name
   - Support for local development (no real emails needed)

### Success Criteria

- Emails are sent via SMTP when account is provisioned
- Email includes account ID and customer details
- Email failures are handled gracefully
- Configuration is externalized (not hardcoded)

---

## Phase 2: Error Scenarios with Specific Payloads

### Error Types and Trigger Payloads

1. **Validation Errors** (Immediate Failure - No Retry)

   - Invalid email format: `"email": "not-an-email"`
   - Missing required fields: Omit `customerId`, `name`, etc.
   - Invalid document type: `"documentType": "INVALID_TYPE"`
   - **API Response**: 400 Bad Request with validation errors

2. **Business Rule Violations** (Immediate Failure - No Retry)

   - Unsupported document type: `"documentType": "DRIVERS_LICENSE"` (if only PASSPORT supported)
   - Document number format validation: Invalid format for passport
   - Age restrictions: Add `birthDate` field, reject if under 18
   - **API Response**: 400 Bad Request with business rule violations
   - **Event**: Publish `OnboardingFailedEvent` immediately

3. **KYC Service Failures** (Retry 3 times, then DLQ → Failure Handler)

   - Blacklist match: Specific customerId triggers KYC failure
   - Document verification fails: Specific document number triggers failure
   - External service timeout: Simulate KYC service unavailable
   - **Payload Trigger**: `"customerId": "BLACKLIST-001"` or specific pattern
   - **Behavior**: Retry 3 times, then DLQ, Failure Handler processes

4. **Identity Verification Failures** (Retry 3 times, then DLQ → Failure Handler)

   - Biometric mismatch: Specific customerId triggers identity failure
   - Document expired: Add expiration date check
   - Liveness check fails: Simulate liveness detection failure
   - **Payload Trigger**: `"customerId": "FAIL-ID-001"` or specific pattern
   - **Behavior**: Retry 3 times, then DLQ, Failure Handler processes

5. **Provisioning Failures** (Retry 3 times, then DLQ → Failure Handler)

   - Account creation conflict: Duplicate account ID (rare but possible)
   - Database constraint violation: Invalid data causes DB error
   - Database connection failure: Simulate DB unavailable
   - **Payload Trigger**: `"customerId": "FAIL-PROV-001"` or specific pattern
   - **Behavior**: Retry 3 times, then DLQ, Failure Handler processes

6. **Notification Failures** (Retry 3 times, then DLQ → Failure Handler)

   - Invalid email address: Email format valid but address doesn't exist
   - SMTP server unavailable: Email server down
   - Email bounce: Simulate email bounce
   - **Payload Trigger**: `"email": "bounce@example.com"` or specific pattern
   - **Behavior**: Retry 3 times, then DLQ, Failure Handler processes

### Implementation Approach

1. **Error Trigger Logic**

   - Add error trigger patterns in each service
   - Check payload for specific values that trigger errors
   - Use configuration for error trigger patterns (not hardcoded)

2. **Error Classification**

   - Create error types: ValidationError, BusinessRuleError, ServiceError
   - Classify errors as transient (retry) vs permanent (no retry)
   - Implement error classification logic

3. **Custom Error Handling**

   - **Validation Errors**: Fail immediately in API, return 400, don't publish event
   - **Business Rule Errors**: Fail immediately in API, return 400, publish `OnboardingFailedEvent`
   - **Service Errors**: Retry 3 times, then DLQ, Failure Handler processes and publishes failure event

---

## Phase 3: Failure Handler Service Integration

### Components to Implement

1. **DLQ Processing**

   - Configure Failure Handler to consume from DLQs
   - Process failed messages from DLQ
   - Extract error details from messages
   - Publish appropriate failure events

2. **Failure Event Publishing**

   - Publish `KYCFailedEvent`, `IdentityVerificationFailedEvent`, etc.
   - Include error code, error message, retry count
   - Update database with failure records

3. **Failure Tracking**

   - Store failures in `onboarding_failures` table
   - Track retry counts
   - Link failures to original request ID

### Success Criteria

- Failure Handler processes DLQ messages
- Failure events are published correctly
- Failures are tracked in database
- Onboarding status is updated to FAILED

---

## Implementation Details

### Error Trigger Patterns (Configuration)

```yaml
error-triggers:
  kyc:
    blacklist-customer-ids: ["BLACKLIST-001", "BLACKLIST-002"]
    failure-pattern: "FAIL-KYC-*"
  identity:
    failure-customer-ids: ["FAIL-ID-001"]
    failure-pattern: "FAIL-ID-*"
  provisioning:
    failure-customer-ids: ["FAIL-PROV-001"]
    failure-pattern: "FAIL-PROV-*"
  notification:
    bounce-emails: ["bounce@example.com"]
    failure-pattern: "fail-*@example.com"
```

### Email Configuration Example

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
onboarding:
  email:
    from: noreply@example.com
    from-name: "Onboarding System"
```

### Testing Approach

1. **Happy Path**: Normal payload → Complete workflow
2. **Validation Error**: Invalid email → 400 response
3. **Business Rule Error**: Unsupported document type → 400 response + failure event
4. **KYC Failure**: `"customerId": "BLACKLIST-001"` → Retry 3 times → DLQ → Failure Handler
5. **Identity Failure**: `"customerId": "FAIL-ID-001"` → Retry 3 times → DLQ → Failure Handler
6. **Provisioning Failure**: `"customerId": "FAIL-PROV-001"` → Retry 3 times → DLQ → Failure Handler
7. **Notification Failure**: `"email": "bounce@example.com"` → Retry 3 times → DLQ → Failure Handler

---

## Files to Modify/Create

### Notification Service

- `pom.xml` - Add Spring Mail dependency
- `application.yml` - Add SMTP configuration
- `service/NotificationService.java` - Implement real email sending
- `config/EmailConfig.java` - Email configuration
- `template/welcome-email.html` - Email template

### All Services

- `service/*.java` - Add error trigger logic
- `config/ErrorConfig.java` - Error trigger configuration
- Error classification logic

### API Service

- `controller/OnboardingController.java` - Enhanced validation
- `service/OnboardingService.java` - Business rule validation
- `exception/*.java` - Custom exception handlers

### Failure Handler

- `listener/DlqEventListener.java` - DLQ message processing
- `service/FailureHandlerService.java` - Enhanced failure handling

---

## Success Criteria Summary

- ✅ Real emails sent via SMTP when accounts are provisioned
- ✅ Validation errors return 400 immediately (no retry)
- ✅ Business rule violations return 400 + publish failure event (no retry)
- ✅ Service errors trigger retry (3 attempts), then DLQ
- ✅ Failure Handler processes DLQ messages and publishes failure events
- ✅ All failures are tracked in database
- ✅ Specific payloads trigger specific error scenarios
- ✅ Error handling strategies work as designed

---

## Estimated Implementation Order

1. **Phase 1**: Real Email (SMTP) - 1-2 hours
2. **Phase 2**: Error Scenarios - 2-3 hours
3. **Phase 3**: Failure Handler Integration - 1-2 hours
4. **Testing**: Verify all scenarios - 1 hour

**Total**: ~5-8 hours