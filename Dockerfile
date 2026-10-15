# ==============================================================================
# STAGE 1: Build & Package Environment
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 1. Pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy application source code
COPY src ./src

# 3. Compile, verify with test suite, and package shaded executable JAR
RUN mvn clean package -DskipTests

# ==============================================================================
# STAGE 2: Minimal & Secure Production Runtime
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

# Security: Run as an unprivileged non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the shaded standalone fat JAR from the builder stage
COPY --from=builder /build/target/transaction-analyzer.jar /app/transaction-analyzer.jar

# Ensure proper file ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Default workspace directory for mounting external CSV files
WORKDIR /data

# Define the container entrypoint
ENTRYPOINT ["java", "-jar", "/app/transaction-analyzer.jar"]

# Default command displays help screen if no arguments are provided
CMD ["help"]