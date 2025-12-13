# Social Media Microservices Platform

## Overview

This project is a production-style microservices-based social media platform built with **Spring Boot** and **Spring Cloud**, designed to showcase modern backend architecture for recruitment and portfolio presentation.

It demonstrates real-world patterns such as service discovery, API gateway routing, centralized configuration, distributed tracing, messaging, full authentication flows, and polyglot persistence.

The system is intentionally modular, scalable, and cloud-ready, implemented as a monorepo for easier navigation during review.

---

## Architecture

The platform follows a cloud-native microservices architecture featuring:

- **API Gateway** (Spring Cloud Gateway) – centralized routing, authentication, rate limiting
- **Service Discovery** (Eureka or Consul) – dynamic service registration and lookup
- **Config Server** – centralized configuration for all services
- **Authentication Service** – JWT, OAuth2 (Google), token lifecycle management
- **User Service** – user profile/metadata management
- **Post Service** – create/edit/delete posts
- **Comment Service** – post comments, sub-comments
- **Notification Service** – event-driven notifications
- **Chat Service** – WebSocket messaging + Message Queue
- **Search Service** – Elasticsearch-based full-text search
- **MongoDB + PostgreSQL + Neo4j** – polyglot persistence demonstration
- **OpenFeign** inter-service communication – declarative REST client
- **Message Broker** (RabbitMQ or Kafka) – async communication
- **Prometheus + Grafana** – metrics & monitoring
- **OpenTelemetry (OTEL)** – distributed tracing
- **Docker & Docker Compose** – containerized deployment

---

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.x**
- **Spring Cloud 2024.x**
- **Spring Security** (JWT + OAuth2 Login)
- **OpenFeign** – declarative REST client for service-to-service communication
- **WebSocket (STOMP)**

### Databases
- **PostgreSQL** (relational)
- **MongoDB** (NoSQL)
- **Neo4j** (graph database for social relationships & profile connections)
- **Redis** (cache & WebSocket session storage)
- **Elasticsearch** (search engine)

### Messaging
- **RabbitMQ or Kafka** (configurable)

### Observability
- **Prometheus** (metrics collection)
- **Grafana** (dashboard visualization)
- **OpenTelemetry + OTEL Collector** (distributed tracing)
- **Micrometer** (metrics instrumentation)
- **ELK / OpenSearch** (optional log aggregation)

### Deployment
- **Docker / Docker Compose**
- **CI/CD Ready** (GitHub Actions recommended)

---

## Repository Structure (Monorepo)

```
/social-media-microservices
│
├── gateway/                    # API Gateway
├── config-server/              # Centralized configuration
├── discovery-server/           # Eureka/Consul
│
├── services/
│   ├── auth-service/           # JWT + OAuth2 authentication
│   ├── user-service/
│   ├── post-service/
│   ├── comment-service/
│   ├── notification-service/
│   ├── chat-service/           # WebSocket + MQ
│   └── search-service/         # Elasticsearch
│
├── infrastructure/
│   ├── docker/                 # Compose for DB, Prometheus, Grafana, MQ
│   ├── otel/                   # OTEL collector config
│   └── grafana/                # Dashboards
│
├── docs/                       # Swagger, diagrams, architecture docs
│
└── README.md
```

---

## Getting Started

### 1. Prerequisites

- **Java 21** or later
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Git**

### 2. Clone the Repository

```bash
git clone https://github.com/NgyenKhoi/social-media-microservices.git
cd social-media-microservices
```

### 3. Start Infrastructure

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

This starts:
- PostgreSQL
- MongoDB
- RabbitMQ / Kafka
- Elasticsearch
- Prometheus
- Grafana
- OTEL Collector

### 4. Start Core Cloud Components

Start services in this order:

```bash
# Config Server
cd config-server && mvn spring-boot:run

# Discovery Server
cd discovery-server && mvn spring-boot:run

# API Gateway
cd gateway && mvn spring-boot:run
```

### 5. Start Microservices

Each service can be started independently:

```bash
cd services/auth-service
mvn spring-boot:run
```

Repeat for other services in `services/` directory.

---

## Observability Dashboard

### Prometheus
- **URL:** http://localhost:9090
- Metrics collection and querying

### Grafana
- **URL:** http://localhost:3000
- **Default Credentials:**
  - Username: `admin`
  - Password: `admin`

**Pre-configured dashboards:**
- JVM Metrics
- Spring Boot Metrics
- RabbitMQ/Kafka Metrics
- Custom Service Dashboards

### Distributed Tracing (OTEL)

Integrates with:
- **Jaeger**
- **Grafana Tempo**
- **Zipkin**

---

## Authentication

The platform provides a comprehensive authentication solution:

### JWT Authentication
- Secure token-based authentication
- Token expiration and validation

### OAuth2 Google Login
- Third-party authentication integration
- Social login capability

### Token Lifecycle Management
- Access tokens
- Refresh tokens
- Token rotation
- Logout & revocation
- CSRF protection & security best practices

---

## Contribution Workflow

### Recommended Git Workflow

```
feature/xxx → pull request → main
```

**Steps:**
1. Create a feature branch: `git checkout -b feature/your-feature-name`
2. Commit your changes: `git commit -am 'Add new feature'`
3. Push to the branch: `git push origin feature/your-feature-name`
4. Submit a pull request for review

---

## License

Open-source for educational and portfolio purposes.
