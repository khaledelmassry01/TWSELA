# Multi-stage build for production
FROM maven:3.8.4-openjdk-17-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Install necessary packages
RUN apk add --no-cache curl

# Create app user
RUN addgroup -S twsela && adduser -S twsela -G twsela

# Set working directory
WORKDIR /app

# Copy the built jar
COPY --from=builder /app/target/twsela-0.0.1-SNAPSHOT.jar app.jar

# Create log directory
RUN mkdir -p /var/log/twsela && chown -R twsela:twsela /var/log/twsela

# Change ownership
RUN chown -R twsela:twsela /app

# Switch to non-root user
USER twsela

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]