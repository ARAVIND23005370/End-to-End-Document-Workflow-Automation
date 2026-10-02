// ===========================
// E2EDocs — Mock Data
// ===========================

import type {
  Document,
  DocumentDetail,
  Rule,
  Workflow,
  WorkflowStep,
  User,
  Department,
  Team,
  DocumentCategory,
  Notification,
  AuditEntry,
  ChartDataPoint,
} from '../types';

// --- Configured Departments (Configurable by Organization) ---
export const mockDepartments: Department[] = [
  { id: 'dept-001', name: 'Operations', code: 'OPS', description: 'General operations & execution' },
  { id: 'dept-002', name: 'Finance & Accounting', code: 'FIN', description: 'Billing, statements, and financial reviews' },
  { id: 'dept-003', name: 'Legal & Contracts', code: 'LEG', description: 'Agreements, compliance, and legal counsel' },
  { id: 'dept-004', name: 'Customer Support', code: 'SUP', description: 'Client inquiries and support tickets' },
  { id: 'dept-005', name: 'Quality & Compliance', code: 'QC', description: 'Audits and regulatory compliance' },
  { id: 'dept-006', name: 'Engineering & Product', code: 'ENG', description: 'Technical documentation and specs' },
];

// --- Configured Teams ---
export const mockTeams: Team[] = [
  { id: 'team-001', name: 'Document Intake Team', departmentId: 'dept-001' },
  { id: 'team-002', name: 'Contract Reviewers', departmentId: 'dept-003' },
  { id: 'team-003', name: 'Accounts Payable', departmentId: 'dept-002' },
  { id: 'team-004', name: 'Tier 2 Support', departmentId: 'dept-004' },
];

// --- Configured Document Categories ---
export const mockDocumentCategories: DocumentCategory[] = [
  { id: 'cat-001', name: 'Service Agreement', description: 'Contracts and service level agreements' },
  { id: 'cat-002', name: 'Support Request', description: 'Customer tickets and escalation documents' },
  { id: 'cat-003', name: 'Internal Policy', description: 'Organizational guidelines and updates' },
  { id: 'cat-004', name: 'Financial Statement', description: 'Periodic reports and balance sheets' },
  { id: 'cat-005', name: 'Technical Specification', description: 'Architecture, diagrams, and spec docs' },
  { id: 'cat-006', name: 'Audit Report', description: 'Security and compliance audit files' },
];

// --- Users ---
export const mockUsers: User[] = [
  { id: 'u-001', name: 'Alexander Vance', email: 'alexander.vance@example.org', role: 'super_admin', department: 'Engineering & Product', departmentId: 'dept-006', status: 'active', lastActive: '2026-10-02T09:30:00Z', createdAt: '2025-03-15T10:00:00Z' },
  { id: 'u-002', name: 'Marcus Rivera', email: 'marcus.r@example.com', role: 'admin', department: 'Operations', departmentId: 'dept-001', status: 'active', lastActive: '2026-10-02T09:15:00Z', createdAt: '2025-04-20T14:00:00Z' },
  { id: 'u-003', name: 'Priya Sharma', email: 'priya.s@example.org', role: 'manager', department: 'Finance & Accounting', departmentId: 'dept-002', status: 'active', lastActive: '2026-10-01T16:45:00Z', createdAt: '2025-06-10T09:00:00Z' },
  { id: 'u-004', name: 'James Okafor', email: 'james.o@example.org', role: 'reviewer', department: 'Legal & Contracts', departmentId: 'dept-003', status: 'active', lastActive: '2026-10-02T08:20:00Z', createdAt: '2025-07-01T11:00:00Z' },
  { id: 'u-005', name: 'Emily Nakamura', email: 'emily.n@example.com', role: 'reviewer', department: 'Quality & Compliance', departmentId: 'dept-005', status: 'active', lastActive: '2026-09-30T14:00:00Z', createdAt: '2025-08-15T08:30:00Z' },
  { id: 'u-006', name: 'David Kowalski', email: 'david.k@example.com', role: 'user', department: 'Customer Support', departmentId: 'dept-004', status: 'active', lastActive: '2026-10-01T11:30:00Z', createdAt: '2025-09-01T13:00:00Z' },
  { id: 'u-007', name: 'Amara Diallo', email: 'amara.d@example.org', role: 'manager', department: 'Operations', departmentId: 'dept-001', status: 'inactive', lastActive: '2026-09-15T10:00:00Z', createdAt: '2025-05-20T09:00:00Z' },
  { id: 'u-008', name: 'Liam O\'Brien', email: 'liam.ob@example.com', role: 'user', department: 'Customer Support', departmentId: 'dept-004', status: 'pending', lastActive: '', createdAt: '2026-09-28T16:00:00Z' },
  { id: 'u-009', name: 'Sofia Petrov', email: 'sofia.p@example.org', role: 'reviewer', department: 'Quality & Compliance', departmentId: 'dept-005', status: 'active', lastActive: '2026-10-02T07:45:00Z', createdAt: '2025-11-01T10:00:00Z' },
  { id: 'u-010', name: 'Raj Mehta', email: 'raj.m@example.com', role: 'admin', department: 'Engineering & Product', departmentId: 'dept-006', status: 'active', lastActive: '2026-10-02T09:00:00Z', createdAt: '2025-04-10T08:00:00Z' },
];

// --- Generic Documents ---
export const mockDocuments: Document[] = [
  {
    id: 'DOC-2026-0847',
    name: 'Contract_2026_014.pdf',
    description: 'Service agreement requiring review.',
    originalSender: { email: 'contracts@example.org', name: 'Alex Vance' },
    type: 'Service Agreement',
    status: 'review',
    priority: 'high',
    source: 'email',
    department: 'Legal & Contracts',
    departmentId: 'dept-003',
    assignedTo: 'James Okafor',
    assignedToId: 'u-004',
    createdAt: '2026-09-28T10:00:00Z',
    updatedAt: '2026-10-01T14:30:00Z',
    size: 2457600,
    tags: ['contract', 'service-agreement', 'external'],
    ruleMatches: 3,
    metadata: { recipient_email: 'inbox@example.com', subject: 'Updated Service Agreement 2026-014' },
  },
  {
    id: 'DOC-2026-0848',
    name: 'Support_Request_4821.pdf',
    description: 'Customer request regarding account access.',
    originalSender: { email: 'customer@example.com', name: 'Jordan Hayes' },
    type: 'Support Request',
    status: 'processing',
    priority: 'medium',
    source: 'email',
    department: 'Customer Support',
    departmentId: 'dept-004',
    assignedTo: 'David Kowalski',
    assignedToId: 'u-006',
    createdAt: '2026-09-29T08:15:00Z',
    updatedAt: '2026-10-01T16:00:00Z',
    size: 1843200,
    tags: ['ticket', 'support', 'inquiry'],
    ruleMatches: 2,
    metadata: { recipient_email: 'support@example.com', subject: 'Urgent: Account Access Help Required' },
  },
  {
    id: 'DOC-2026-0849',
    name: 'Internal_Policy_Update.pdf',
    description: 'Updated internal policy document.',
    originalSender: null,
    type: 'Internal Policy',
    status: 'approved',
    priority: 'low',
    source: 'manual_upload',
    department: 'Operations',
    departmentId: 'dept-001',
    assignedTo: 'Marcus Rivera',
    assignedToId: 'u-002',
    createdAt: '2026-09-30T09:00:00Z',
    updatedAt: '2026-10-01T09:15:00Z',
    size: 1048576,
    tags: ['policy', 'internal', 'annual-update'],
    ruleMatches: 1,
  },
  {
    id: 'DOC-2026-0850',
    name: 'Security_Audit_Report_2026.pdf',
    description: 'Third-party infrastructure security assessment findings.',
    originalSender: { email: 'audits@security-partner.org', name: 'External Auditor' },
    type: 'Audit Report',
    status: 'review',
    priority: 'critical',
    source: 'integration',
    department: 'Quality & Compliance',
    departmentId: 'dept-005',
    assignedTo: 'Emily Nakamura',
    assignedToId: 'u-005',
    createdAt: '2026-09-25T11:30:00Z',
    updatedAt: '2026-10-02T08:00:00Z',
    size: 3686400,
    tags: ['security', 'compliance', 'audit'],
    ruleMatches: 5,
  },
  {
    id: 'DOC-2026-0851',
    name: 'Q3_Financial_Summary.xlsx',
    description: 'Quarterly consolidated financial report and balance statements.',
    originalSender: { email: 'reporting@finance-team.example', name: 'Elena Rostova' },
    type: 'Financial Statement',
    status: 'approved',
    priority: 'high',
    source: 'api',
    department: 'Finance & Accounting',
    departmentId: 'dept-002',
    assignedTo: 'Priya Sharma',
    assignedToId: 'u-003',
    createdAt: '2026-10-01T10:00:00Z',
    updatedAt: '2026-10-01T15:00:00Z',
    size: 2048000,
    tags: ['financials', 'quarterly', 'summary'],
    ruleMatches: 3,
  },
  {
    id: 'DOC-2026-0852',
    name: 'API_Integration_Spec_v3.docx',
    description: 'Updated architecture and protocol specification for webhook handlers.',
    originalSender: null,
    type: 'Technical Specification',
    status: 'draft',
    priority: 'low',
    source: 'manual_upload',
    department: 'Engineering & Product',
    departmentId: 'dept-006',
    assignedTo: 'Alexander Vance',
    assignedToId: 'u-001',
    createdAt: '2026-09-20T14:00:00Z',
    updatedAt: '2026-09-29T11:00:00Z',
    size: 4096000,
    tags: ['specs', 'api', 'architecture'],
    ruleMatches: 1,
  },
  {
    id: 'DOC-2026-0853',
    name: 'Vendor_Invoice_INV-9021.pdf',
    description: 'Software subscription invoice for cloud telemetry infrastructure.',
    originalSender: { email: 'billing@cloud-provider.example', name: 'Automated Billing' },
    type: 'Service Agreement',
    status: 'processing',
    priority: 'medium',
    source: 'email',
    department: 'Finance & Accounting',
    departmentId: 'dept-002',
    assignedTo: 'Priya Sharma',
    assignedToId: 'u-003',
    createdAt: '2026-10-01T07:00:00Z',
    updatedAt: '2026-10-02T06:30:00Z',
    size: 512000,
    tags: ['invoice', 'accounts-payable'],
    ruleMatches: 2,
    metadata: { recipient_email: 'invoices@example.com', subject: 'Invoice INV-9021 for Cloud Services' },
  },
  {
    id: 'DOC-2026-0854',
    name: 'Customer_Feedback_Form_602.pdf',
    description: 'Scanned user satisfaction survey and feedback responses.',
    originalSender: null,
    type: 'Support Request',
    status: 'rejected',
    priority: 'low',
    source: 'scanned',
    department: 'Customer Support',
    departmentId: 'dept-004',
    assignedTo: 'David Kowalski',
    assignedToId: 'u-006',
    createdAt: '2026-09-27T13:45:00Z',
    updatedAt: '2026-10-01T09:00:00Z',
    size: 819200,
    tags: ['feedback', 'scanned'],
    ruleMatches: 1,
  },
];

// --- Document Detail ---
export const mockDocumentDetail: DocumentDetail = {
  ...mockDocuments[0],
  description: 'Service agreement requiring review.',
  contentPreview: 'This Master Services Agreement ("Agreement") is entered into by and between the parties identified below...',
  detectedInfo: [
    { label: 'Document Type', value: 'Service Agreement' },
    { label: 'Original Sender Email', value: 'contracts@example.org' },
    { label: 'Original Sender Name', value: 'Alex Vance' },
    { label: 'Source Channel', value: 'Email Ingestion' },
    { label: 'Classification', value: 'Standard Review' },
    { label: 'Language', value: 'English' },
  ],
  ruleEvaluations: [
    {
      ruleId: 'r-001',
      ruleName: 'External Email Ingestion Routing',
      matched: true,
      evaluatedAt: '2026-09-28T10:01:00Z',
      conditionsChecked: 2,
      actionsTriggered: ['Set Priority: High', 'Assign to James Okafor'],
    },
    {
      ruleId: 'r-002',
      ruleName: 'Contract Review Workflow Trigger',
      matched: true,
      evaluatedAt: '2026-09-28T10:01:05Z',
      conditionsChecked: 2,
      actionsTriggered: ['Start Workflow: Contract Review', 'Send Email: Original Sender Confirmation'],
    },
  ],
  auditHistory: [
    { id: 'a-001', timestamp: '2026-10-01T14:30:00Z', userId: 'u-004', userName: 'James Okafor', action: 'reviewed', resource: 'Document', resourceId: 'DOC-2026-0847', status: 'success', details: 'Initial legal review completed' },
    { id: 'a-002', timestamp: '2026-09-28T10:01:05Z', userId: 'system', userName: 'Rule Engine', action: 'assigned', resource: 'Document', resourceId: 'DOC-2026-0847', status: 'success', details: 'Assigned to reviewer via rule match' },
    { id: 'a-003', timestamp: '2026-09-28T10:00:00Z', userId: 'system', userName: 'Email Ingestion', action: 'uploaded', resource: 'Document', resourceId: 'DOC-2026-0847', status: 'success', details: 'Document ingested from email: contracts@example.org' },
  ],
  notifications: [
    { id: 'n-001', title: 'Review Assigned', message: 'You have been assigned to review Contract_2026_014.pdf.', type: 'info', read: true, documentId: 'DOC-2026-0847', documentName: 'Contract_2026_014.pdf', timestamp: '2026-09-28T10:02:00Z' },
  ],
};

// --- Generic Rules ---
export const mockRules: Rule[] = [
  {
    id: 'r-001',
    name: 'External Email Ingestion Routing',
    description: 'Matches incoming emails from partner domains and sets priority and assignment.',
    status: 'active',
    evaluationOrder: 10,
    conditionGroups: [
      {
        id: 'cg-001',
        logic: 'AND',
        conditions: [
          { id: 'c-001', field: 'metadata.source', operator: 'equals', value: 'email' },
          { id: 'c-002', field: 'sender.email', operator: 'ends_with', value: '@example.org' },
        ],
      },
    ],
    actions: [
      { id: 'act-001', type: 'set_priority', value: 'high' },
      { id: 'act-002', type: 'assign_user', value: 'James Okafor' },
      {
        id: 'act-003',
        type: 'send_email',
        value: 'Acknowledge Receipt',
        emailConfig: {
          recipientType: 'original_sender',
          subject: 'Receipt confirmation for {{document.name}}',
          message: 'Your document has been received and queued for review.',
        },
      },
    ],
    createdAt: '2026-06-15T10:00:00Z',
    updatedAt: '2026-09-01T14:00:00Z',
    createdBy: 'Alexander Vance',
    matchCount: 47,
    lastTriggered: '2026-10-01T12:00:00Z',
  },
  {
    id: 'r-002',
    name: 'Customer Support Escalation',
    description: 'Identifies customer request documents and initiates the support intake workflow.',
    status: 'active',
    evaluationOrder: 20,
    conditionGroups: [
      {
        id: 'cg-002',
        logic: 'OR',
        conditions: [
          { id: 'c-003', field: 'document.type', operator: 'equals', value: 'Support Request' },
          { id: 'c-004', field: 'document.description', operator: 'contains', value: 'customer' },
        ],
      },
    ],
    actions: [
      { id: 'act-004', type: 'start_workflow', value: 'Customer Request Intake' },
      { id: 'act-005', type: 'assign_user', value: 'David Kowalski' },
      { id: 'act-006', type: 'send_notification', value: 'Support Queue Alert' },
    ],
    createdAt: '2026-06-20T09:00:00Z',
    updatedAt: '2026-08-15T11:00:00Z',
    createdBy: 'Marcus Rivera',
    matchCount: 31,
    lastTriggered: '2026-10-01T15:00:00Z',
  },
  {
    id: 'r-003',
    name: 'High-Priority Content Scanner',
    description: 'Triggers critical priority when specific audit or risk keywords appear in content or description.',
    status: 'active',
    evaluationOrder: 5,
    conditionGroups: [
      {
        id: 'cg-003',
        logic: 'OR',
        conditions: [
          { id: 'c-005', field: 'document.tags', operator: 'contains', value: 'audit' },
          { id: 'c-006', field: 'document.description', operator: 'contains', value: 'urgent' },
        ],
      },
    ],
    actions: [
      { id: 'act-007', type: 'set_priority', value: 'critical' },
      { id: 'act-008', type: 'send_notification', value: 'Compliance Team Alert' },
    ],
    createdAt: '2026-05-10T08:00:00Z',
    updatedAt: '2026-09-20T16:00:00Z',
    createdBy: 'Alexander Vance',
    matchCount: 14,
    lastTriggered: '2026-09-28T10:01:10Z',
  },
];

// --- Generic Workflows ---
const createSteps = (names: string[], types: WorkflowStep['type'][], statuses: WorkflowStep['status'][]): WorkflowStep[] =>
  names.map((name, i) => ({ id: `ws-${i}`, name, type: types[i], status: statuses[i], order: i + 1 }));

export const mockWorkflows: Workflow[] = [
  {
    id: 'wf-001',
    name: 'Standard Document Intake',
    description: 'Generic automated pipeline for classifying and routing incoming documents.',
    status: 'active',
    steps: createSteps(['Ingestion', 'Rule Evaluation', 'Department Routing', 'Assignment', 'Notification'], ['trigger', 'evaluation', 'routing', 'action', 'notification'], ['completed', 'completed', 'completed', 'completed', 'completed']),
    trigger: 'On document arrival',
    owner: 'Alexander Vance',
    documentsProcessed: 342,
    createdAt: '2026-04-01T10:00:00Z',
    updatedAt: '2026-09-15T10:00:00Z',
  },
  {
    id: 'wf-002',
    name: 'Contract Review',
    description: 'Multi-stage review and approval gate for agreements and contracts.',
    steps: createSteps(['Document Received', 'Initial Check', 'Review', 'Approval', 'Final Decision', 'Notification'], ['trigger', 'evaluation', 'review', 'review', 'decision', 'notification'], ['completed', 'completed', 'in_progress', 'pending', 'pending', 'pending']),
    status: 'active',
    trigger: 'Rule: External Email Ingestion Routing',
    owner: 'James Okafor',
    documentsProcessed: 89,
    createdAt: '2026-05-15T09:00:00Z',
    updatedAt: '2026-10-01T15:00:00Z',
  },
  {
    id: 'wf-003',
    name: 'Customer Request Intake',
    description: 'Intake and prioritization for customer requests and support documents.',
    steps: createSteps(['Request Ingested', 'Category Check', 'Assignment', 'Resolution', 'Confirmation'], ['trigger', 'evaluation', 'action', 'review', 'notification'], ['completed', 'completed', 'completed', 'in_progress', 'pending']),
    status: 'active',
    trigger: 'Rule: Customer Support Escalation',
    owner: 'David Kowalski',
    documentsProcessed: 64,
    createdAt: '2026-06-01T10:00:00Z',
    updatedAt: '2026-09-28T10:00:00Z',
  },
];

// --- Notifications ---
export const mockNotifications: Notification[] = [
  { id: 'n-010', title: 'Document requires review', message: 'Security_Audit_Report_2026.pdf needs your attention. Priority: Critical.', type: 'warning', read: false, documentId: 'DOC-2026-0850', documentName: 'Security_Audit_Report_2026.pdf', timestamp: '2026-10-02T08:00:00Z', priority: 'critical' },
  { id: 'n-011', title: 'Rule triggered', message: 'External Email Ingestion Routing rule matched on Contract_2026_014.pdf.', type: 'info', read: false, documentId: 'DOC-2026-0847', documentName: 'Contract_2026_014.pdf', timestamp: '2026-10-02T07:00:00Z' },
  { id: 'n-012', title: 'Workflow completed', message: 'Standard Document Intake workflow completed for Internal_Policy_Update.pdf.', type: 'success', read: false, documentId: 'DOC-2026-0849', documentName: 'Internal_Policy_Update.pdf', timestamp: '2026-10-01T14:30:00Z' },
  { id: 'n-013', title: 'Document rejected', message: 'Customer_Feedback_Form_602.pdf was rejected during review.', type: 'error', read: true, documentId: 'DOC-2026-0854', documentName: 'Customer_Feedback_Form_602.pdf', timestamp: '2026-10-01T09:00:00Z' },
];

// --- Audit Entries ---
export const mockAuditEntries: AuditEntry[] = [
  { id: 'au-001', timestamp: '2026-10-02T09:30:00Z', userId: 'u-001', userName: 'Alexander Vance', action: 'uploaded', resource: 'Document', resourceId: 'DOC-2026-0849', status: 'success', details: 'Uploaded document: Internal_Policy_Update.pdf' },
  { id: 'au-002', timestamp: '2026-10-02T08:00:00Z', userId: 'u-005', userName: 'Emily Nakamura', action: 'reviewed', resource: 'Document', resourceId: 'DOC-2026-0850', status: 'success', details: 'Started review of Security_Audit_Report_2026.pdf' },
  { id: 'au-003', timestamp: '2026-10-01T16:00:00Z', userId: 'u-004', userName: 'James Okafor', action: 'assigned', resource: 'Document', resourceId: 'DOC-2026-0847', status: 'success', details: 'Assigned Contract_2026_014.pdf to review queue' },
  { id: 'au-004', timestamp: '2026-10-01T14:30:00Z', userId: 'u-003', userName: 'Priya Sharma', action: 'approved', resource: 'Document', resourceId: 'DOC-2026-0851', status: 'success', details: 'Approved Q3_Financial_Summary.xlsx' },
];

// --- Dashboard Stats ---
export const mockDashboardStats = {
  totalDocuments: 842,
  processing: 28,
  approved: 512,
  review: 184,
  rejected: 42,
  draft: 76,
  criticalItems: 4,
  activeRules: 3,
  activeWorkflows: 3,
};

export const mockStatusDistribution: ChartDataPoint[] = [
  { label: 'Approved', value: 512, color: 'var(--color-success-500)' },
  { label: 'In Review', value: 184, color: 'var(--color-warning-500)' },
  { label: 'Draft', value: 76, color: 'var(--color-gray-400)' },
  { label: 'Rejected', value: 42, color: 'var(--color-error-500)' },
  { label: 'Processing', value: 28, color: 'var(--color-info-500)' },
];

export const mockDepartmentWorkload: ChartDataPoint[] = [
  { label: 'Operations', value: 180 },
  { label: 'Legal & Contracts', value: 165 },
  { label: 'Finance & Accounting', value: 154 },
  { label: 'Customer Support', value: 142 },
  { label: 'Quality & Compliance', value: 112 },
  { label: 'Engineering & Product', value: 89 },
];

export const mockWeeklyActivity: ChartDataPoint[] = [
  { label: 'Mon', value: 42 },
  { label: 'Tue', value: 58 },
  { label: 'Wed', value: 51 },
  { label: 'Thu', value: 67 },
  { label: 'Fri', value: 45 },
  { label: 'Sat', value: 12 },
  { label: 'Sun', value: 8 },
];
