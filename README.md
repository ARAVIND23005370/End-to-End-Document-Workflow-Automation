# AI-Driven Document Workflow & Decision System

##  Project Overview
Organizations process a large number of documents for verification, compliance, and approvals. Manual handling is slow, error-prone, and lacks transparency.

This project is a **backend-first system** that automates document processing by extracting information using AI (OCR), applying company-defined rules and policies **before decision-making**, and executing workflow-based outcomes such as **approve, review, or reject**, with admin intervention when required.

---

##  Objectives
- Automate document intake and processing
- Extract structured information from documents using AI
- Apply business rules and policies before decisions
- Support system-driven and admin-assisted workflows
- Maintain audit logs for transparency and compliance

---

##  Core Features
- User and Admin role management
- Document upload and metadata storage
- AI-based OCR text extraction
- Rule-based decision engine
- Workflow routing (Auto / Manual Review)
- Admin review and final decision handling
- Audit logging for traceability

---

##  System Architecture (Backend First)
Client / Frontend
↓
Spring Boot REST APIs
↓
Rule Engine & Workflow Logic
↓
AI OCR Service (External)
↓
Relational Database


> AI is integrated as an external service.  
> Decision logic is rule-based and handled inside Spring Boot.

---

##  Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security (Role-based access)
- REST APIs

### Database
- MySQL / PostgreSQL

### AI Integration
- OCR via external AI service (Tesseract / Cloud OCR)
- Confidence-score–based processing

### Tools
- Maven
- Git & GitHub
- Postman

---

##  Project Modules

### 1️⃣ User Management
- User registration
- Role-based access (USER / ADMIN)

### 2️⃣ Document Management
- Upload documents (PDF / Image)
- Store document metadata
- Track document processing status

### 3️⃣ AI Extraction Module
- OCR-based text extraction
- Confidence score generation

### 4️⃣ Rule Engine & Workflow
- Apply company rules and policies
- Determine workflow outcome:
  - Auto decision
  - Manual admin review

### 5️⃣ Admin Review & Decision
- Admin reviews flagged documents
- Takes final decision
- Adds review comments

### 6️⃣ Audit Logging
- Track system and admin actions
- Maintain decision history with timestamps

---

##  Database Design
The system follows a relational database design including:
- Users
- Documents
- OCR Data
- Extracted Fields
- Rules
- Rule Execution
- Decisions
- Admin Reviews
- Audit Logs

Refer to the ER diagram in the `/docs` directory.

---

##  API Development Status

| Module | Status |
|------|-------|
| Project Setup |  Completed |
| Entity Design |  In Progress |
| Repositories |  Pending |
| REST APIs |  Pending |
| OCR Integration |  Pending |
| Rule Engine |  Pending |

---

##  Testing
- Unit testing using JUnit
- API testing using Postman

---

##  Future Enhancements
- Policy versioning
- Decision analytics dashboard
- Rule optimization suggestions
- Frontend integration using React
- Multi-tenant support

---

##  Project Structure

ai-document-workflow-system
│
├── src/main/java/com/project/documentworkflow
│ ├── controller
│ ├── service
│ ├── repository
│ ├── model
│ ├── config
│
├── src/main/resources
│ ├── application.yml
│
├── docs
│ ├── ER_Diagram.png
│ ├── Architecture.png
│
├── README.md
└── pom.xml


---

##  Team
- Backend Development
- Database Design
- AI Integration
- Workflow Logic
- Documentation

---

##  License
This project is developed for academic and learning purposes.




