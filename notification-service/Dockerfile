# notification-service — Multi-stage build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B package -pl notification-service -am -DskipTests -q
    
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S quizplatform && adduser -S quizplatform -G quizplatform
USER quizplatform
WORKDIR /app
COPY --from=builder /workspace/notification-service/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3     CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1
EXPOSE 8085
ENTRYPOINT ["java","-XX:+UseZGC","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
