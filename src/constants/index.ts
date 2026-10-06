// ===========================
// E2EDocs — Application Constants
// ===========================

import type { NavItem, UserRole } from '../types';

// --- Application ---
export const APP_NAME = 'E2EDocs';
export const APP_TAGLINE = 'Document Workflow Automation';
export const APP_DESCRIPTION =
  'E2EDocs lets teams define rules that classify, route, prioritize and process documents through configurable workflows.';
export const APP_VERSION = '1.0.0';
export const PLACEHOLDER_DOMAIN = 'https://e2edocs.example.com';

// --- Navigation ---
export const NAVIGATION_ITEMS: NavItem[] = [
  { label: 'Overview', path: '/dashboard', icon: 'LayoutDashboard' },
  { label: 'Documents', path: '/documents', icon: 'FileText' },
  { label: 'Decision Automation', path: '/decision-rules', icon: 'CheckCircle2' },
  { label: 'Folder Classification', path: '/folder-classification', icon: 'FolderTree' },
  { label: 'Folder Sorting', path: '/folder-sorting', icon: 'ArrowUpDown' },
  { label: 'Routing & Assignment', path: '/routing', icon: 'SendHorizontal' },
  { label: 'Communication', path: '/communication', icon: 'Mail' },
  { label: 'Workflows', path: '/workflows', icon: 'Workflow' },
  { label: 'Notifications', path: '/notifications', icon: 'Bell' },
  { label: 'Users', path: '/users', icon: 'Users', roles: ['super_admin', 'admin'] },
  { label: 'Audit Logs', path: '/audit-logs', icon: 'ScrollText', roles: ['super_admin', 'admin', 'manager'] },
  { label: 'Reports', path: '/reports', icon: 'BarChart3' },
  { label: 'Settings', path: '/settings', icon: 'Settings' },
];

// --- Status Labels ---
export const DOCUMENT_STATUS_LABELS: Record<string, string> = {
  draft: 'Draft',
  processing: 'Processing',
  review: 'In Review',
  approved: 'Approved',
  rejected: 'Rejected',
};

export const PRIORITY_LABELS: Record<string, string> = {
  critical: 'Critical',
  high: 'High',
  medium: 'Medium',
  low: 'Low',
};

export const RULE_STATUS_LABELS: Record<string, string> = {
  active: 'Active',
  inactive: 'Inactive',
  draft: 'Draft',
};

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  super_admin: 'Super Admin',
  admin: 'Admin',
  manager: 'Manager',
  reviewer: 'Reviewer',
  user: 'User',
};

export const USER_STATUS_LABELS: Record<string, string> = {
  active: 'Active',
  inactive: 'Inactive',
  pending: 'Pending',
};

// --- Document Sources ---
export const DOCUMENT_SOURCE_LABELS: Record<string, string> = {
  manual_upload: 'Manual Upload',
  email: 'Email',
  api: 'API Integration',
  integration: 'External System',
  scanned: 'Scanned Document',
};

// --- Rule Sections & Types ---
export const RULE_SECTIONS = [
  {
    id: 'decision',
    label: 'Decision Automation',
    shortLabel: 'Decision',
    badge: 'DECISION RULE',
    description: 'Configure rules that determine whether a document should be approved, rejected, or sent for manual review.',
    btnLabel: 'Add Decision Rule',
  },
  {
    id: 'folder',
    label: 'Folder Classification',
    shortLabel: 'Folder',
    badge: 'FOLDER RULE',
    description: 'Automatically classify documents and organize them into user-configured folders or categories based on extracted document information.',
    btnLabel: 'Add Folder Rule',
  },
  {
    id: 'sorting',
    label: 'Folder Sorting & Priority',
    shortLabel: 'Sorting',
    badge: 'SORTING RULE',
    description: 'Configure how documents are ordered inside folders, categories, queues, or processing views.',
    btnLabel: 'Add Sorting Rule',
  },
  {
    id: 'routing',
    label: 'Routing & Assignment',
    shortLabel: 'Routing',
    badge: 'ROUTING RULE',
    description: 'Route documents to a configured operational destination after rule evaluation.',
    btnLabel: 'Add Routing Rule',
  },
  {
    id: 'communication',
    label: 'Email & Notifications',
    shortLabel: 'Communication',
    badge: 'COMMUNICATION RULE',
    description: 'Configure automated communication triggered by document processing events.',
    btnLabel: 'Add Communication Rule',
  },
];

export const DECISION_OUTCOME_OPTIONS = [
  { value: 'approved', label: 'Approve Document (APPROVED)' },
  { value: 'rejected', label: 'Reject Document (REJECTED)' },
  { value: 'review', label: 'Send for Manual Review (MANUAL REVIEW)' },
];

export const SORTING_PRIORITY_OPTIONS = [
  { value: 'critical', label: 'Critical Priority (Top / 1st in queue)' },
  { value: 'high', label: 'High Priority (2nd in queue)' },
  { value: 'medium', label: 'Medium Priority (Normal processing)' },
  { value: 'low', label: 'Low Priority (Backlog)' },
];

export const ROUTING_DESTINATION_TYPES = [
  { value: 'assign_user', label: 'Assign to Specific User' },
  { value: 'assign_department', label: 'Route to Department' },
  { value: 'assign_team', label: 'Route to Team' },
  { value: 'assign_queue', label: 'Route to Processing Queue' },
  { value: 'start_workflow', label: 'Trigger Automated Workflow' },
  { value: 'forward_document', label: 'Forward Document Externally' },
];

// --- Condition Operators ---
export const CONDITION_OPERATORS = [
  { value: 'equals', label: 'equals' },
  { value: 'not_equals', label: 'does not equal' },
  { value: 'contains', label: 'contains keyword / text' },
  { value: 'not_contains', label: 'does not contain' },
  { value: 'starts_with', label: 'starts with' },
  { value: 'ends_with', label: 'ends with' },
  { value: 'matches', label: 'matches regex / pattern' },
  { value: 'greater_than', label: 'greater than' },
  { value: 'less_than', label: 'less than' },
  { value: 'in', label: 'is one of' },
  { value: 'not_in', label: 'is not one of' },
];

// --- Action Types ---
export const ACTION_TYPES = [
  { value: 'set_decision', label: 'Set Document Decision' },
  { value: 'assign_folder', label: 'Assign Virtual Folder / Category' },
  { value: 'set_priority', label: 'Set Document Sorting Priority' },
  { value: 'assign_user', label: 'Assign to User' },
  { value: 'assign_department', label: 'Assign to Department' },
  { value: 'assign_team', label: 'Assign to Team' },
  { value: 'assign_queue', label: 'Assign to Processing Queue' },
  { value: 'start_workflow', label: 'Start Workflow' },
  { value: 'send_email', label: 'Send Automated Email' },
  { value: 'send_notification', label: 'Send Notification Alert' },
  { value: 'forward_document', label: 'Forward Document' },
  { value: 'add_tag', label: 'Add Tag' },
];

// --- Email Action Recipient Options ---
export const EMAIL_RECIPIENT_OPTIONS = [
  { value: 'original_sender', label: 'Original Sender Email' },
  { value: 'assigned_user', label: 'Assigned User' },
  { value: 'department', label: 'Department Notification Channel' },
  { value: 'custom', label: 'Specific Email Address' },
];

// --- Condition Fields (Generic & Extensible) ---
export const CONDITION_FIELDS = [
  { value: 'document.name', label: 'Document Name' },
  { value: 'document.type', label: 'Document Type' },
  { value: 'document.description', label: 'Document Description' },
  { value: 'document.content', label: 'Document Content' },
  { value: 'sender.email', label: 'Original Sender Email' },
  { value: 'sender.name', label: 'Original Sender Name' },
  { value: 'metadata.recipient_email', label: 'Recipient Email' },
  { value: 'metadata.subject', label: 'Subject' },
  { value: 'metadata.source', label: 'Ingestion / Upload Source' },
  { value: 'file.extension', label: 'File Extension' },
  { value: 'file.size', label: 'File Size' },
  { value: 'document.priority', label: 'Priority' },
  { value: 'document.department', label: 'Department' },
  { value: 'document.tags', label: 'Tags' },
  { value: 'extracted.text', label: 'Extracted Text' },
  { value: 'extracted.category', label: 'Extracted Category' },
  { value: 'metadata.custom', label: 'Custom Metadata Field' },
];

// --- Pagination ---
export const DEFAULT_PAGE_SIZE = 20;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
