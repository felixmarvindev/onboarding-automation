# Digital Onboarding Automation System

An event-driven workflow automation system for digital customer onboarding, inspired by enterprise onboarding pipelines used in fintech, telecom, and banking platforms.

## Overview

This system automates the full lifecycle of a new customer onboarding request by orchestrating multiple independent stages using an event-driven architecture. Each stage is responsible for a single business responsibility and emits events to drive the next step in the onboarding lifecycle.

### Architecture

The system uses an event-driven architecture with RabbitMQ for message queuing. Each stage is a separate Spring Boot service that listens to events, processes business logic, and emits events to trigger the next stage.

**Event Flow:**
```
OnboardingRequested → KYCCompleted → IdentityVerified → AccountProvisioned → NotificationSent → OnboardingCompleted
```

### Components

- **onboarding-api**: REST API to initiate onboarding requests
- **kyc-service**: KYC processing stage
- **identity-service**: Identity verification stage
- **provisioning-service**: Account/service provisioning stage
- **notification-service**: Customer notification stage
- **completion-service**: Final onboarding completion stage
- **failure-handler**: Service to handle and track failures
- **common-events**: Shared event models and utilities

## Features

- ✅ Event-driven architecture with RabbitMQ
- ✅ Asynchronous workflow processing
- ✅ Retry logic with exponential backoff
- ✅ Dead-letter queues for failed messages
- ✅ Failure handling and tracking
- ✅ Docker containerization
- ✅ PostgreSQL persistence
- ✅ REST API with Swagger documentation
- ✅ CI/CD pipeline with Jenkins
- ✅ Code quality with SonarQube
- ✅ Load testing with JMeter

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 15 (optional, included in Docker Compose)
- RabbitMQ (optional, included in Docker Compose)

## Quick Start

### Using Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd onboarding-automation
```

2. Build and start all services:
```bash
docker-compose up --build
```

3. The system will be available at:
   - **API**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **RabbitMQ Management**: http://localhost:15672 (guest/guest)
   - **PostgreSQL**: localhost:5432 (onboarding_user/onboarding_pass)

4. Send a test request:
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

5. Monitor logs:
```bash
docker-compose logs -f onboarding-api
docker-compose logs -f kyc-service
docker-compose logs -f identity-service
docker-compose logs -f provisioning-service
docker-compose logs -f notification-service
docker-compose logs -f completion-service
```

6. Stop all services:
```bash
docker-compose down
```

### Local Development

1. Start RabbitMQ and PostgreSQL (via Docker Compose):
```bash
docker-compose up postgres rabbitmq
```

2. Build the project:
```bash
mvn clean install
```

3. Run services individually:
```bash
# Terminal 1: API
cd onboarding-api && mvn spring-boot:run

# Terminal 2: KYC Service
cd kyc-service && mvn spring-boot:run

# Terminal 3: Identity Service
cd identity-service && mvn spring-boot:run

# Terminal 4: Provisioning Service
cd provisioning-service && mvn spring-boot:run

# Terminal 5: Notification Service
cd notification-service && mvn spring-boot:run

# Terminal 6: Completion Service
cd completion-service && mvn spring-boot:run

# Terminal 7: Failure Handler
cd failure-handler && mvn spring-boot:run
```

## API Documentation

### Swagger UI

Once the services are running, access the API documentation at:
- http://localhost:8080/swagger-ui.html

### Endpoints

#### POST /api/onboarding/request

Initiates a new onboarding request.

**Request Body:**
```json
{
  "customerId": "string (required)",
  "name": "string (required)",
  "email": "string (required, valid email)",
  "documentType": "string (required)",
  "documentNumber": "string (required)"
}
```

**Response:**
```json
{
  "requestId": "uuid",
  "status": "INITIATED",
  "timestamp": "2024-01-01T12:00:00"
}
```

## Event Flow

1. **OnboardingRequested**: Published by onboarding-api when a new request is received
2. **KYCCompleted**: Published by kyc-service after KYC processing
3. **IdentityVerified**: Published by identity-service after identity verification
4. **AccountProvisioned**: Published by provisioning-service after account creation
5. **NotificationSent**: Published by notification-service after sending notification
6. **OnboardingCompleted**: Published by completion-service when onboarding is complete

### Failure Events

- **KYCFailed**: Published when KYC processing fails
- **IdentityVerificationFailed**: Published when identity verification fails
- **ProvisioningFailed**: Published when provisioning fails
- **NotificationFailed**: Published when notification sending fails
- **OnboardingFailed**: Published by failure-handler when onboarding fails

## Configuration

### Application Properties

Each service has its own `application.yml` file. Key configurations:

- **RabbitMQ**: Configured via `spring.rabbitmq.*`
- **PostgreSQL**: Configured via `spring.datasource.*` (provisioning-service and failure-handler)
- **Ports**: Each service runs on a different port (8080-8086)

### Docker Environment Variables

Services can be configured via environment variables in `docker-compose.yml`:

- `SPRING_RABBITMQ_HOST`: RabbitMQ host
- `SPRING_RABBITMQ_PORT`: RabbitMQ port
- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: PostgreSQL username
- `SPRING_DATASOURCE_PASSWORD`: PostgreSQL password

## Database Schema

### Tables

- **onboarding_requests**: Tracks onboarding request status
- **accounts**: Stores provisioned account information
- **onboarding_failures**: Records failure details

## Testing

### Unit Tests

Run unit tests:
```bash
mvn test
```

### Integration Tests

Run integration tests:
```bash
mvn verify
```

### Load Testing

Use JMeter to run load tests:
```bash
cd load-test
jmeter -n -t load-test.jmx -l results.jtl
jmeter -g results.jtl -o report/
```

## CI/CD

### Jenkins Pipeline

The project includes a Jenkinsfile for CI/CD pipeline:

1. Checkout source code
2. Build project
3. Run unit tests
4. Code coverage analysis
5. SonarQube analysis
6. Quality gate check
7. Build Docker images
8. Docker Compose test

### SonarQube

Code quality analysis is configured in `sonar-project.properties`.

## Project Structure

```
onboarding-automation/
├── pom.xml (parent)
├── docker-compose.yml
├── Jenkinsfile
├── README.md
├── common-events/          # Shared event models
├── onboarding-api/         # REST API service
├── kyc-service/            # KYC processing service
├── identity-service/       # Identity verification service
├── provisioning-service/   # Account provisioning service
├── notification-service/   # Notification service
├── completion-service/     # Completion service
├── failure-handler/        # Failure handling service
└── load-test/              # JMeter load tests
```

## Troubleshooting

### Services won't start

1. Check Docker is running: `docker ps`
2. Check ports are available: `netstat -an | grep 8080`
3. Check logs: `docker-compose logs [service-name]`

### RabbitMQ connection errors

1. Verify RabbitMQ is running: `docker-compose ps rabbitmq`
2. Check RabbitMQ management UI: http://localhost:15672
3. Verify queues and exchanges are created

### Database connection errors

1. Verify PostgreSQL is running: `docker-compose ps postgres`
2. Check database exists: `docker-compose exec postgres psql -U onboarding_user -d onboarding_db`
3. Verify connection credentials

### Events not flowing

1. Check all services are running: `docker-compose ps`
2. Check RabbitMQ queues: http://localhost:15672
3. Check service logs for errors
4. Verify exchange and queue bindings

## License

This project is provided as-is for portfolio/demonstration purposes.

## Contributing

This is a portfolio project. For questions or suggestions, please open an issue.
