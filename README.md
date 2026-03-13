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
- [Supported Document Types](#supported-document-types)
- [Rule Engine](#rule-engine)
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
| Fixed approval categories | Configurable rules per document type |
| Rejection with no explanation | Every rejection includes the exact reason |
| One rule applies to all documents | Different rules per document type (Medical, Legal, HR...) |
| Keywords hardcoded in code | Admin defines required keywords in Rule Setup UI |

> **Core principle:** AI handles data extraction only. All decisions are rule-based, explainable, and fully auditable.

---

## How It Works

```
Document Upload (PDF / DOCX / TXT)
    │
    ▼
Document Type Detection  ──────────────  Auto-detects: LOAN, MEDICAL, STUDENT, HR, LEGAL...
    │
    ▼
OCR Text Extraction  ──────────────────  Extracts text + calculates confidence score
    │                                    (PDFBox for PDF, plain text for TXT/DOCX)
    ▼
Rule & Keyword Evaluation  ────────────  Rules filtered by document type → evaluated in priority order
    │                                    Confidence threshold check → Keyword presence check
    │
    ├──▶  APPROVED  ───────────────────  All rules passed + all keywords found → logged
    │
    ├──▶  REJECTED  ───────────────────  Rule failed or keyword missing → reason stored + logged
    │
    └──▶  REVIEW    ───────────────────  Borderline score (30–70%) → Admin queue → Manual decision
```

---

## Features

### User Management
- User registration and authentication with JWT
- Role-based access control — `ADMIN`, `STAFF`, `VIEWER`
- Secure, stateless REST API

### Document Management
- Upload documents — **PDF, DOCX, TXT**
- **Apache PDFBox** integration — reads actual text from PDF files
- Auto-detect document type from content and filename
- Store and track document metadata and status
- Priority tagging — High, Medium, Low
- Department assignment and folder routing

### AI OCR Extraction
- Extract raw text from uploaded documents
- **Smart confidence scoring** — calculated from 10 document-specific checks
- **Different scoring logic per document type** — Medical, Student, HR, Legal each have their own criteria
- Ready for integration with any external OCR service

### Rule Engine ⭐ New
- Define unlimited custom rules per organization
- **Document Type selector** — each rule applies to one specific document type OR all types
- **Required Keywords box** — admin enters comma-separated keywords; ALL must be present to pass
- Live keyword preview — tags appear as you type
- Set confidence thresholds — documents below threshold are rejected
- Priority-ordered execution — P1 rules evaluated before P2, P3
- Enable or disable individual rules without deleting them
- Every rejection records the exact rule and keyword that failed

### Document Forwarding
- Forward any document to any department or person
- Free-text destination — no fixed department list
- Full forward history tracked per document

### Admin Review
- Manual review queue for borderline documents (30–70% confidence)
- Admin can approve, reject, or escalate with written comments
- All overrides stored in audit trail

### Audit Logging
- Every action logged — upload, decision, forward, email, role change
- Shows file name, document type, and confidence % on upload
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
│  Type-filtered  │ │  PDFBox +     │ │  Action Logger  │
│  Keyword check  │ │  Smart Score  │ │                 │
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
| PDF Reading | Apache PDFBox 2.0.30 |
| OCR | Tess4J 5.11.0 |
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
│       │   ├── model/
│       │   │   ├── AuditLog.java
│       │   │   ├── Decision.java
│       │   │   ├── Document.java
│       │   │   ├── DocumentForward.java
│       │   │   ├── OCRData.java
│       │   │   ├── Rule.java          ← requiredKeywords + documentType fields added
│       │   │   └── User.java
│       │   ├── service/
│       │   │   ├── DecisionEngineService.java   ← keyword check + type filtering
│       │   │   ├── DocumentWorkflowService.java ← PDFBox + smart confidence scoring
│       │   │   └── ...
│       │   └── resources/
│       │       └── application.properties
├── docflow-frontend/
│   └── src/
│       └── DocflowApp.jsx    ← keyword box + document type selector in Rule Setup
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
| `Document` | documentId, fileName, documentType, status, priority, folderPath, uploadedByEmail |
| `OCRData` | id, extractedText, confidenceScore, document |
| `Rule` | ruleId, ruleName, conditionDescription, thresholdValue, priority, active, **documentType**, **requiredKeywords** |
| `Decision` | decisionId, decisionType, decisionSource, decisionReason, decisionTime |
| `DocumentForward` | id, documentId, forwardedTo, forwardedBy, forwardType, note, forwardedAt |
| `AuditLog` | id, action, documentId, performedBy, details, actionTime |

---

## Supported Document Types

DocFlow automatically detects the document type from content and filename, then applies type-specific rules and confidence scoring.

| Type | Auto-detected From | Confidence Checks Include |
|---|---|---|
| `LOAN` | loan, pan, aadhaar, income | name, PAN, income, amount, signature |
| `COMPLAINT` | complaint, grievance | name, date, issue, reference, signature |
| `INVOICE` | invoice, gst, bill to | invoice no, GST, amount, vendor, items |
| `MEDICAL` | patient, doctor, hospital, diagnosis | patient, doctor, diagnosis, medicine, test |
| `STUDENT` | student, college, cgpa, marks | register no, college, course, marks, HOD |
| `HR` | employee, designation, salary, ctc | designation, department, salary, joining, PAN |
| `LEGAL` | court, advocate, petition, plaintiff | court, case, advocate, clause, witness |
| `CONTRACT` | contract, agreement, parties | parties, terms, payment, duration, witness |
| `IDENTITY` | aadhaar, passport, pan | name, DOB, address, gender, issued |
| `FINANCIAL` | financial, balance sheet | — |
| `GENERAL` | everything else | name, date, address, signature |

---

## Rule Engine

### How rules work

Each rule has:
- **Rule Name** — descriptive label
- **Threshold** — documents below this confidence % fail this rule
- **Priority** — rules are checked in order (1 = first)
- **Document Type** — rule only applies to this type (`ALL` applies to everything)
- **Required Keywords** — ALL listed keywords must be present in the document text

### Evaluation order

```
1. Load all active rules
2. Filter rules matching document type (or ALL)
3. For each rule in priority order:
   a. Check confidence score ≥ threshold
   b. Check ALL required keywords are present in extracted text
   c. If either check fails → REJECTED (with exact reason)
4. If all rules pass → APPROVED
5. If confidence is borderline (30–70%) → REVIEW
```

### Example rules

| Rule Name | Type | Threshold | Keywords |
|---|---|---|---|
| Medical Document Check | MEDICAL | 70% | patient, doctor, hospital |
| Student Document Check | STUDENT | 70% | student, college, register |
| HR Document Check | HR | 70% | employee, designation, salary |
| Legal Document Check | LEGAL | 70% | court, advocate, petition |
| General Quality Check | ALL | 30% | *(none)* |

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
| `POST` | `/api/rules` | ADMIN, STAFF | Create rule with type + keywords |
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
    "status": "APPROVED"
  }
}
```

**Decision Response — Rejected (threshold):**
```json
{
  "success": true,
  "data": {
    "documentId": 6,
    "decision": "REJECTED",
    "decisionSource": "FAILED_RULE: Priority 1 — Medical Document Check",
    "decisionReason": "Confidence score 48% is below required threshold of 70%",
    "status": "REJECTED"
  }
}
```

**Decision Response — Rejected (missing keyword):**
```json
{
  "success": true,
  "data": {
    "documentId": 7,
    "decision": "REJECTED",
    "decisionSource": "FAILED_RULE: Priority 1 — Medical Document Check",
    "decisionReason": "Required keyword 'hospital' not found in document. All keywords must be present: [patient, doctor, hospital]",
    "status": "REJECTED"
  }
}
```

---

## Decision Logic

```
On document upload:
  1. Document type auto-detected from content + filename
  2. OCR text extracted (PDFBox for PDF, plain text for TXT/DOCX)
  3. Confidence score calculated using 10 type-specific checks
  4. Active rules loaded, filtered by document type, sorted by priority
  5. For each applicable rule:
       a. IF confidence score < rule threshold → REJECTED
       b. IF any required keyword missing from text → REJECTED
          Reason stored: exact rule name + keyword that failed
          Stop. No further rules evaluated.
  6. If all rules pass → APPROVED
  7. If score is borderline (30-70%) and threshold failed → REVIEW
       Enters admin queue for manual decision
  8. All outcomes stored with full context in AuditLog
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
cd path/to/documentworkflow
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

### Test Documents
Create these in Notepad and upload to test all 3 decisions:

**reject.txt** — empty form (score ~10%) → ❌ REJECTED
```
form
name:
date:
```

**review.txt** — partial content (score ~40-55%) → 🔄 REVIEW
```
COMPLAINT FORM
Name: Priya Sharma
Date: 05/03/2024
Complaint: ATM issue Chennai
```

**approve.txt** — complete content (score ~80%+) → ✅ APPROVED
```
LOAN APPLICATION FORM
Applicant Name: Rajesh Kumar
Date of Birth: 15/08/1990
PAN Number: ABCDE1234F
Mobile: 9876543210
Email: rajesh@gmail.com
Address: 123 Anna Nagar Chennai
Loan Amount: Rs. 500000
Signature: Rajesh Kumar
Date: 10/03/2024
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
| PDF text extraction (PDFBox) | ✅ Complete |
| Smart confidence scoring per document type | ✅ Complete |
| Auto document type detection | ✅ Complete |
| Rule engine with priority ordering | ✅ Complete |
| Document type filter per rule | ✅ Complete |
| Required keywords per rule | ✅ Complete |
| APPROVE / REVIEW / REJECT decision engine | ✅ Complete |
| Document forwarding | ✅ Complete |
| Email notifications on reject | ✅ Complete |
| Audit logging | ✅ Complete |
| React frontend with keyword rule UI | ✅ Complete |
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
- **Real OCR engine** — Tesseract / Google Vision for scanned image documents

---

## License

This project is developed for academic and learning purposes.

---

<div align="center">
  <strong>DocFlow</strong> · End-to-End Document Workflow Automation<br/>
  <sub>Built with Spring Boot + React · PDF extraction with PDFBox · Keyword-based rules per document type · Decisions that are transparent, explainable, and auditable by design</sub>
</div>
