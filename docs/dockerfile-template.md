# Quiz Platform — Dockerfiles

# ──────────────────────────────────────────────────────────────
# Standard multi-stage Dockerfile for all Spring Boot services
# Usage: Copy this to each service directory as Dockerfile
# ──────────────────────────────────────────────────────────────

# NOTE: Each service has its own Dockerfile in its directory.
# They all follow this pattern.

# ── user-service/Dockerfile ───────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy parent POM and service POM for dependency resolution
COPY pom.xml .
COPY common-libraries/common-domain/pom.xml common-libraries/common-domain/pom.xml
COPY common-libraries/common-security/pom.xml common-libraries/common-security/pom.xml
COPY common-libraries/common-observability/pom.xml common-libraries/common-observability/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# Download dependencies (cached layer)
RUN mvn -B dependency:go-offline -pl user-service -am -q || true

# Copy source
COPY common-libraries/ common-libraries/
COPY user-service/src user-service/src

# Build
RUN mvn -B package -pl user-service -am -DskipTests -q

# ── Runtime image ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root
RUN addgroup -S quizplatform && adduser -S quizplatform -G quizplatform
USER quizplatform

WORKDIR /app

# Copy fat JAR
COPY --from=builder /workspace/user-service/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", \
    "--enable-preview", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
