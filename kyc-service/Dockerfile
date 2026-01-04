# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY kyc-service/pom.xml ./kyc-service/
RUN mvn dependency:go-offline -B -pl common-events,kyc-service
COPY common-events ./common-events
COPY kyc-service ./kyc-service
RUN mvn clean package -DskipTests -pl common-events,kyc-service

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/kyc-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
