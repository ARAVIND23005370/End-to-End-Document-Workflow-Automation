# DocFlow — End-to-End Document Workflow Automation

> Upload any document. Let the system extract, evaluate, decide, and track — automatically, transparently, and at scale.

---

## Table of Contents

- [Overview](#overview)
- [How It Works](#how-it-works)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Database Design](#database-design)
- [API Reference](#api-reference)
- [Decision Logic](#decision-logic)
- [Getting Started](#getting-started)
- [Development Status](#development-status)
- [Future Enhancements](#future-enhancements)
- [License](#license)

---

## Overview

Every organization — regardless of size or sector — processes documents that require verification, evaluation, and approval. Doing this manually is slow, inconsistent, and impossible to audit reliably.

**DocFlow** is a full-stack document workflow automation system that handles the entire lifecycle of a document — from the moment it is uploaded to the final decision — without manual intervention.

### What makes DocFlow different

| Traditional Approach | DocFlow |
|---|---|
| Manual review of every document | Automated evaluation against configurable rules |
| Decisions made inconsistently | Every decision follows the same rule logic |
| No audit trail | Every action logged with timestamp and reason |
| Fixed approval categories | Configurable rules for any use case |
| Rejection with no explanation | Every rejection includes the exact reason |

> **Core principle:** AI handles data extraction only. All decisions are rule-based, explainable, and fully auditable.

---

## How It Works

```
Document Upload
    │
    ▼
OCR Text Extraction  ──────────────────────  AI extracts text + confidence score
    │
    ▼
Rule & Policy Evaluation  ─────────────────  Rules evaluated in priority order (P1 → P2 → P3)
    │
    ├──▶  APPROVED  ───────────────────────  All rules passed → stored + logged
    │
    ├──▶  REJECTED  ───────────────────────  Rule failed → reason stored + logged
    │
    └──▶  REVIEW    ───────────────────────  Flagged → Admin queue → Manual decision → logged
```

---

## Features

### User Management
- User registration and authentication with JWT
- Role-based access control — `ADMIN`, `STAFF`, `VIEWER`
- Secure, stateless REST API

### Document Management
- Upload documents in any format — PDF, DOCX, PNG, JPG
- Store and track document metadata and status
- Priority tagging — High, Medium, Low
- Department assignment and folder routing

### AI OCR Extraction
- Extract raw text and structure from uploaded documents
- Generate confidence scores per document
- Ready for integration with any external OCR service

### Rule Engine
- Define unlimited custom rules per organization
- Set confidence thresholds — documents below threshold are rejected
- Priority-ordered execution — P1 rules evaluated before P2, P3
- Enable or disable individual rules without deleting them
- Every rejection records the exact rule that failed

### Document Forwarding
- Forward any document to any department or person
- Free-text destination — no fixed department list
- Full forward history tracked per document

### Admin Review
- Manual review queue for documents flagged for REVIEW
- Admin can approve, reject, or escalate with written comments
- Admin decisions override system decisions
- All overrides stored in audit trail

### Audit Logging
- Every action logged — upload, decision, forward, email, role change
- Timestamped and attributed to the user who performed it
- Filter audit log by document or view full system history
- Compliance-ready — nothing is ever deleted

### Email Notifications
- Automatic email sent to uploader when document is rejected
- User can opt in or out of notifications
- Configurable via Gmail or any SMTP provider

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Client  (React Frontend)                   │
│              localhost:5173  /  Production URL               │
└───────────────────────────────┬──────────────────────────────┘
                                │  HTTP REST + JWT
┌───────────────────────────────▼──────────────────────────────┐
│                  Spring Boot API Layer                        │
│            Controllers  ·  Services  ·  Security             │
└────────┬──────────────────┬──────────────────┬───────────────┘
         │                  │                  │
┌────────▼────────┐ ┌───────▼───────┐ ┌────────▼────────┐
│   Rule Engine   │ │  OCR Service  │ │  Audit Service  │
│  Priority-based │ │  (External)   │ │  Action Logger  │
│  decision logic │ │  AI Powered   │ │                 │
└────────┬────────┘ └───────┬───────┘ └────────┬────────┘
         │                  │                  │
┌────────▼──────────────────▼──────────────────▼────────┐
│                   Relational Database                   │
│              H2  (Dev)  ·  MySQL / PostgreSQL  (Prod)  │
└────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend
| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Persistence | Spring Data JPA |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| OCR | Apache PDFBox 2.0.30 + Tess4J 5.11.0 |
| Build | Maven |

### Frontend
| Component | Technology |
|---|---|
| Framework | React 18 + Vite |
| Styling | Inline CSS with CSS variables |
| Font | Plus Jakarta Sans + JetBrains Mono |
| HTTP | Fetch API |

### Database
| Environment | Database |
|---|---|
| Development | H2 (file-based, auto-schema) |
| Production | MySQL / PostgreSQL |

### Tools
| Purpose | Tool |
|---|---|
| API Testing | Postman |
| Version Control | Git + GitHub |
| IDE | IntelliJ IDEA |
| Package Manager | npm (frontend) |

---

## Project Structure

```
docflow/
├── src/
│   └── main/
│       ├── java/com/project/documentworkflow/
│       │   ├── config/
│       │   │   └── CorsConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── AuditController.java
│       │   │   ├── DecisionController.java
│       │   │   ├── DocumentController.java
│       │   │   ├── DocumentForwardController.java
│       │   │   ├── FileUploadController.java
│       │   │   ├── RuleController.java
│       │   │   └── UserController.java
│       │   ├── dto/
│       │   │   ├── ApiResponse.java
│       │   │   ├── DecisionHistoryResponse.java
│       │   │   ├── DecisionRequest.java
│       │   │   ├── EmailPreferenceRequest.java
│       │   │   ├── ForwardRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   └── UploadResponse.java
│       │   ├── exception/
│       │   │   ├── DocumentNotFoundException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── model/
│       │   │   ├── AuditLog.java
│       │   │   ├── Decision.java
│       │   │   ├── Document.java
│       │   │   ├── DocumentForward.java
│       │   │   ├── OCRData.java
│       │   │   ├── Rule.java
│       │   │   └── User.java
│       │   ├── repository/
│       │   │   ├── AuditLogRepository.java
│       │   │   ├── DecisionRepository.java
│       │   │   ├── DocumentForwardRepository.java
│       │   │   ├── DocumentRepository.java
│       │   │   ├── OCRDataRepository.java
│       │   │   ├── RuleRepository.java
│       │   │   └── UserRepository.java
│       │   ├── security/
│       │   │   ├── JwtFilter.java
│       │   │   ├── JwtUtil.java
│       │   │   └── SecurityConfig.java
│       │   └── service/
│       │       ├── AuditService.java
│       │       ├── DecisionEngineService.java
│       │       ├── DecisionService.java
│       │       ├── DocumentForwardService.java
│       │       ├── DocumentService.java
│       │       ├── DocumentWorkflowService.java
│       │       ├── EmailNotificationService.java
│       │       ├── JwtService.java
│       │       ├── RuleService.java
│       │       └── UserService.java
│       └── resources/
│           └── application.properties
├── docflow-frontend/
│   └── src/
│       └── DocflowApp.jsx
├── docs/
│   ├── ER_Diagram.png
│   └── Architecture.png
├── pom.xml
└── README.md
```

---

## Database Design

```
User ────────────────── uploads ──────────────── Document
 │                                                   │
 │                                              ┌────┴─────┐
 │                                           OCRData    Decision
 │                                                          │
 └──── AuditLog ◄──── all actions             Rule ────────┘
                                           (evaluated against)
                      DocumentForward ◄── Document
```

| Entity | Key Fields |
|---|---|
| `User` | id, name, email, password, role, emailNotifyOnReject |
| `Document` | documentId, fileName, status, priority, department, folderPath, uploadedByEmail |
| `OCRData` | id, extractedText, confidenceScore, document |
| `Rule` | ruleId, ruleName, conditionDescription, thresholdValue, priority, active |
| `Decision` | decisionId, decisionType, decisionSource, decisionReason, decisionTime |
| `DocumentForward` | id, documentId, forwardedTo, forwardedBy, forwardType, note, forwardedAt |
| `AuditLog` | id, action, documentId, performedBy, details, actionTime |

---

## API Reference

### Authentication — Public

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT token |

### Documents

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/upload` | ADMIN, STAFF | Upload and process a document |
| `GET` | `/api/documents/{id}` | Any | Get document details |

### Rules

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/rules` | Any | List all rules |
| `POST` | `/api/rules` | ADMIN, STAFF | Create a new rule |
| `DELETE` | `/api/rules/{id}` | ADMIN, STAFF | Delete a rule |
| `PUT` | `/api/rules/{id}/toggle` | ADMIN, STAFF | Enable or disable a rule |

### Decisions

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/decisions` | Any | List all decisions |
| `GET` | `/api/decisions/{id}` | Any | Get decision with rejection reason |

### Document Forwarding

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/forward` | ADMIN, STAFF | Forward document to any destination |
| `GET` | `/api/forward/{docId}` | Any | Get forward history for a document |
| `GET` | `/api/forward` | Any | List all forward records |

### Audit

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/audit` | ADMIN, STAFF | Full system audit log |
| `GET` | `/api/audit/{docId}` | Any | Audit trail for a specific document |

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users` | ADMIN | List all users |
| `GET` | `/api/users/me` | Any | Get current user profile |
| `PUT` | `/api/users/{id}/role` | ADMIN | Update a user's role |
| `PUT` | `/api/users/email-preference` | Any | Toggle email notifications |

---

### Example — Upload and Get Decision

**Upload:**
```http
POST /api/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <your-document>
```

**Decision Response — Approved:**
```json
{
  "success": true,
  "data": {
    "documentId": 5,
    "decision": "APPROVED",
    "status": "COMPLETED",
    "folderPath": "uploads/approved"
  }
}
```

**Decision Response — Rejected:**
```json
{
  "success": true,
  "data": {
    "documentId": 6,
    "decision": "REJECTED",
    "decisionSource": "FAILED_RULE: Minimum Quality Check",
    "decisionReason": "OCR confidence 58% is below required threshold of 80%",
    "status": "COMPLETED"
  }
}
```

---

## Decision Logic

```
On document upload:

  1. OCR service extracts text → confidence score generated

  2. Active rules loaded and sorted by priority (P1 first)

  3. For each rule:
       IF confidence score < rule threshold
           → REJECTED
              Reason: "Failed rule: <name> — score <x>% below <threshold>%"
              Stop. No further rules evaluated.

  4. If all rules pass:
       → APPROVED

  5. If flagged for human review:
       → REVIEW
          Enters admin queue
          Admin approves or rejects with comment
          Audit log updated with final decision

  6. All outcomes stored with full context in AuditLog
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+ (for frontend)
- IntelliJ IDEA (recommended)

### Run Backend

```powershell
cd path/to/docflow
mvn spring-boot:run
```

API available at: `http://localhost:8080`
H2 Console (dev): `http://localhost:8080/h2-console`

### Run Frontend

```powershell
cd docflow-frontend
npm install
npm run dev
```

Frontend available at: `http://localhost:5173`

### Configuration

`src/main/resources/application.properties`:

```properties
server.port=8080

spring.datasource.url=jdbc:h2:file:./documentworkflow_db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

jwt.secret=YourSecureSecretKeyHere
```

---

## Development Status

| Module | Status |
|---|---|
| Project setup & configuration | ✅ Complete |
| Entity & database design | ✅ Complete |
| JWT authentication & security | ✅ Complete |
| Role-based access control | ✅ Complete |
| Document upload & management | ✅ Complete |
| OCR integration | ✅ Complete |
| Rule engine with priority ordering | ✅ Complete |
| System decision engine | ✅ Complete |
| Document forwarding | ✅ Complete |
| Email notifications on reject | ✅ Complete |
| Audit logging | ✅ Complete |
| React frontend | ✅ Complete |
| Admin review module | 🔄 In Progress |
| Production database migration | 🔄 In Progress |

---

## Future Enhancements

- **Policy versioning** — track and rollback rule changes over time
- **Decision analytics dashboard** — approval rates, rejection trends, processing time metrics
- **Rule optimization engine** — AI-assisted threshold tuning based on historical data
- **Multi-tenant support** — isolated workspaces per organization
- **Webhook integration** — push decisions to external systems in real time
- **Export reports** — audit logs and decision history as Excel or PDF
- **Mobile application** — document upload and status tracking on mobile

---

## License

This project is developed for academic and learning purposes.

---

<div align="center">
  <strong>DocFlow</strong> · End-to-End Document Workflow Automation<br/>
  <sub>Built with Spring Boot + React · Decisions that are transparent, explainable, and auditable by design</sub>
</div>
