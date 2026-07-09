# Quiz Platform — Enterprise Java Microservices

[![CI/CD](https://github.com/yourusername/quiz-platform/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/yourusername/quiz-platform/actions)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Enterprise-grade redesign of the Telegram quiz platform for Uzbek university students. Migrated from Node.js/Supabase to Java 21 microservices with Clean Architecture, DDD, and Hexagonal Architecture.

---

## 🏗️ Architecture

```
telegram-service  →  gateway-service (8080)
                           │
          ┌────────────────┼─────────────────┐
          ▼                ▼                 ▼
    user-service     quiz-service    question-service
       (8082)           (8083)           (8084)
          │                │
          └────────────────┤
                           ▼
               notification-service (8085)
                           │
                        RabbitMQ
                           │
                ┌──────────┴──────────┐
                ▼                     ▼
         admin-service       question-import-service
            (8086)                  (8087)
```

## 📚 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (virtual threads) |
| Framework | Spring Boot 3.3, Spring Cloud 2023 |
| Database | PostgreSQL 16 + Flyway |
| Cache | Redis 7 |
| Messaging | RabbitMQ 3.13 |
| Storage | MinIO (S3) |
| Security | Spring Security + JWT (RS256) |
| API Docs | Springdoc OpenAPI |
| Observability | Prometheus + Grafana + Loki + Tempo |
| CI/CD | GitHub Actions + Helm + Kubernetes |

## 🚀 Quick Start (Local Development)

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker + Docker Compose

### 1. Start infrastructure

```bash
cd infrastructure
docker compose -f docker-compose.infra.yml up -d
```

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env with your BOT_TOKEN and other values
```

### 3. Build all modules

```bash
mvn clean package -DskipTests
```

### 4. Run a specific service

```bash
# user-service
cd user-service
mvn spring-boot:run

# Or run the fat JAR:
java -jar target/user-service-1.0.0-SNAPSHOT.jar
```

### 5. Run all services via Docker Compose

```bash
docker compose -f infrastructure/docker-compose.yml up -d
```

---

## 📊 Service Ports

| Service | Port | Swagger UI |
|---|---|---|
| gateway-service | 8080 | — |
| telegram-service | 8081 | — |
| user-service | 8082 | http://localhost:8082/swagger-ui.html |
| quiz-service | 8083 | http://localhost:8083/swagger-ui.html |
| question-service | 8084 | http://localhost:8084/swagger-ui.html |
| notification-service | 8085 | — |
| admin-service | 8086 | http://localhost:8086/swagger-ui.html |
| question-import-service | 8087 | http://localhost:8087/swagger-ui.html |

## 🔧 Infrastructure UIs

| Tool | URL | Credentials |
|---|---|---|
| RabbitMQ Management | http://localhost:15672 | guest/guest |
| MinIO Console | http://localhost:9001 | quizplatform/quizplatform123 |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin/admin |

---

## 📁 Project Structure

```
quiz-platform/
├── common-libraries/
│   ├── common-domain/          # Shared events, exceptions, value objects
│   ├── common-security/        # JWT utilities, role enums
│   └── common-observability/   # Micrometer config, logging setup
├── gateway-service/            # Spring Cloud Gateway (port 8080)
├── telegram-service/           # Telegram bot client (port 8081)
├── user-service/               # User management, profiles, auth (port 8082)
├── quiz-service/               # Quiz sessions, categories, leaderboard (port 8083)
├── question-service/           # Question CRUD and search (port 8084)
├── notification-service/       # Telegram message delivery (port 8085)
├── admin-service/              # Admin API, analytics, reports (port 8086)
├── question-import-service/    # Document parsing, async import (port 8087)
├── infrastructure/
│   ├── docker-compose.infra.yml    # PostgreSQL, Redis, RabbitMQ, MinIO, monitoring
│   └── init-scripts/
├── monitoring/
│   ├── prometheus/             # Alerting rules
│   ├── grafana/               # Pre-built dashboards
│   └── loki/                  # Log aggregation config
├── helm/quiz-platform/        # Helm chart for k8s deployment
├── k8s/                       # Kustomize manifests
├── docs/                      # ADRs, API specs, diagrams
└── .github/workflows/         # CI/CD pipelines
```

---

## 🧪 Testing

```bash
# Unit tests only (fast)
mvn test

# Unit + integration tests (requires Docker for Testcontainers)
mvn verify

# Tests for a specific service
mvn test -pl user-service
```

---

## 🌍 Business Domain

Multi-tenant quiz platform for Uzbek university students:
- **Universities**: TMI (Toshkent Moliya Instituti), TDIU, TEAM, WIUT
- **Access model**: 15,000 UZS/semester payment via bank card receipt
- **Quiz delivery**: Telegram native polls (anonymous, immediate scoring)
- **Question modes**: Sequential (with configurable offset) + Random (Fisher-Yates)
- **Timeouts**: Per-question (configurable) + 5-minute inactivity auto-close
- **Leaderboards**: Weekly/Monthly/All-time, filtered by academic profile

---

## 📖 Documentation

- [Architecture Decision Records](docs/adr/)
- [API Specifications](docs/api/)
- [Deployment Guide](docs/guides/deployment.md)
- [Developer Guide](docs/guides/developer.md)

---

## 📜 License

MIT License — see [LICENSE](LICENSE)
