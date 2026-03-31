# Multi-stage Dockerfile for Spring Boot (Java 21)

# --- Build stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy pom and resolve dependencies first to leverage Docker layer caching
COPY pom.xml ./
RUN mvn -q -DskipTests -Dspring-boot.build-image.skip=true dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests -Dspring-boot.build-image.skip=true package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Optional JVM and Spring profile settings
ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=dev

# Copy the built jar from the builder stage
COPY --from=builder /build/target/hr.system-1.0.0.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]