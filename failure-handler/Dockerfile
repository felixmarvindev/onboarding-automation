# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-events/pom.xml ./common-events/
COPY failure-handler/pom.xml ./failure-handler/
RUN mvn dependency:go-offline -B -pl common-events,failure-handler
COPY common-events ./common-events
COPY failure-handler ./failure-handler
RUN mvn clean package -DskipTests -pl common-events,failure-handler

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/failure-handler/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
