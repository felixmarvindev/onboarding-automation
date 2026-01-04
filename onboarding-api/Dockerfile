# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY onboarding-api/pom.xml ./onboarding-api/
RUN mvn dependency:go-offline -B -pl common-events,onboarding-api
COPY common-events ./common-events
COPY onboarding-api ./onboarding-api
RUN mvn clean package -DskipTests -pl common-events,onboarding-api

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/onboarding-api/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
