# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY identity-service/pom.xml ./identity-service/
RUN mvn dependency:go-offline -B -pl common-events,identity-service
COPY common-events ./common-events
COPY identity-service ./identity-service
RUN mvn clean package -DskipTests -pl common-events,identity-service

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/identity-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
