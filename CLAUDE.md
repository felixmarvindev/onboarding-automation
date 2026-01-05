# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an event-driven workflow automation system for digital customer onboarding. It uses a microservices architecture where services communicate via RabbitMQ events, orchestrating the full lifecycle of customer onboarding requests.

## Build and Development Commands

### Build the entire project
```bash
mvn clean install
```

### Build without tests
```bash
mvn clean install -DskipTests
```

### Run tests
```bash
# All tests
mvn test

# Single module
cd <service-name> && mvn test

# Single test class
mvn test -Dtest=ClassName

# Single test method
mvn test -Dtest=ClassName#methodName
```

### Run services locally

Start infrastructure first (RabbitMQ and PostgreSQL):
```bash
docker-compose up postgres rabbitmq
```

Then run services individually in separate terminals:
```bash
cd <service-name> && mvn spring-boot:run
```

### Run entire system with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# View logs
docker-compose logs -f <service-name>

# Stop all services
docker-compose down
```

### Code quality and coverage
```bash
# Generate JaCoCo coverage report
mvn jacoco:report

# Run SonarQube analysis
mvn sonar:sonar
```

## Architecture

### Event-Driven Flow

The system orchestrates onboarding through a chain of events. Each service listens to specific events, processes business logic, and publishes the next event:

```
OnboardingRequested → KYC → Identity → Provisioning → Notification → Completion
```

**Critical Pattern**: Each service:
1. Listens to a specific routing key on the `onboarding.exchange` topic exchange
2. Processes business logic
3. Publishes success event (next stage) OR failure event
4. Failure events are routed to `failure-handler` service

### RabbitMQ Configuration Pattern

All services follow this RabbitMQ setup pattern:

- **Exchange**: `onboarding.exchange` (topic exchange, durable)
- **Dead Letter Exchange**: `onboarding.dlx` (for failed message handling)
- **Queue Naming**: `<service>.queue` (e.g., `kyc.queue`)
- **DLQ Naming**: `<service>.dlq` (dead letter queue)
- **Routing Keys**: Defined in `common-events/src/main/java/com/onboarding/events/EventRoutingKeys.java`

Each service's queue has:
- `x-dead-letter-exchange`: `onboarding.dlx`
- `x-dead-letter-routing-key`: `<service>.dlq`
- `defaultRequeueRejected`: false (messages go to DLQ on rejection)

### Event Models

All event classes are in the `common-events` module. Key patterns:

- **BaseEvent**: Contains `requestId`, `customerId`, `timestamp`
- **Success Events**: Extend BaseEvent (e.g., `KYCCompletedEvent`, `IdentityVerifiedEvent`)
- **Failure Events**: Extend `BaseFailureEvent` with `errorMessage`, `failureReason`, `retryCount`

### Retry and Error Handling

Services use Spring Retry with:
- `@Retryable`: Max 3 attempts, exponential backoff (1s initial, 2x multiplier)
- `@Recover`: Fallback method publishes failure events
- After max retries exhausted, messages go to DLQ
- DLQ messages are consumed by `failure-handler` service

### Error Trigger Mechanism

Services support injecting errors for testing via `ErrorTriggerProperties`:
- Customer ID blacklist (exact match)
- Failure pattern (wildcard matching with `*`)
- Configured in `application.yml` under `error-trigger.<service>`

Example:
```yaml
error-trigger:
  kyc:
    blacklist-customer-ids: ["CUST-ERROR", "CUST-FAIL"]
    failure-pattern: "FAIL-*"
```

### Database Services

Only two services use PostgreSQL:

1. **provisioning-service**: Stores `onboarding_requests` and `accounts` tables
2. **failure-handler**: Stores `onboarding_failures` table for tracking all failures

All database schemas auto-create via JPA/Hibernate DDL.

## Service Responsibilities

- **onboarding-api** (port 8080): REST API entry point, publishes `OnboardingRequested` events
- **kyc-service**: Performs KYC validation, publishes `KYCCompleted` or `KYCFailed`
- **identity-service**: Verifies identity documents, publishes `IdentityVerified` or `IdentityVerificationFailed`
- **provisioning-service**: Creates accounts in database, publishes `AccountProvisioned` or `ProvisioningFailed`
- **notification-service**: Sends welcome emails, publishes `NotificationSent` or `NotificationFailed`
- **completion-service**: Finalizes onboarding, publishes `OnboardingCompleted`
- **failure-handler**: Listens to all failure events and DLQs, persists failures to database, publishes `OnboardingFailed`

## Testing the System

Send a test onboarding request:
```bash
curl -X POST http://localhost:8080/api/onboarding/request \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "documentType": "PASSPORT",
    "documentNumber": "DOC123456"
  }'
```

Monitor RabbitMQ: http://localhost:15672 (guest/guest)

Trigger a KYC failure for testing:
```bash
curl -X POST http://localhost:8080/api/onboarding/request \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-ERROR",
    "name": "Test Error",
    "email": "error@example.com",
    "documentType": "PASSPORT",
    "documentNumber": "DOC123456"
  }'
```

## Module Structure

This is a Maven multi-module project. When adding dependencies to services, remember that `common-events` is already included as a dependency in all service modules.

Parent POM manages:
- Spring Boot 3.2.0
- Java 17
- Dependency versions for Spring AMQP, PostgreSQL, JUnit, Mockito
- JaCoCo and SonarQube plugins

## Key Configuration Files

- `application.yml`: Default configuration for local development
- `application-docker.yml`: Configuration profile for Docker environment
- `docker-compose.yml`: Service orchestration with environment variables
- `notification-service/.env`: Email SMTP credentials (not in git)

## Jenkins CI/CD Pipeline

The Jenkinsfile defines stages:
1. Build → Unit Tests → Code Coverage → SonarQube Analysis → Quality Gate
2. Package → Build Docker Images → Docker Compose Test

Quality gate will abort pipeline if SonarQube metrics fail.
