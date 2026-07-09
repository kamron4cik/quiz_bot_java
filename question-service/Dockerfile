# question-service — Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY pom.xml .
COPY common-libraries/ common-libraries/
COPY question-service/ question-service/
RUN apk add --no-cache maven &&     mvn -B package -pl question-service -am -DskipTests -q
    
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S quizplatform && adduser -S quizplatform -G quizplatform
USER quizplatform
WORKDIR /app
COPY --from=builder /workspace/question-service/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3     CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1
EXPOSE 8084
ENTRYPOINT ["java","--enable-preview","-XX:+UseZGC","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
