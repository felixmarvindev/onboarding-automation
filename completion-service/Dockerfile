# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY completion-service/pom.xml ./completion-service/
RUN mvn dependency:go-offline -B -pl common-events,completion-service
COPY common-events ./common-events
COPY completion-service ./completion-service
RUN mvn clean package -DskipTests -pl common-events,completion-service

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/completion-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
