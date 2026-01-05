# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM
COPY pom.xml .

# Copy POMs for all modules (parent POM requires them to exist)
COPY kyc-service/pom.xml ./kyc-service/
COPY identity-service/pom.xml ./identity-service/
COPY provisioning-service/pom.xml ./provisioning-service/
COPY notification-service/pom.xml ./notification-service/
COPY completion-service/pom.xml ./completion-service/
COPY failure-handler/pom.xml ./failure-handler/
COPY onboarding-api/pom.xml ./onboarding-api/
COPY common-events/pom.xml ./common-events/

# Download dependencies for the modules we need
RUN --mount=type=cache,target=/root/.m2 \
    sh -c 'set -e; \
    for i in 1 2 3; do \
        if mvn dependency:go-offline -B -pl common-events,onboarding-api -am; then \
            exit 0; \
        else \
            echo "Attempt $i failed, cleaning corrupted artifacts..."; \
            find /root/.m2/repository -name "*.lastUpdated" -delete || true; \
            find /root/.m2/repository -name "*.lock" -delete || true; \
            rm -rf /root/.m2/repository/com/google/guava/guava/16.0.1 || true; \
            rm -rf /root/.m2/repository/org/apache/maven/surefire/surefire-shared-utils/3.2.5 || true; \
            [ $i -lt 3 ] && sleep 10 || exit 1; \
        fi; \
    done'


# Copy the actual source code
COPY common-events ./common-events
COPY onboarding-api ./onboarding-api

# Build the project
RUN --mount=type=cache,target=/root/.m2 \
        mvn package -DskipTests -pl common-events,onboarding-api

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /app/onboarding-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
