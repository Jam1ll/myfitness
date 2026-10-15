# MyFitness Backend

A robust, production-ready backend architecture for a fitness tracking application. This project is built using a **Microservices Architecture** with **Spring Boot**, fully containerized, and orchestrated via **Kubernetes**. 

The primary goal of this repository is to demonstrate a scalable, resilient, and secure backend system that goes beyond simple CRUD operations, implementing cloud-native patterns and professional infrastructure management.

## Architecture Overview

The system is composed of several independent microservices communicating synchronously via **OpenFeign**, with a single entry point managed by a Spring Cloud API Gateway. The entire infrastructure, including databases and observability tools, is orchestrated locally using Kubernetes.

### Tech Stack & Infrastructure
* **Core:** Java 21, Spring Boot 4.1.1, Spring Cloud
* **Databases:** PostgreSQL (Relational Data), Redis (Caching/Session)
* **Orchestration & Containerization:** Docker, Kubernetes (K8s)
* **Inter-Service Communication:** OpenFeign
* **Observability & Tracing:** Zipkin, ELK Stack (Elasticsearch, Logstash, Kibana)
* **Security:** JWT (JSON Web Tokens), Spring Security, Kubernetes Secrets

---

## Key Technical Implementations

To ensure production readiness and system resilience, the following cloud-native practices were implemented:

* **Centralized Configuration:** Managed via `config-server` to keep environment variables and properties decoupled from the codebase.
* **Service Resilience:** Deployed with strict **Liveness and Readiness Probes** via Spring Boot Actuator to prevent Kubernetes from routing traffic to unready or frozen pods.
* **Resource Optimization:** Explicit CPU and Memory `requests` and `limits` in K8s Deployments to prevent Java heap memory exhaustion and ensure predictable cluster performance.
* **Data Persistence:** Database storage relies on K8s **PersistentVolumeClaims (PVCs)**, ensuring no data is lost during pod lifecycle events or cluster restarts.
* **Security & Secret Management:** Sensitive data (JWT secret keys, database passwords) are entirely removed from plain text files and managed natively through **Kubernetes Secrets**, injected into containers at runtime.
* **Distributed Observability:** Every HTTP request is tagged and traced across microservices using Micrometer/Zipkin, with logs aggregated in the ELK stack for rapid debugging.

---

## Microservices Breakdown

| Service | Port (Internal) | Description |
|---------|----------------|-------------|
| **`api-gateway`** | 8080 | The single entry point for all client requests. Handles route predicates, cross-cutting concerns, and forwards traffic to the underlying services. |
| **`auth-service`** | - | Manages user registration, authentication, and generates JWTs for secure sessions. |
| **`catalog-service`** | - | Manages the static catalog of available exercises, muscle groups, and equipment. |
| **`workout-service`** | - | Handles user-specific workout routines. Communicates synchronously with the `catalog-service` via **OpenFeign** to validate exercise existence. |
| **`config-server`** | 8888 | Serves configuration properties to all microservices upon startup. |

*(Note: Internal services do not expose external NodePorts by design. External access is routed exclusively through the `api-gateway` or accessed via secure K8s `port-forward` tunnels for debugging).*

---

## How to Run Locally

This project is designed to run on a local Kubernetes cluster (e.g., Docker Desktop with K8s enabled or Minikube).

**1. Apply Infrastructure & Persistent Volumes**
```bash
# Setup database storage
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
```
**2. Configure Secrets**

Apply your local secrets to secure the database and JWT generation:

```bash
kubectl create secret generic app-secrets \
  --from-literal=SPRING_DATASOURCE_PASSWORD=your_password \
  --from-literal=JWT_SECRET_KEY=your_secure_jwt_key
```

**3. Deploy Microservices**

Deploy the configuration server, gateway, and backend services:

```bash
kubectl apply -f k8s/config-server.yaml
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/catalog-service.yaml
kubectl apply -f k8s/workout-service.yaml
kubectl apply -f k8s/api-gateway.yaml
```

**4. Access the API**

To bypass local NodePort/IPv6 routing complexities and establish a clean connection to the Gateway, use Kubernetes port-forwarding:

```bash
kubectl port-forward deployment/api-gateway 8080:8080
```
The API is now accessible at ⁠http://localhost:8080/api/v1/...⁠


