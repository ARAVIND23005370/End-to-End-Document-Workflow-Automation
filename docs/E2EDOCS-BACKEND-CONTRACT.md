# E2EDocs Backend Integration Contract

> **Document Version**: 1.0.0 — Frozen Frontend Contract  
> **Platform**: E2EDocs — Document Workflow Automation Platform  
> **Author / Credit**: Developed by AravindRamesh  
> **Source of Truth**: Current E2EDocs React/TypeScript Frontend Codebase (`src/`)

---

## 1. Authentication Contract

### Endpoints

| Method | Endpoint | Purpose | Request Body | Response (200 OK) | Auth Req |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/signup/status` | Check if initial Main Admin bootstrap signup is available | None | `SignupStatusResponse` (`{ "available": boolean }`) | Public |
| `POST` | `/api/auth/register` | Bootstrap registration for first initial Main Admin & Org (available only before system is initialized) | `RegisterRequest` | `AuthResponse` | Public |
| `POST` | `/api/auth/login` | Authenticate user | `LoginRequest` | `AuthResponse` | Public |
| `POST` | `/api/auth/logout` | Invalidate session | None | `200 OK` / `204 No Content` | Bearer Token |
| `GET` | `/api/auth/me` | Fetch active profile | None | `User` | Bearer Token |
| `POST` | `/api/auth/forgot-password` | Request password reset email | `ForgotPasswordRequest` | `MessageResponse` | Public |
| `POST` | `/api/auth/reset-password` | Reset password using token | `ResetPasswordRequest` | `MessageResponse` | Public |

### Schemas

#### `SignupStatusResponse`
```json
{
  "available": true
}
```

#### `RegisterRequest` (Bootstrap Only)
> **Note**: This endpoint is only available when no users exist in the system (fresh deployment). The first registered user automatically becomes `ADMIN` and creates the primary organization. After system initialization, public registration is disabled, and additional users must be invited via `POST /api/users/invite` or `POST /api/users`.
```json
{
  "name": "Sarah Chen",
  "email": "sarah.chen@example.com",
  "password": "StrongPassword123!",
  "organizationName": "Acme Global Enterprises"
}
```

#### `LoginRequest`
```json
{
  "email": "user@example.com",
  "password": "plain-text-password"
}
```

#### `AuthResponse`
```json
{
  "user": {
    "id": "u-001",
    "name": "Sarah Chen",
    "email": "sarah.chen@example.com",
    "role": "super_admin",
    "department": "Engineering & Product",
    "departmentId": "dept-006",
    "status": "active",
    "avatar": null,
    "lastActive": "2026-10-02T09:30:00Z",
    "createdAt": "2025-03-15T10:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### `ForgotPasswordRequest`
```json
{
  "email": "user@example.com"
}
```

#### `ResetPasswordRequest`
```json
{
  "token": "reset-token-string",
  "password": "new-password"
}
```

#### Token Handling
- **Type**: Standard JWT Bearer token.
- **Header**: `Authorization: Bearer <token>`.
- **Session Behavior**: Client stores token in memory/state and attaches header to all authenticated requests.

---

## 2. Document Contract

### Endpoints

| Method | Endpoint | Purpose | Params / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/documents` | List & filter documents | Query: `search`, `status`, `priority`, `source`, `department`, `page`, `pageSize`, `sortBy`, `sortOrder` | `{ "data": Document[], "total": number }` |
| `GET` | `/api/documents/export` | Export documents metadata as CSV | Query: `search`, `status`, `priority`, `source`, `department` | CSV File Stream (`text/csv; charset=UTF-8`, `Content-Disposition: attachment; filename="e2edocs-documents.csv"`) |
| `GET` | `/api/documents/{id}` | Get document details | Path: `id` | `DocumentDetail` |
| `POST` | `/api/documents` | Upload / ingest document | Multipart (`file`, optional metadata) | `Document` |
| `PUT` | `/api/documents/{id}` | Update document metadata | JSON: `Partial<Document>` | `Document` |
| `DELETE` | `/api/documents/{id}` | Delete document | Path: `id` | `204 No Content` |
| `GET` | `/api/documents/{id}/download` | Download raw file binary | Path: `id` | Binary Stream (`application/pdf`, etc.) |
| `POST` | `/api/documents/{id}/send-email` | Manually email document to recipient with stored attachment | Path: `id`, JSON: `DocumentSendEmailRequest` | `MessageResponse` (`{ "message": string }`) |

### Schemas

#### `DocumentSendEmailRequest`
```json
{
  "recipient": "person@example.com",
  "subject": "Document: Contract_2026_014.pdf",
  "message": "Please find attached the document for your review."
}
```

#### `Document`
```json
{
  "id": "DOC-2026-0847",
  "name": "Contract_2026_014.pdf",
  "description": "Service agreement requiring review.",
  "originalSender": {
    "email": "contracts@example.org",
    "name": "Alex Vance"
  },
  "type": "Service Agreement",
  "status": "review",
  "priority": "high",
  "source": "email",
  "department": "Legal & Contracts",
  "departmentId": "dept-003",
  "assignedTo": "James Okafor",
  "assignedToId": "u-004",
  "createdAt": "2026-09-28T10:00:00Z",
  "updatedAt": "2026-10-01T14:30:00Z",
  "size": 2457600,
  "tags": ["contract", "service-agreement", "external"],
  "ruleMatches": 3,
  "workflowId": "wf-002",
  "metadata": {
    "recipient_email": "inbox@example.com",
    "subject": "Updated Service Agreement 2026-014"
  }
}
```

#### `DocumentDetail` (Extends `Document`)
```json
{
  "id": "DOC-2026-0847",
  "name": "Contract_2026_014.pdf",
  "description": "Service agreement requiring review.",
  "originalSender": {
    "email": "contracts@example.org",
    "name": "Alex Vance"
  },
  "type": "Service Agreement",
  "status": "review",
  "priority": "high",
  "source": "email",
  "department": "Legal & Contracts",
  "departmentId": "dept-003",
  "assignedTo": "James Okafor",
  "assignedToId": "u-004",
  "createdAt": "2026-09-28T10:00:00Z",
  "updatedAt": "2026-10-01T14:30:00Z",
  "size": 2457600,
  "tags": ["contract", "service-agreement", "external"],
  "ruleMatches": 3,
  "workflowId": "wf-002",
  "metadata": {
    "recipient_email": "inbox@example.com",
    "subject": "Updated Service Agreement 2026-014"
  },
  "contentPreview": "This Master Services Agreement (\"Agreement\") is entered into...",
  "detectedInfo": [
    { "label": "Document Type", "value": "Service Agreement", "confidence": 0.98 },
    { "label": "Original Sender Email", "value": "contracts@example.org" },
    { "label": "Original Sender Name", "value": "Alex Vance" },
    { "label": "Source Channel", "value": "Email Ingestion" }
  ],
  "ruleEvaluations": [
    {
      "ruleId": "r-001",
      "ruleName": "External Email Ingestion Routing",
      "matched": true,
      "evaluatedAt": "2026-09-28T10:01:00Z",
      "conditionsChecked": 2,
      "actionsTriggered": ["Set Priority: High", "Assign to James Okafor"]
    }
  ],
  "auditHistory": [
    {
      "id": "a-001",
      "timestamp": "2026-10-01T14:30:00Z",
      "userId": "u-004",
      "userName": "James Okafor",
      "action": "reviewed",
      "resource": "Document",
      "resourceId": "DOC-2026-0847",
      "status": "success",
      "details": "Initial legal review completed"
    }
  ],
  "notifications": []
}
```

---

## 3. Rule Engine Contract

### Endpoints

| Method | Endpoint | Purpose | Params / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/rules` | List all rules | None | `Rule[]` |
| `GET` | `/api/rules/{id}` | Get rule by ID | Path: `id` | `Rule` |
| `POST` | `/api/rules` | Create new rule | `RuleInput` | `Rule` |
| `PUT` | `/api/rules/{id}` | Update existing rule | Path: `id`, Body: `RuleInput` | `Rule` |
| `DELETE` | `/api/rules/{id}` | Delete rule | Path: `id` | `204 No Content` |

### `Rule` Schema
```json
{
  "id": "r-001",
  "name": "External Email Ingestion Routing",
  "description": "Matches incoming emails from partner domains and sets priority and assignment.",
  "status": "active",
  "evaluationOrder": 10,
  "conditionGroups": [
    {
      "id": "cg-001",
      "logic": "AND",
      "conditions": [
        {
          "id": "c-001",
          "field": "metadata.source",
          "operator": "equals",
          "value": "email"
        },
        {
          "id": "c-002",
          "field": "sender.email",
          "operator": "ends_with",
          "value": "@example.org"
        }
      ]
    }
  ],
  "actions": [
    {
      "id": "act-001",
      "type": "set_priority",
      "value": "high"
    },
    {
      "id": "act-002",
      "type": "assign_user",
      "value": "James Okafor"
    },
    {
      "id": "act-003",
      "type": "send_email",
      "value": "Acknowledge Receipt",
      "emailConfig": {
        "recipientType": "original_sender",
        "subject": "Receipt confirmation for {{document.name}}",
        "message": "Your document has been received and queued for review."
      }
    }
  ],
  "createdAt": "2026-06-15T10:00:00Z",
  "updatedAt": "2026-09-01T14:00:00Z",
  "createdBy": "Sarah Chen",
  "matchCount": 47,
  "lastTriggered": "2026-10-01T12:00:00Z"
}
```

---

## 4. Rule Conditions Contract

### Condition Fields
The frontend rule builder supports the following exact fields:
1. `document.name` — Document Name
2. `document.type` — Document Type / Category
3. `document.description` — Document Description
4. `document.content` — Document Full Text Content
5. `sender.email` — Original Sender Email Address
6. `sender.name` — Original Sender Name
7. `metadata.recipient_email` — Destination Email
8. `metadata.subject` — Ingestion Subject
9. `metadata.source` — Ingestion / Upload Source (`email`, `manual_upload`, `api`, `integration`, `scanned`)
10. `file.extension` — File Extension (`.pdf`, `.docx`, etc.)
11. `file.size` — File Size (bytes)
12. `document.priority` — Priority (`critical`, `high`, `medium`, `low`)
13. `document.department` — Department
14. `document.tags` — Tag strings
15. `extracted.text` — Extracted OCR / parse text
16. `extracted.category` — Inferred category
17. `metadata.custom` — Custom metadata field

### Supported Operators
- `equals`: Exact equality
- `not_equals`: Inequality
- `contains`: Substring match
- `not_contains`: Negative substring match
- `starts_with`: Prefix match
- `ends_with`: Suffix match
- `matches`: Regex pattern match
- `greater_than`: Numeric / date comparison
- `less_than`: Numeric / date comparison
- `in`: Membership in set / comma-separated list
- `not_in`: Exclusion from set

### Group & Logic Evaluation
- Rules evaluate sequentially according to `evaluationOrder` (ascending: 1, 2, 5, 10...).
- `conditionGroups` are evaluated with top-level `OR` logic between groups.
- Inside each `ConditionGroup`, conditions are evaluated using the group's `logic` (`AND` or `OR`).

---

## 5. Rule Actions Contract

### Action Types (`ActionType`)
1. `set_priority` — Assigns document priority (`critical`, `high`, `medium`, `low`).
2. `assign_user` — Assigns document to a specific user name or ID.
3. `start_workflow` — Triggers an active workflow by name or ID.
4. `send_email` — Sends templated notification email.
5. `send_notification` — Dispatches in-app notification.
6. `forward_document` — Forwards document to an external endpoint / channel.
7. `set_decision` — Sets document decision (`approve`, `reject`, `review`).
8. `add_tag` — Appends tag string to document.

> [!IMPORTANT]
> `route_department` is **permanently deprecated** and **completely removed**. The backend must NOT accept or require `route_department`.

---

## 6. Send Email Action Contract

The `send_email` action uses `EmailActionConfig`:
```json
{
  "recipientType": "original_sender",
  "customRecipient": "alerts@partner.org",
  "subject": "Receipt confirmation for {{document.name}}",
  "message": "Your document has been received and queued for review."
}
```

### Recipient Types (`EmailRecipientType`)
- `original_sender`: Routes to `document.originalSender.email`.
- `assigned_user`: Routes to email of the user assigned in `document.assignedToId`.
- `department`: Routes to the notification channel / email of `document.departmentId`.
- `custom`: Routes to literal address specified in `customRecipient`.

### Template Variables Supported
- `{{document.name}}`
- `{{document.type}}`
- `{{document.status}}`
- `{{document.priority}}`
- `{{document.id}}`

---

## 7. Workflow Contract

### Endpoints

| Method | Endpoint | Purpose | Params / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/workflows` | List all workflows | None | `Workflow[]` |
| `GET` | `/api/workflows/{id}` | Get workflow by ID | Path: `id` | `Workflow` |
| `POST` | `/api/workflows` | Create workflow | `WorkflowInput` | `Workflow` |
| `PUT` | `/api/workflows/{id}` | Update workflow | Path: `id`, Body: `WorkflowInput` | `Workflow` |

### `Workflow` Schema
```json
{
  "id": "wf-001",
  "name": "Standard Document Intake",
  "description": "Generic automated pipeline for classifying and routing incoming documents.",
  "status": "active",
  "steps": [
    { "id": "ws-0", "name": "Ingestion", "type": "trigger", "status": "completed", "order": 1 },
    { "id": "ws-1", "name": "Rule Evaluation", "type": "evaluation", "status": "completed", "order": 2 },
    { "id": "ws-2", "name": "Department Routing", "type": "routing", "status": "completed", "order": 3 },
    { "id": "ws-3", "name": "Assignment", "type": "action", "status": "completed", "order": 4 },
    { "id": "ws-4", "name": "Notification", "type": "notification", "status": "completed", "order": 5 }
  ],
  "trigger": "On document arrival",
  "owner": "Sarah Chen",
  "documentsProcessed": 342,
  "createdAt": "2026-04-01T10:00:00Z",
  "updatedAt": "2026-09-15T10:00:00Z"
}
```

---

## 8. Users Contract

### Endpoints

| Method | Endpoint | Purpose | Params / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | List all users | None | `User[]` |
| `GET` | `/api/users/{id}` | Get user by ID | Path: `id` | `User` |
| `POST` | `/api/users` | Invite / create user | `CreateUserRequest` | `User` |
| `PUT` | `/api/users/{id}` | Update user profile / role / status | Path: `id`, Body: `Partial<User>` | `User` |

### `User` Schema
```json
{
  "id": "u-001",
  "name": "Sarah Chen",
  "email": "sarah.chen@example.com",
  "role": "super_admin",
  "department": "Engineering & Product",
  "departmentId": "dept-006",
  "status": "active",
  "avatar": null,
  "lastActive": "2026-10-02T09:30:00Z",
  "createdAt": "2025-03-15T10:00:00Z"
}
```

---

## 9. Organization Contract

The organization service supplies dynamic organizational structures without hardcoded dropdowns.

### Endpoints

| Method | Endpoint | Purpose | Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/organization/departments` | List configurable departments | `Department[]` |
| `GET` | `/api/organization/teams` | List configurable teams | `Team[]` |
| `GET` | `/api/organization/categories` | List document categories | `DocumentCategory[]` |

### Schemas

#### `Department`
```json
{
  "id": "dept-001",
  "name": "Operations",
  "code": "OPS",
  "description": "General operations & execution"
}
```

#### `Team`
```json
{
  "id": "team-001",
  "name": "Document Intake Team",
  "departmentId": "dept-001",
  "leaderId": "u-002"
}
```

#### `DocumentCategory`
```json
{
  "id": "cat-001",
  "name": "Service Agreement",
  "description": "Contracts and service level agreements"
}
```

---

## 10. Notifications Contract

### Endpoints

| Method | Endpoint | Purpose | Params / Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/notifications` | List all notifications | None | `Notification[]` |
| `PUT` | `/api/notifications/{id}/read` | Mark single notification as read | Path: `id` | `200 OK` |
| `PUT` | `/api/notifications/read-all` | Mark all notifications as read | None | `200 OK` |
| `GET` | `/api/notifications/unread-count` | Get total unread count | None | `number` (or `{ "count": number }`) |

### `Notification` Schema
```json
{
  "id": "n-010",
  "title": "Document requires review",
  "message": "Security_Audit_Report_2026.pdf needs your attention. Priority: Critical.",
  "type": "warning",
  "read": false,
  "documentId": "DOC-2026-0850",
  "documentName": "Security_Audit_Report_2026.pdf",
  "timestamp": "2026-10-02T08:00:00Z",
  "priority": "critical"
}
```

---

## 11. Audit Logs Contract

### Endpoints

| Method | Endpoint | Purpose | Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/audit-logs` | Retrieve platform audit history | Query: `search`, `action`, `page`, `pageSize` | `{ "data": AuditEntry[], "total": number }` |

### `AuditEntry` Schema
```json
{
  "id": "au-001",
  "timestamp": "2026-10-02T09:30:00Z",
  "userId": "u-001",
  "userName": "Sarah Chen",
  "action": "uploaded",
  "resource": "Document",
  "resourceId": "DOC-2026-0849",
  "status": "success",
  "details": "Uploaded document: Internal_Policy_Update.pdf",
  "ipAddress": "192.168.1.10"
}
```

---

## 12. Analytics & Dashboard Contract

### Endpoints

| Method | Endpoint | Purpose | Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard/stats` | High-level overview counts | `DashboardStats` |
| `GET` | `/api/dashboard/status-distribution` | Document breakdown by status | `ChartDataPoint[]` |
| `GET` | `/api/dashboard/department-workload` | Workload volume per department | `ChartDataPoint[]` |
| `GET` | `/api/dashboard/weekly-activity` | Document intake activity per weekday | `ChartDataPoint[]` |
| `GET` | `/api/dashboard/recent-documents` | Last 5 ingested documents | `Document[]` |
| `GET` | `/api/dashboard/recent-activity` | Last 5 audit activities | `AuditEntry[]` |

### `DashboardStats` Schema
```json
{
  "totalDocuments": 842,
  "processing": 28,
  "approved": 512,
  "review": 184,
  "rejected": 42,
  "draft": 76,
  "criticalItems": 4,
  "activeRules": 3,
  "activeWorkflows": 3
}
```

### `ChartDataPoint` Schema
```json
{
  "label": "Approved",
  "value": 512,
  "color": "var(--color-success-500)"
}
```

---

## 13. Settings Contract

The Settings view is partitioned as follows:
- **Profile**: Backed by `PUT /api/users/{id}` (Updates name, email).
- **Security**: Backed by `POST /api/auth/change-password` (Passes `currentPassword`, `newPassword`).
- **Notifications**: UI preference toggles (Email notifications, Document assignments, Rule triggers, Workflow updates, Weekly digest). Can persist to `PUT /api/users/{id}/preferences`.
- **Preferences**: Local client settings (Language, Date format, Default page size). Persisted in `localStorage`.
- **Organization**: Backed by `PUT /api/organization/settings` (Organization Name, Domain). Restricted to `admin` / `super_admin`.

---

## 14. File Upload Contract

### Ingestion Contract
- **Method**: `POST`
- **Endpoint**: `/api/documents/upload` (or `/api/documents`)
- **Format**: `multipart/form-data`
- **Field Name**: `file` (Binary payload)
- **Metadata Fields** (optional Form Data / JSON):
  - `name`: string
  - `description`: string
  - `type`: string
  - `tags`: string (comma-separated or JSON array)
  - `source`: `'manual_upload' | 'email' | 'api' | 'integration' | 'scanned'`
  - `originalSenderEmail`: string
  - `originalSenderName`: string
- **Accepted MIME Types**: `.pdf`, `.docx`, `.xlsx`, `.csv`, `.txt`, `.png`, `.jpg`
- **Response**: `201 Created` returning the initialized `Document` object.

---

## 15. API Client Configuration

### Client Defaults
- **Base URL**: `import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'`
- **Headers**:
  - `Content-Type`: `application/json` (omitted for `multipart/form-data`)
  - `Authorization`: `Bearer <token>`
- **HTTP Status Code Mapping**:
  - `200 OK`: Success with payload
  - `201 Created`: Resource successfully created
  - `204 No Content`: Successful deletion / action without response body
  - `400 Bad Request`: Validation failure
  - `401 Unauthorized`: Missing or invalid Bearer token (clears session, triggers redirect to `/login`)
  - `403 Forbidden`: Insufficient role permissions
  - `404 Not Found`: Resource ID does not exist
  - `500 Internal Server Error`: Backend error

---

## 16. Enums & Constants Dictionary

### `UserRole`
`'super_admin' | 'admin' | 'manager' | 'reviewer' | 'user'`

### `UserStatus`
`'active' | 'inactive' | 'pending'`

### `DocumentStatus`
`'draft' | 'processing' | 'review' | 'approved' | 'rejected'`

### `Priority`
`'critical' | 'high' | 'medium' | 'low'`

### `DocumentSource`
`'manual_upload' | 'email' | 'api' | 'integration' | 'scanned'`

### `RuleStatus`
`'active' | 'inactive' | 'draft'`

### `ConditionLogic`
`'AND' | 'OR'`

### `ConditionOperator`
`'equals' | 'not_equals' | 'contains' | 'not_contains' | 'starts_with' | 'ends_with' | 'matches' | 'greater_than' | 'less_than' | 'in' | 'not_in'`

### `ActionType`
`'set_priority' | 'assign_user' | 'start_workflow' | 'send_email' | 'send_notification' | 'forward_document' | 'set_decision' | 'add_tag'`

### `EmailRecipientType`
`'original_sender' | 'assigned_user' | 'department' | 'custom'`

### `WorkflowStatus`
`'active' | 'inactive' | 'draft'`

### `NotificationType`
`'info' | 'success' | 'warning' | 'error'`

### `AuditAction`
`'created' | 'updated' | 'deleted' | 'approved' | 'rejected' | 'assigned' | 'routed' | 'reviewed' | 'exported' | 'uploaded'`

### `AuditStatus`
`'success' | 'failure' | 'warning'`

---

## 17. Pagination Contract

All paginated endpoints expect:
- **Request Query Parameters**:
  - `page`: 1-based page number (e.g. `1`, `2`, `3`)
  - `pageSize`: items per page (e.g. `10`, `20`, `50`)
- **Response Shape**:
  ```json
  {
    "data": [ ... ],
    "total": 142
  }
  ```
  *(Note: The frontend pagination component calculates `totalPages = Math.ceil(total / pageSize)`).*

---

## 18. Date & Time Contract

- **Format**: Strict **ISO-8601 UTC** formatted strings.
- **Example**: `2026-10-02T09:30:00Z`
- **Fields Requiring ISO Format**:
  - `createdAt`, `updatedAt`, `timestamp`, `evaluatedAt`, `lastActive`, `lastTriggered`.

---

## 19. Error Response Contract

No strict error envelope is enforced by the frontend, but standard REST errors are expected:
```json
{
  "message": "Error description for user display",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "errors": {
    "email": "Email already exists"
  }
}
```

---

## 20. Role & Permission Contract

| Navigation Area / Action | Allowed Roles | Enforcement Note |
| :--- | :--- | :--- |
| **Overview / Dashboard** | `super_admin`, `admin`, `manager`, `reviewer`, `user` | Viewable by all |
| **Documents** | `super_admin`, `admin`, `manager`, `reviewer`, `user` | Reviewers/Users restricted to assigned documents by backend |
| **Rules / Rule Builder** | `super_admin`, `admin`, `manager` | Backend authorization required |
| **Workflows** | `super_admin`, `admin`, `manager` | Backend authorization required |
| **Notifications** | `super_admin`, `admin`, `manager`, `reviewer`, `user` | Scoped to authenticated user |
| **Users Management** | `super_admin`, `admin` | Frontend hides nav link; backend authorization required |
| **Audit Logs** | `super_admin`, `admin`, `manager` | Frontend hides nav link; backend authorization required |
| **Reports** | `super_admin`, `admin`, `manager` | Aggregated telemetry |
| **Settings** | `super_admin`, `admin`, `manager`, `reviewer`, `user` | Organization tab restricted to `admin` / `super_admin` |

---

## 21. Mock Data Migration Mapping

| Mock Function (`src/services/api.ts`) | Target Backend API Endpoint | Target HTTP Method | Expected Payload / Response |
| :--- | :--- | :--- | :--- |
| `authService.login` | `/api/auth/login` | `POST` | `{ email, password }` &rarr; `{ user, token }` |
| `authService.logout` | `/api/auth/logout` | `POST` | Empty &rarr; `200 OK` |
| `authService.getCurrentUser` | `/api/auth/me` | `GET` | Headers &rarr; `User` |
| `authService.forgotPassword` | `/api/auth/forgot-password` | `POST` | `{ email }` &rarr; `MessageResponse` |
| `authService.resetPassword` | `/api/auth/reset-password` | `POST` | `{ token, password }` &rarr; `MessageResponse` |
| `documentService.getAll` | `/api/documents` | `GET` | Query params &rarr; `{ data: Document[], total }` |
| `documentService.getById` | `/api/documents/{id}` | `GET` | Path ID &rarr; `DocumentDetail` |
| `documentService.create` | `/api/documents` | `POST` | Multipart / JSON &rarr; `Document` |
| `documentService.update` | `/api/documents/{id}` | `PUT` | `Partial<Document>` &rarr; `Document` |
| `documentService.delete` | `/api/documents/{id}` | `DELETE` | Path ID &rarr; `204 No Content` |
| `ruleService.getAll` | `/api/rules` | `GET` | None &rarr; `Rule[]` |
| `ruleService.getById` | `/api/rules/{id}` | `GET` | Path ID &rarr; `Rule` |
| `ruleService.create` | `/api/rules` | `POST` | `RuleInput` &rarr; `Rule` |
| `ruleService.update` | `/api/rules/{id}` | `PUT` | `RuleInput` &rarr; `Rule` |
| `ruleService.delete` | `/api/rules/{id}` | `DELETE` | Path ID &rarr; `204 No Content` |
| `workflowService.getAll` | `/api/workflows` | `GET` | None &rarr; `Workflow[]` |
| `workflowService.getById` | `/api/workflows/{id}` | `GET` | Path ID &rarr; `Workflow` |
| `userService.getAll` | `/api/users` | `GET` | None &rarr; `User[]` |
| `userService.getById` | `/api/users/{id}` | `GET` | Path ID &rarr; `User` |
| `userService.update` | `/api/users/{id}` | `PUT` | `Partial<User>` &rarr; `User` |
| `organizationService.getDepartments` | `/api/organization/departments` | `GET` | None &rarr; `Department[]` |
| `organizationService.getTeams` | `/api/organization/teams` | `GET` | None &rarr; `Team[]` |
| `organizationService.getDocumentCategories` | `/api/organization/categories` | `GET` | None &rarr; `DocumentCategory[]` |
| `notificationService.getAll` | `/api/notifications` | `GET` | None &rarr; `Notification[]` |
| `notificationService.markAsRead` | `/api/notifications/{id}/read` | `PUT` | Path ID &rarr; `200 OK` |
| `notificationService.markAllRead` | `/api/notifications/read-all` | `PUT` | None &rarr; `200 OK` |
| `notificationService.getUnreadCount` | `/api/notifications/unread-count` | `GET` | None &rarr; `number` |
| `auditService.getAll` | `/api/audit-logs` | `GET` | Query params &rarr; `{ data: AuditEntry[], total }` |
| `dashboardService.getStats` | `/api/dashboard/stats` | `GET` | None &rarr; `DashboardStats` |
| `dashboardService.getStatusDistribution` | `/api/dashboard/status-distribution` | `GET` | None &rarr; `ChartDataPoint[]` |
| `dashboardService.getDepartmentWorkload` | `/api/dashboard/department-workload` | `GET` | None &rarr; `ChartDataPoint[]` |
| `dashboardService.getWeeklyActivity` | `/api/dashboard/weekly-activity` | `GET` | None &rarr; `ChartDataPoint[]` |
| `dashboardService.getRecentDocuments` | `/api/dashboard/recent-documents` | `GET` | None &rarr; `Document[]` |
| `dashboardService.getRecentActivity` | `/api/dashboard/recent-activity` | `GET` | None &rarr; `AuditEntry[]` |

---

## 22. Contract Inconsistencies & Notes for Backend Development

1. **Pagination Wrapping Structure**:
   - The frontend expects paginated endpoints (`/api/documents`, `/api/audit-logs`) to return `{ data: T[], total: number }`.
   - If using Spring Data JPA, configure the response wrapper DTO to serialize the list as `data` and total element count as `total` (rather than standard Spring `content` and `totalElements`).
2. **Dynamic Org Configuration**:
   - `organizationService` endpoints (`/api/organization/departments`, etc.) must be implemented dynamically in the backend database.
3. **No Department Routing Action**:
   - The Rule Builder does not contain or generate `route_department` actions. The rule evaluation engine will execute `assign_user`, `start_workflow`, `set_priority`, `send_email`, `send_notification`, `set_decision`, and `add_tag`.
4. **Original Sender & Description**:
   - The backend `Document` entity must store `originalSender` (`{ email, name }`) and `description` as first-class fields.
