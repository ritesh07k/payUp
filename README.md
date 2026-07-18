# PayUp

A Razorpay-inspired payment gateway built as a microservices system, with a React dashboard on top. Built as a portfolio project to demonstrate backend architecture, event-driven design, observability, and full-stack integration.

## Overview

PayUp lets a merchant create orders, capture payments against them, issue refunds, and register webhook endpoints to receive event notifications — the core primitives of a real payment gateway. It started as a Spring Boot monolith and was migrated into six independently deployable microservices communicating over REST and Kafka.

## Architecture

```
                        ┌─────────────────┐
                        │   React SPA      │
                        │  (Vite, 5173)    │
                        └────────┬─────────┘
                                 │
                        ┌────────▼─────────┐
                        │   API Gateway     │
                        │ Spring Cloud      │
                        │ Gateway (8080)    │
                        │ JWT auth + CORS   │
                        └────────┬─────────┘
           ┌──────────┬──────────┼──────────┬──────────┐
           ▼          ▼          ▼          ▼          ▼
      ┌────────┐ ┌─────────┐┌─────────┐┌─────────┐┌──────────┐
      │  Auth   │ │Merchant ││ Payment ││ Refund  ││ Webhook  │
      │ (8081)  │ │ (8082)  ││ (8083)  ││ (8084)  ││ (8085)   │
      └────┬────┘ └────┬────┘└────┬────┘└────┬────┘└────┬─────┘
           │           │          │          │          │
           └───────────┴──────────┴──────────┴──────────┘
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
              ┌──────────┐ ┌──────────┐ ┌──────────┐
              │PostgreSQL │ │  Kafka    │ │  Redis    │
              │(db/service)│ │           │ │           │
              └──────────┘ └──────────┘ └──────────┘

  Observability: Zipkin (tracing) · Prometheus (metrics) · Grafana (dashboards)
```

Each service owns its own PostgreSQL database (database-per-service pattern). Services communicate synchronously via REST (e.g. refund-service calling payment-service to validate a payment) and asynchronously via Kafka (e.g. payment-events, refund-events consumed by webhook-service to trigger notifications).

## Services

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 8080 | Single entry point, JWT validation, request routing, CORS |
| `auth-service` | 8081 | Signup, login, JWT issuance/refresh, Redis-backed token blacklist |
| `merchant-service` | 8082 | Merchant profile management, consumes `MerchantCreatedEvent` |
| `payment-service` | 8083 | Orders, payment capture, ledger entries; produces `payment-events`, consumes `refund-events` |
| `refund-service` | 8084 | Refund processing; calls payment-service via REST for validation, produces `refund-events` |
| `webhook-service` | 8085 | Webhook endpoint registration, HMAC-signed delivery with retry/backoff/DLQ |
| `common` | — | Shared module: base entities, API response wrapper, global exception handling |

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.4.5, Maven (multi-module)
- Spring Cloud Gateway (reactive, WebFlux)
- Spring Data JPA, PostgreSQL (one database per service)
- Apache Kafka (event-driven communication between services)
- Redis (JWT refresh token storage, token blacklist, rate limiting)
- Docker Compose (local infrastructure)

**Frontend**
- Vite + React (JavaScript)
- Tailwind CSS v4
- React Router v6
- Axios
- Context API (auth state)

**Observability**
- Zipkin — distributed tracing across all six services
- Prometheus — metrics scraping via Spring Actuator
- Grafana — dashboards on top of Prometheus

## Key Features

- **JWT authentication** with refresh token rotation (Redis-backed) and a token blacklist for logout/revocation
- **Idempotent payment capture and refunds** — client-supplied idempotency keys prevent duplicate charges/refunds on retry
- **Event-driven architecture** — Kafka producers/consumers decouple services; each service defines its own event DTOs with `spring.json.type.mapping` aliases for cross-service deserialization
- **Webhook delivery system** — HMAC-SHA256 signed payloads, AES-256-GCM encrypted webhook secrets at rest, exponential backoff retry (1min → 5min → 30min → dead-letter queue)
- **Rate limiting** — IP-based, enforced at the gateway
- **Full distributed tracing** — a single request's trace ID is visible end-to-end across api-gateway → payment-service → Kafka → webhook-service in Zipkin
- **Dashboard** — merchant-facing UI to browse orders/payments/refunds/webhooks, create orders, capture payments, and issue refunds, with live aggregate metrics (total volume, success rate, active webhooks)

## Security Notes

- Every service scans `com.payup` base package so the shared `GlobalExceptionHandler` is picked up consistently
- Actuator endpoints are explicitly permitted per service for Prometheus scraping without exposing the rest of the API
- API Gateway's `JwtAuthGlobalFilter` bypasses `OPTIONS` requests so CORS preflight isn't blocked by auth
- Webhook secrets are shown once at registration time (never stored or logged in plaintext) and encrypted at rest with AES-256-GCM

## Known Limitations

- Scoped to INR only — multi-currency aggregation is a possible future extension
- Partial refunds are supported by the API but the frontend currently treats any refund as marking a payment fully "refunded" in the UI
- No automated test suite yet — all endpoints were verified manually via curl and the frontend

## Running Locally

This project was built and tested inside GitHub Codespaces, but runs the same way on any machine with Docker and Java installed.

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 18+ and npm
- Docker and Docker Compose

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd payUp
```

### 2. Set up environment variables

Create a `.env` file in the project root (this is gitignored — never commit it) with the following variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payup
DB_USER=payup_user
DB_PASSWORD=<your-db-password>

REDIS_HOST=localhost
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=localhost:9092

JWT_SECRET_KEY=<your-jwt-secret-key>
JWT_EXPIRATION=86400000

WEBHOOK_ENCRYPTION_KEY=<your-webhook-encryption-key>

SHOW_SQL=true
```

Generate strong random values for `JWT_SECRET_KEY` and `WEBHOOK_ENCRYPTION_KEY` — don't reuse example values from any documentation, and never commit this file.

Source it in every terminal you use for backend work:

```bash
set -a; source .env; set +a
```

### 3. Start infrastructure

```bash
docker compose up -d
```

This brings up PostgreSQL, Redis, Kafka, Zookeeper, Zipkin, Prometheus, and Grafana.

Verify everything is healthy:

```bash
docker ps
```

### 4. Build and run the backend services

From the project root, build all modules:

```bash
mvn clean install -DskipTests
```

Then start each service in its own terminal (remember to source `.env` in each one):

```bash
cd auth-service && mvn spring-boot:run
cd merchant-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd refund-service && mvn spring-boot:run
cd webhook-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

Confirm the gateway is routing correctly:

```bash
curl -s http://localhost:8080/api/v1/auth/login \
  -X POST -H "Content-Type: application/json" \
  -d '{"email":"test@payup.com","password":"yourpassword"}'
```

### 5. Set up and run the frontend

```bash
cd frontend
npm install
```

Create `frontend/.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Start the dev server:

```bash
npm run dev
```

Visit `http://localhost:5173` in your browser.

### 6. (Optional) View observability dashboards

- Zipkin: `http://localhost:9411`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

### Notes for Codespaces users

If running in GitHub Codespaces instead of locally:
- Set forwarded ports 8080 (gateway) and 5173 (frontend) to **Public** visibility, otherwise the browser can't reach them
- Use the actual `https://*.app.github.dev` forwarded URL in `frontend/.env` — `localhost` won't resolve correctly from the browser tab
- The forwarded URL changes if the Codespace rebuilds, so `VITE_API_BASE_URL` may need updating after a rebuild
