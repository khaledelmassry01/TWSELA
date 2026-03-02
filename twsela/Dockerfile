# ==================================================================
# Multi-stage build for Twsela Spring Boot application
# ==================================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy only POM first — layer is cached until pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (this layer rebuilds only when src changes)
COPY src src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S twsela && adduser -S twsela -G twsela

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/twsela-0.0.1-SNAPSHOT.jar app.jar

# Create log directory and set ownership
RUN mkdir -p /var/log/twsela \
    && chown -R twsela:twsela /var/log/twsela \
    && chown -R twsela:twsela /app

# Switch to non-root user
USER twsela

# Expose application port
EXPOSE 8000

# Health check against the custom /api/health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8000/api/health || exit 1

# Optimised JVM flags for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]