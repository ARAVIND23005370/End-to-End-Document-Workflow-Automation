# E2EDocs — Document Workflow Automation Platform

E2EDocs is an enterprise document intake, validation, text extraction, OCR, rule evaluation, workflow automation, and audit tracking platform built with **Spring Boot 3.3.4 (Java 17)**, **PostgreSQL (Supabase)**, and **React / TypeScript**.

---

## 1. Architecture & Pipeline Overview

```text
Document Ingestion (Multipart / API)
   ↓
Magic-Byte Validation & File Storage
   ↓
Text Extraction & OCR Abstraction (PDF, DOCX, TXT, CSV, JSON)
   ↓
DocumentContext Construction
   ↓
Rule Engine (11 Operators, AND/OR Condition Groups, Priority Evaluation)
   ↓
Action Execution
   ├── SET_PRIORITY
   ├── ASSIGN_USER
   ├── START_WORKFLOW
   ├── SET_DECISION
   ├── ADD_TAG
   ├── SEND_NOTIFICATION
   ├── SEND_EMAIL
   └── FORWARD_DOCUMENT
   ↓
Workflow Progression & Step Transitions
   ↓
In-App Notifications & Email Dispatch
   ↓
Immutable Audit Trail Logging
```

---

## 2. Environment Variables & Configuration

Copy `.env.example` to `.env` to configure your environment:

```bash
cp .env.example .env
```

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `SERVER_PORT` | Backend HTTP listening port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev`, `prod`, `test`) | `prod` |
| `DB_HOST` | Supabase PostgreSQL Host / Pooler Host | `db.ytchrjgkrygvfvdzdrfh.supabase.co` |
| `DB_PORT` | PostgreSQL Port | `5432` |
| `DB_NAME` | Database Name | `postgres` |
| `DB_USERNAME` | Database Username | `postgres` |
| `DB_PASSWORD` | Database User Password | `${DB_PASSWORD}` |
| `DB_SSL_MODE` | SSL Mode (`require`, `verify-full`) | `require` |
| `JWT_SECRET` | 256-bit signing key for JWT tokens | *Strong random string* |
| `JWT_EXPIRATION_MS` | JWT validity period in milliseconds | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost:5173,http://localhost:3000` |
| `E2EDOCS_STORAGE_DIRECTORY` | Persistent file storage directory | `/app/storage/` (container) or `uploads/` (local) |
| `MAIL_HOST` | SMTP server host (e.g. Gmail / SendGrid / SES) | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port (587 STARTTLS or 465 SSL) | `587` |
| `MAIL_USERNAME` | SMTP account username / email address | `user@example.com` |
| `MAIL_PASSWORD` | SMTP password / App Password | `app-password` |
| `MAIL_FROM` | Sender email address | `no-reply@e2edocs.com` |
| `MAIL_FROM_NAME` | Sender display name | `E2EDocs Platform` |
| `E2EDOCS_SEED_ENABLED` | Enable development database seeding | `false` in production |
| `LOG_LEVEL_ROOT` | Global logging level | `INFO` |
| `LOG_LEVEL_APP` | Application logging level | `INFO` |

---

## 3. Local Development & Testing

### Backend Commands (Maven Wrapper)

```powershell
# Run unit & integration tests
.\mvnw.cmd clean test

# Clean compile backend source files
.\mvnw.cmd clean compile

# Run Spring Boot backend locally
.\mvnw.cmd spring-boot:run
```

### Frontend Commands (Node / Vite)

```bash
# Install dependencies
npm install

# Run Vite dev server
npm run dev

# Build production bundle
npm run build
```

---

## 4. Docker Containerization

### Multi-Stage Dockerfile

The multi-stage `Dockerfile` uses:
1. **Builder Stage**: `maven:3.9.9-eclipse-temurin-17-alpine` to download dependencies, compile source, and package the executable JAR.
2. **Runner Stage**: `eclipse-temurin:17-jre-alpine` with non-root user `appuser` (UID 10001), container JVM tuning (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`), persistent storage volume at `/app/storage`, and built-in Docker health probe.

### Build Docker Image

```bash
docker build -t e2edocs-backend:1.0.0 .
```

### Run Docker Container

```bash
docker run -d \
  --name e2edocs-backend \
  -p 8080:8080 \
  -e DB_HOST=db.ytchrjgkrygvfvdzdrfh.supabase.co \
  -e DB_PORT=5432 \
  -e DB_NAME=postgres \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD="your_database_password" \
  -e JWT_SECRET="your_secure_256_bit_jwt_secret_key" \
  -v e2edocs_data:/app/storage \
  e2edocs-backend:1.0.0
```

---

## 5. Docker Compose

For production-like local deployment:

```bash
# Validate compose configuration
docker compose config

# Start backend container in background
docker compose up -d

# View container logs with correlation IDs
docker compose logs -f backend

# Stop container
docker compose down
```

---

## 6. Health, Readiness & Observability Endpoints

The backend exposes Spring Boot Actuator health probes and application status endpoints:

| Endpoint | Method | Purpose | Authentication |
| :--- | :--- | :--- | :--- |
| `/api/health` | `GET` | High-level application health check | Public |
| `/actuator/health` | `GET` | Overall container health check | Public |
| `/actuator/health/liveness` | `GET` | Container liveness probe | Public |
| `/actuator/health/readiness` | `GET` | Database & subsystem readiness probe | Public |
| `/actuator/info` | `GET` | Application metadata and build info | Public |

---

## 7. Structured Logging & Request Tracing

- Every incoming HTTP request is assigned a unique `X-Correlation-ID` (or adopts incoming `X-Correlation-ID` / `X-Request-ID` headers).
- Correlation ID is injected into SLF4J MDC (`correlationId`) and emitted in structured log lines:
  ```text
  2026-10-02T15:50:00.123+00:00 INFO [http-nio-8080-exec-1] com.e2edocs.service.DocumentService [corrId=a1b2c3d4-e5f6-7890] - Uploaded document: Contract_2026.pdf
  ```
- `X-Correlation-ID` is returned in response headers for client/ingress tracing.

---

## 8. Security & Best Practices

1. **Non-Root Execution**: Container runs as dedicated unprivileged user `appuser:appgroup` (UID/GID 10001).
2. **Zero Credentials in Code**: Database passwords, JWT signing keys, and secrets are exclusively supplied via environment variables.
3. **Data Sanitization**: Global exception handlers prevent leaking database internals, SQL statements, or Java stack traces to clients.
4. **Tenant Isolation**: Multi-tenant authorization ensures users cannot access or execute actions across different organizations.
5. **Main Admin Bootstrap**: First-time registration initializes the single Main Admin account and organization; public registration is disabled post-initialization.

---

## 9. Storage & Backup Considerations

- **Container Volume**: Container mounts `/app/storage` to a persistent Docker volume or host directory.
- **Production Storage**: For cloud scale, replace local volume mount with AWS S3 / Cloud Storage / Supabase Storage via the pluggable `StorageService` interface.
