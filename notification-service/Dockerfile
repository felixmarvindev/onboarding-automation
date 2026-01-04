# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY notification-service/pom.xml ./notification-service/
RUN mvn dependency:go-offline -B -pl common-events,notification-service
COPY common-events ./common-events
COPY notification-service ./notification-service
RUN mvn clean package -DskipTests -pl common-events,notification-service

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
