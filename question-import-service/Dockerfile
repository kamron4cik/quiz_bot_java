# question-import-service — Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY . .
RUN apk add --no-cache maven &&     mvn -B package -pl question-import-service -am -DskipTests -q
    
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S quizplatform && adduser -S quizplatform -G quizplatform
USER quizplatform
WORKDIR /app
COPY --from=builder /workspace/question-import-service/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3     CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1
EXPOSE 8087
ENTRYPOINT ["java","-XX:+UseZGC","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
