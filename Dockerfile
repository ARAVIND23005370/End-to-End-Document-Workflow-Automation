# ===================================================================
# E2EDocs Backend - Multi-Stage Production Dockerfile
# ===================================================================

# -------------------------------------------------------------------
# Stage 1: Build Stage
# -------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile optimized production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# -------------------------------------------------------------------
# Stage 2: Minimal Production Runtime
# -------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runner

# Metadata labels
LABEL maintainer="AravindRamesh" \
      project="E2EDocs" \
      description="E2EDocs Document Workflow Automation Backend" \
      version="1.0.0"

# Install curl for container health check probe
RUN apk add --no-cache curl tzdata

# Create dedicated non-privileged runtime user and group
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

WORKDIR /app

# Create persistent storage mount directory with correct ownership
RUN mkdir -p /app/storage && \
    chown -R appuser:appgroup /app

# Copy application JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/e2edocs-backend-*.jar /app/app.jar

# Switch to non-root user
USER appuser:appgroup

# Expose HTTP port
EXPOSE 8080

# Define volume for persistent storage
VOLUME ["/app/storage"]

# Production Docker healthcheck probe
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Container-optimized JVM execution
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+ExitOnOutOfMemoryError", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=prod", \
            "-jar", "/app/app.jar"]
