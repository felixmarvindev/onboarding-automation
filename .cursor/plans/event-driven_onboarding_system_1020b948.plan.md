---
name: Event-Driven Onboarding System
overview: A 5-phase implementation plan for building an event-driven digital onboarding system using Spring Boot, RabbitMQ, and Docker. Each phase builds upon the previous one, starting with the core happy path and progressing through resilience, containerization, CI/CD, and final polish.
todos:
  - id: phase1-structure
    content: Set up multi-module Maven project structure with parent POM and all service modules
    status: completed
  - id: phase1-events
    content: Implement common-events module with all event models and serialization utilities
    status: completed
    dependencies:
      - phase1-structure
  - id: phase1-rabbitmq
    content: Configure RabbitMQ exchanges, queues, and bindings in all services
    status: completed
    dependencies:
      - phase1-events
  - id: phase1-api
    content: Implement onboarding-api REST endpoint to initiate onboarding requests
    status: completed
    dependencies:
      - phase1-rabbitmq
  - id: phase1-services
    content: Implement all 5 stage services (KYC, Identity, Provisioning, Notification, Completion) with event listeners and publishers
    status: completed
    dependencies:
      - phase1-rabbitmq
  - id: phase1-database
    content: Set up PostgreSQL schema and JPA entities for onboarding requests and accounts
    status: completed
    dependencies:
      - phase1-structure
  - id: phase2-retry
    content: Add Spring Retry configuration with exponential backoff to all services
    status: completed
    dependencies:
      - phase1-services
  - id: phase2-failure-events
    content: Implement failure event models and failure event publishing logic
    status: completed
    dependencies:
      - phase1-events
  - id: phase2-dlq
    content: Configure dead-letter queues and exchanges for failed messages
    status: completed
    dependencies:
      - phase1-rabbitmq
  - id: phase2-failure-handler
    content: Implement failure-handler service to process failure events and update status
    status: completed
    dependencies:
      - phase2-failure-events
      - phase2-dlq
  - id: phase3-dockerfiles
    content: Create Dockerfiles for each service module with multi-stage builds
    status: completed
    dependencies:
      - phase1-services
  - id: phase3-compose
    content: Create docker-compose.yml with all services, RabbitMQ, and PostgreSQL
    status: completed
    dependencies:
      - phase3-dockerfiles
  - id: phase4-tests
    content: Write unit and integration tests with 70%+ coverage
    status: completed
    dependencies:
      - phase2-failure-handler
  - id: phase4-jenkins
    content: Create Jenkinsfile with build, test, SonarQube, and Docker stages
    status: completed
    dependencies:
      - phase3-compose
  - id: phase4-jmeter
    content: Create JMeter test plan for load testing onboarding API
    status: completed
    dependencies:
      - phase1-api
  - id: phase5-refinement
    content: Refine code structure, naming, and remove temporary code
    status: completed
    dependencies:
      - phase4-tests
  - id: phase5-docs
    content: Write comprehensive README, API docs (Swagger), and event documentation
    status: completed
    dependencies:
      - phase5-refinement
---

# Event-Driven Digital Onboarding System - Implementation Plan

## Architecture Overview

The system uses an event-driven architecture with RabbitMQ for message queuing. Each stage is a separate Spring Boot service that listens to events, processes business logic, and emits events to trigger the next stage.

**Event Flow:**

```
OnboardingRequested → KYCCompleted → IdentityVerified → AccountProvisioned → NotificationSent → OnboardingCompleted
```

Each stage can also emit failure events that trigger retry logic or failure handling.

**Component Structure:**

- `onboarding-api`: REST API to initiate onboarding requests
- `kyc-service`: KYC processing stage
- `identity-service`: Identity verification stage
- `provisioning-service`: Account/service provisioning stage
- `notification-service`: Customer notification stage
- `completion-service`: Final onboarding completion stage
- Shared: Common event models, utilities, and configuration

---

## Phase 1: Core Onboarding Flow (Happy Path)

### Objective

Implement the complete happy path workflow where a customer onboarding request flows through all stages successfully.

### Components to Implement

1. **Project Structure Setup**

   - Spring Boot parent project with multi-module structure
   - Modules: `onboarding-api`, `kyc-service`, `identity-service`, `provisioning-service`, `notification-service`, `completion-service`, `common-events`
   - `pom.xml` with Spring Boot, Spring AMQP (RabbitMQ), Spring Data JPA, PostgreSQL dependencies
   - Basic package structure: `com.onboarding.{module}.{controller|service|config|model|listener}`

2. **Common Events Module** (`common-events`)

   - Event base class/interfaces
   - Event models:
     - `OnboardingRequestedEvent` (customerId, requestId, customerData)
     - `KYCCompletedEvent` (requestId, kycStatus, kycData)
     - `IdentityVerifiedEvent` (requestId, verificationStatus, identityData)
     - `AccountProvisionedEvent` (requestId, accountId, accountDetails)
     - `NotificationSentEvent` (requestId, notificationType, deliveryStatus)
     - `OnboardingCompletedEvent` (requestId, finalStatus, completionTimestamp)
   - Event serialization/deserialization utilities

3. **RabbitMQ Configuration** (in each service)

   - Exchange configuration: `onboarding.exchange` (topic exchange)
   - Queue bindings:
     - `kyc.queue` → routing key: `onboarding.requested`
     - `identity.queue` → routing key: `kyc.completed`
     - `provisioning.queue` → routing key: `identity.verified`
     - `notification.queue` → routing key: `account.provisioned`
     - `completion.queue` → routing key: `notification.sent`
   - RabbitTemplate and MessageListenerContainer setup

4. **Onboarding API** (`onboarding-api`)

   - REST controller: `POST /api/onboarding/request`
   - Request DTO: `OnboardingRequest` (customerId, name, email, documentType, documentNumber)
   - Service: Publishes `OnboardingRequestedEvent` to RabbitMQ
   - Response: `{requestId, status: "INITIATED", timestamp}`

5. **KYC Service** (`kyc-service`)

   - Event listener for `OnboardingRequestedEvent`
   - Mock KYC processing logic (simulate external service call with delay)
   - Publishes `KYCCompletedEvent` on success
   - Logs: Request received, processing started, KYC completed

6. **Identity Service** (`identity-service`)

   - Event listener for `KYCCompletedEvent`
   - Mock identity verification (document validation simulation)
   - Publishes `IdentityVerifiedEvent` on success
   - Logs: Verification received, validation in progress, identity verified

7. **Provisioning Service** (`provisioning-service`)

   - Event listener for `IdentityVerifiedEvent`
   - Mock account provisioning (generate accountId, create account record)
   - Simple database persistence (PostgreSQL): `accounts` table (id, requestId, customerId, accountId, status, createdAt)
   - Publishes `AccountProvisionedEvent` on success
   - Logs: Provisioning request received, account created, provisioning completed

8. **Notification Service** (`notification-service`)

   - Event listener for `AccountProvisionedEvent`
   - Mock notification sending (email/SMS simulation)
   - Publishes `NotificationSentEvent` on success
   - Logs: Notification request received, notification sent, delivery confirmed

9. **Completion Service** (`completion-service`)

   - Event listener for `NotificationSentEvent`
   - Updates final onboarding status
   - Publishes `OnboardingCompletedEvent`
   - Logs: Completion request received, onboarding marked complete

10. **Database Schema** (PostgreSQL)

    - `onboarding_requests` table: requestId (PK), customerId, status, createdAt, completedAt
    - `accounts` table: id (PK), requestId, customerId, accountId, status, createdAt

### Events Involved

- `onboarding.requested` → triggers KYC
- `kyc.completed` → triggers Identity Verification
- `identity.verified` → triggers Provisioning
- `account.provisioned` → triggers Notification
- `notification.sent` → triggers Completion
- `onboarding.completed` (final event)

### Success Criteria

- All services start without errors
- REST API accepts onboarding request and returns requestId
- Events flow sequentially through all stages
- All services log their processing steps
- Account record is created in database
- Onboarding status progresses from INITIATED → COMPLETED

### Manual Verification Steps

1. Start RabbitMQ locally (or via Docker)
2. Start PostgreSQL database
3. Start all services in order (or use Spring Boot DevTools for auto-restart)
4. Send POST request to `/api/onboarding/request` with sample customer data
5. Monitor logs across all services to see event flow
6. Check database for account creation and status updates
7. Verify final completion event is published

---

## Phase 2: Retry Logic and Failure Handling

### Objective

Add resilience to the system: retry logic, failure events, error handling, and dead-letter queues.

### Components to Implement

1. **Failure Event Models** (`common-events`)

   - `KYCFailedEvent`, `IdentityVerificationFailedEvent`, `ProvisioningFailedEvent`, `NotificationFailedEvent`
   - All include: requestId, errorCode, errorMessage, retryCount, timestamp

2. **Retry Configuration** (each service)

   - Spring Retry integration
   - Retry policy: max 3 attempts with exponential backoff (1s, 2s, 4s)
   - Retryable exceptions: Transient failures (network timeouts, temporary service unavailability)

3. **Dead Letter Queues** (RabbitMQ)

   - DLX configuration: `onboarding.dlx` exchange
   - DLQ for each service: `kyc.dlq`, `identity.dlq`, `provisioning.dlq`, `notification.dlq`
   - Messages that fail after all retries are sent to DLQ

4. **Failure Event Publishing**

   - Each service publishes failure event after retry exhaustion
   - Failure events include retry count and error details
   - Failure events use routing keys: `kyc.failed`, `identity.failed`, etc.

5. **Failure Handler Service** (new module: `failure-handler`)

   - Listens to all failure events
   - Updates onboarding status to `FAILED` in database
   - Logs failure details with requestId for traceability
   - Optionally publishes `OnboardingFailedEvent`

6. **Database Updates**

   - Add `status` enum: INITIATED, IN_PROGRESS, COMPLETED, FAILED, RETRYING
   - Add `onboarding_failures` table: id, requestId, stage, errorCode, errorMessage, retryCount, failedAt
   - Update status tracking throughout workflow

7. **Transient vs Permanent Failures**

   - Define transient failures (network errors, timeouts) → retry
   - Define permanent failures (invalid data, business rule violations) → fail immediately
   - Each service implements failure classification logic

8. **Circuit Breaker Pattern** (optional, for advanced resilience)

   - Resilience4j integration
   - Circuit breaker for external service calls (if time permits)

### Events Involved

- All existing success events
- New failure events: `kyc.failed`, `identity.failed`, `provisioning.failed`, `notification.failed`
- `onboarding.failed` (final failure event)

### Success Criteria

- Services retry transient failures up to 3 times
- After retry exhaustion, messages are sent to DLQ
- Failure events are published and handled
- Database records failure details
- Onboarding status correctly reflects FAILED state
- Permanent failures fail immediately without retries

### Manual Verification Steps

1. Test transient failure: Temporarily disable a downstream service, trigger onboarding
2. Verify retry logs show 3 attempts with backoff
3. Check DLQ for message after retries exhausted
4. Verify failure event is published
5. Check database for failure record and FAILED status
6. Test permanent failure: Send invalid data, verify immediate failure
7. Test recovery: Fix issue, reprocess DLQ message, verify success

---

## Phase 3: Docker & Local Environment Orchestration

### Objective

Containerize all services and set up local development environment using Docker Compose.

### Components to Implement

1. **Dockerfiles** (one per service module)

   - Multi-stage builds (build stage, runtime stage)
   - Base image: `eclipse-temurin:17-jre-alpine`
   - Copy JAR files from build
   - Expose service ports
   - Health checks

2. **Docker Compose Configuration** (`docker-compose.yml`)

   - Services:
     - `rabbitmq`: Official RabbitMQ image with management plugin
     - `postgres`: PostgreSQL 15 image
     - `onboarding-api`: Build from Dockerfile
     - `kyc-service`: Build from Dockerfile
     - `identity-service`: Build from Dockerfile
     - `provisioning-service`: Build from Dockerfile
     - `notification-service`: Build from Dockerfile
     - `completion-service`: Build from Dockerfile
   - Networks: `onboarding-network`
   - Volumes: PostgreSQL data persistence
   - Environment variables: Database connection, RabbitMQ connection, service ports
   - Depends_on with health checks

3. **Environment Configuration**

   - `application.yml` per service with Docker profiles
   - Spring profiles: `local`, `docker`
   - Externalized configuration for database and RabbitMQ URLs
   - Service discovery via Docker service names

4. **Build Script** (optional: `build.sh`)

   - Maven clean install
   - Build all Docker images
   - Tag images appropriately

5. **Startup Scripts**

   - `docker-compose up` command documentation
   - Health check verification script
   - Service dependency order verification

6. **README Updates**

   - Docker setup instructions
   - Prerequisites (Docker, Docker Compose)
   - Commands to start/stop services
   - How to view logs: `docker-compose logs -f [service-name]`
   - How to access RabbitMQ management UI (http://localhost:15672)
   - How to access PostgreSQL (port 5432)

### Success Criteria

- All services build successfully into Docker images
- Docker Compose starts all services in correct order
- All services connect to RabbitMQ and PostgreSQL
- Services communicate via Docker network
- Health checks pass for all services
- No manual service startup required

### Manual Verification Steps

1. Run `docker-compose up --build`
2. Verify all containers start successfully
3. Check logs for connection success messages
4. Access RabbitMQ management UI, verify queues and exchanges exist
5. Connect to PostgreSQL, verify databases/tables exist
6. Send onboarding request via API
7. Monitor logs across containers to verify event flow
8. Verify database updates across containers
9. Test service restart: `docker-compose restart [service]`, verify reconnection

---

## Phase 4: CI/CD, Code Quality, and Load Testing Demos

### Objective

Set up demo-level CI/CD pipeline, code quality tools, and load testing to showcase portfolio completeness.

### Components to Implement

1. **Jenkins Pipeline** (`Jenkinsfile`)

   - Stages:
     - Checkout source code
     - Build (Maven clean install)
     - Run unit tests
     - Static code analysis (SonarQube integration)
     - Build Docker images
     - Push images to registry (optional: Docker Hub or local registry)
     - Deploy to staging (optional: simple Docker Compose deployment)
   - Pipeline should be declarative (Jenkinsfile)
   - Triggers: Git webhook or manual trigger

2. **SonarQube Integration**

   - `sonar-project.properties` configuration
   - SonarQube server setup (Docker container in docker-compose.yml for local demo)
   - Maven SonarQube plugin configuration
   - Code quality gates: Coverage threshold, code smells, security vulnerabilities
   - SonarQube analysis runs in Jenkins pipeline

3. **Unit Tests**

   - JUnit 5 tests for each service
   - Mock event publishers/listeners
   - Test coverage: Service logic, event handling, error scenarios
   - MockMvc tests for REST API
   - Target: 70%+ code coverage

4. **Integration Tests**

   - Spring Boot Test with embedded RabbitMQ (or Testcontainers)
   - Testcontainers for PostgreSQL integration tests
   - End-to-end workflow tests: Full event flow from API to completion
   - Failure scenario tests

5. **JMeter Load Testing**

   - JMeter test plan: `load-test.jmx`
   - Scenario: Send 100 concurrent onboarding requests
   - Metrics to capture: Response times, throughput, error rates
   - Test endpoints: POST /api/onboarding/request
   - Result analysis: Generate HTML reports

6. **Code Quality Tools**

   - Checkstyle configuration (Java code style)
   - SpotBugs (static analysis)
   - PMD (code quality rules)
   - All integrated into Maven build

7. **Docker Compose Extension** (for CI/CD tools)

   - Add SonarQube service (if not external)
   - Jenkins service (optional, or use external Jenkins)
   - JMeter service (for running load tests)

8. **CI/CD Documentation**

   - Pipeline overview diagram
   - How to run SonarQube analysis locally
   - How to run JMeter tests
   - How to interpret test results

### Success Criteria

- Jenkins pipeline runs successfully end-to-end
- SonarQube analysis completes and shows code quality metrics
- Unit tests achieve 70%+ coverage
- Integration tests verify complete workflow
- JMeter load test completes and generates reports
- Code quality tools run in build process
- All quality gates pass

### Manual Verification Steps

1. Set up Jenkins (locally or CI server), configure pipeline
2. Trigger pipeline, verify all stages complete
3. View SonarQube dashboard, review code quality metrics
4. Run unit tests: `mvn test`, verify coverage report
5. Run integration tests: `mvn verify`, verify all pass
6. Run JMeter: `jmeter -n -t load-test.jmx -l results.jtl`, generate HTML report
7. Analyze load test results: Response times, throughput
8. Verify code quality tools run during build

---

## Phase 5: Final Portfolio Polish

### Objective

Refine code structure, naming, documentation, and overall project presentation for portfolio showcase.

### Components to Implement

1. **Code Structure Refinement**

   - Review and standardize package naming
   - Ensure consistent code style across all modules
   - Remove any debugging code or temporary workarounds
   - Add meaningful JavaDoc comments to public APIs
   - Extract magic numbers to constants
   - Improve variable and method naming for clarity

2. **Error Messages and Logging**

   - Standardize log message formats
   - Use structured logging (JSON format or consistent text format)
   - Ensure all log messages include requestId for traceability
   - Improve error messages to be user-friendly and actionable
   - Add correlation IDs for request tracing

3. **Configuration Management**

   - Externalize all configuration (no hardcoded values)
   - Use Spring profiles appropriately
   - Document configuration properties
   - Add validation for required configuration

4. **README Documentation**

   - Clear project description and architecture overview
   - Prerequisites and setup instructions
   - How to run locally (Docker Compose)
   - API documentation (endpoints, request/response examples)
   - Architecture diagram (ASCII art or reference to diagram file)
   - Event flow diagram
   - How to verify system is working
   - Troubleshooting section

5. **API Documentation**

   - OpenAPI/Swagger integration (SpringDoc OpenAPI)
   - API endpoint documentation with examples
   - Request/response schemas
   - Access Swagger UI at `/swagger-ui.html`

6. **Event Documentation**

   - Document all events (purpose, payload, routing keys)
   - Event sequence diagram
   - Failure event handling documentation

7. **Database Schema Documentation**

   - ER diagram or schema documentation
   - Table descriptions
   - Index documentation

8. **Project Structure Documentation**

   - Module descriptions
   - Package structure explanation
   - Key classes and their responsibilities

9. **Portfolio Presentation Elements**

   - Clean, professional README
   - Consistent code formatting
   - Meaningful commit messages (if using Git)
   - .gitignore properly configured
   - License file (if applicable)

10. **Final Testing and Verification**

    - Full end-to-end test of happy path
    - Full end-to-end test of failure scenarios
    - Verify all documentation is accurate
    - Check for any remaining TODOs or FIXMEs
    - Performance verification (services start quickly, events flow smoothly)

### Success Criteria

- Code is clean, well-structured, and follows Java best practices
- All documentation is accurate and complete
- README provides clear setup and usage instructions
- API is documented via Swagger
- Event flow is clearly documented
- No debugging code or temporary workarounds remain
- Code passes all quality checks
- Project is ready for portfolio showcase

### Manual Verification Steps

1. Review README, verify all instructions work
2. Run full Docker Compose setup from scratch, verify everything works
3. Test API via Swagger UI, verify documentation matches implementation
4. Review code structure, ensure consistency
5. Run all tests, verify 100% pass
6. Check logs, verify they are clear and traceable
7. Review documentation for accuracy
8. Present project to a colleague (or self-review) for clarity feedback

---

## Implementation Order and Dependencies

- **Phase 1** must be completed first (foundation)
- **Phase 2** depends on Phase 1 (adds resilience)
- **Phase 3** can be done after Phase 1 or Phase 2 (containerization)
- **Phase 4** depends on Phases 1-3 (CI/CD and quality)
- **Phase 5** is final polish after all functionality is complete

## Key Design Decisions

1. **Multi-module Maven project**: Keeps services separate but manageable in one repository
2. **Topic Exchange for RabbitMQ**: Allows flexible routing and easy addition of new listeners
3. **PostgreSQL for persistence**: More portfolio-worthy than in-memory, still simple to set up
4. **Spring Boot for all services**: Consistent framework, easy configuration, production-ready
5. **Mock external services**: Keeps focus on architecture, avoids external API dependencies
6. **Docker Compose for orchestration**: Simple, effective for local demo, industry standard

## Estimated File Structure (Final)

```
onboarding-automation/
├── pom.xml (parent)
├── docker-compose.yml
├── Jenkinsfile
├── README.md
├── common-events/
│   └── src/main/java/com/onboarding/events/
├── onboarding-api/
│   └── src/main/java/com/onboarding/api/
├── kyc-service/
│   └── src/main/java/com/onboarding/kyc/
├── identity-service/
│   └── src/main/java/com/onboarding/identity/
├── provisioning-service/
│   └── src/main/java/com/onboarding/provisioning/
├── notification-service/
│   └── src/main/java/com/onboarding/notification/
├── completion-service/
│   └── src/main/java/com/onboarding/completion/
└── failure-handler/
    └── src/main/java/com/onboarding/failure/
```