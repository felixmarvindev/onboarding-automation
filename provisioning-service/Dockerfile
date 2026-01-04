# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY provisioning-service/pom.xml ./provisioning-service/
RUN mvn dependency:go-offline -B -pl common-events,provisioning-service
COPY common-events ./common-events
COPY provisioning-service ./provisioning-service
RUN mvn clean package -DskipTests -pl common-events,provisioning-service

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/provisioning-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
