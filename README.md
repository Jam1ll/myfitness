# MyFitness Backend

A robust backend architecture for a fitness tracking application. This project is built using a **Microservices Architecture** with **Spring Boot 4.1.x**, fully containerized, and orchestrated via **Docker Compose** for local development and **Kubernetes (K8s)** for production-grade deployments.

The primary goal of this repository is to demonstrate a scalable, resilient, secure, and highly observable backend system that implements cloud-native patterns, event-driven communication, and professional infrastructure management.

## Architecture Overview

The system is composed of several independent microservices. It utilizes both synchronous communication via **OpenFeign** (fortified with **Resilience4j**) and asynchronous, event-driven communication via **Apache Kafka**.

A single entry point is managed by a Spring Cloud API Gateway (built on WebFlux). The entire infrastructure is observable through centralized logging and distributed tracing.

### Tech Stack & Infrastructure

- **Core:** Java 21, Spring Boot 4.1, Spring Cloud (2025.1.x)
- **Databases:** PostgreSQL (Relational Data), Redis (Caching & Rate-Limiting)
- **Message Broker:** Apache Kafka (Event-Driven Architecture)
- **Orchestration & Containerization:** Docker, Docker Compose, Kubernetes
- **Observability & Tracing:** Zipkin, Micrometer (Brave), ELK Stack (Elasticsearch, Logstash, Kibana)
- **Mapping & Boilerplate:** MapStruct, Lombok
- **Security:** JWT (JSON Web Tokens), Spring Security, Google API Client

---

## Key Technical Implementations

To ensure production readiness and system resilience, the following cloud-native practices were implemented:

- **Centralized Configuration:** Managed via a Git-backed `config-server` to keep environment variables decoupled from the codebase. All services use `spring-retry` to ensure successful connection to the config server upon startup.

- **Event-Driven & Synchronous Microservices:**
  - Uses **Apache Kafka** to decouple domain boundaries (e.g., `session-service` publishing workout completion events).
  - Uses **OpenFeign** for direct service-to-service calls, wrapped with **Resilience4j** for circuit breaking and fault tolerance.

- **Reactive API Gateway:** Built with Spring WebFlux, acting as the sole entry point, handling route predicates, JWT validation, and rate-limiting via Redis.

- **Advanced Observability:** Every HTTP request and Kafka message is traced using Zipkin. Application logs are formatted in JSON using `logstash-logback-encoder` and forwarded via TCP to an ELK stack for centralized monitoring.

- **Kubernetes-Ready:** Infrastructure is fully defined in K8s manifests (Secrets, Deployments, Services, PVCs), ensuring high availability, rolling updates, and secure secret management.

---

## Microservices Breakdown

| Service | Description |
|---------|-------------|
| **`api-gateway`** | (Port 8080) Reactive entry point. Handles security, routing, and Redis-backed rate-limiting. |
| **`config-server`** | (Port 8888) Centralized configuration server fetching properties from an external Git repository. |
| **`auth-service`** | Manages user registration, Google OAuth2 integration, and generates JWTs for secure sessions. |
| **`catalog-service`** | Manages the static catalog of exercises, utilizing Redis for high-performance caching. |
| **`workout-service`** | Handles user-specific workout routines. Uses OpenFeign/Resilience4j to validate data with the catalog. |
| **`session-service`** | Manages active workout sessions (sets, reps, RPE). Publishes domain events to Kafka upon completion. |

---

## How to Run

You can run this project using either Docker Compose (recommended for rapid local development) or Kubernetes (recommended for staging/production simulation).

### Option A: Local Development via Docker Compose

#### 1. Configure Environment Variables

Ensure you have a `.env` file in the root directory:

```bash
cp .env.example .env
```

Update the `.env` file with your secure values (e.g., `DB_PASSWORD`, `JWT_SECRET_KEY`, `GOOGLE_CLIENT_ID`).

#### 2. Build and Deploy

Use Docker Compose to build the microservices and start the cluster in the correct dependency order (utilizing `service_healthy` checks):

```bash
docker compose up -d --build
```

### Option B: Deployment via Kubernetes

#### 1. Apply Secrets and Base Infrastructure

Apply your local secrets and stateful services (Databases, Message Brokers, Observability):

```bash
kubectl apply -f k8s/00-app-secrets.yaml
kubectl apply -f k8s/01-postgres-db.yaml
kubectl apply -f k8s/02-redis.yaml
kubectl apply -f k8s/04-zipkin.yaml
kubectl apply -f k8s/05-elk-stack.yaml
```

#### 2. Deploy Microservices

Deploy the configuration server first, wait for it to be ready, then deploy the rest of the services:

```bash
kubectl apply -f k8s/03-config-server.yaml

# Wait for config-server to be ready, then:

kubectl apply -f k8s/06-auth-service.yaml
kubectl apply -f k8s/07-catalog-service.yaml
kubectl apply -f k8s/08-workout-service.yaml
kubectl apply -f k8s/09-api-gateway.yaml

# Ensure session-service and Kafka manifests are applied as well.
```

#### 3. Access the API (K8s)

Use port-forwarding to access the Gateway:

```bash
kubectl port-forward deployment/api-gateway 8080:8080
```

---

## Telemetry & Monitoring Access

| Component | URL | Description |
|-----------|-----|-------------|
| **API Gateway** | `http://localhost:8080/api/v1/...` | Main API entry point. |
| **Zipkin** | `http://localhost:9411` | Distributed tracing and request visualization. |
| **Kibana** | `http://localhost:5601` | Centralized log aggregation and analysis. |
| **Kafka UI** | `http://localhost:8081` | Message broker inspection and monitoring. |
