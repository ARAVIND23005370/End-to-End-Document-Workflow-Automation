// ===========================
// E2EDocs — Core Type Definitions
// ===========================

// --- Auth & User ---
export type UserRole = 'super_admin' | 'admin' | 'manager' | 'reviewer' | 'user';
export type UserStatus = 'active' | 'inactive' | 'pending';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  department?: string;
  departmentId?: string;
  status: UserStatus;
  avatar?: string;
  lastActive: string;
  createdAt: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

// --- Organization & Configurable Entities ---
export interface Department {
  id: string;
  name: string;
  code?: string;
  description?: string;
}

export interface Team {
  id: string;
  name: string;
  departmentId?: string;
  leaderId?: string;
}

export interface DocumentCategory {
  id: string;
  name: string;
  description?: string;
}

// --- Documents ---
export type DocumentStatus = 'draft' | 'processing' | 'review' | 'approved' | 'rejected';
export type Priority = 'critical' | 'high' | 'medium' | 'low';
export type DocumentSource = 'manual_upload' | 'email' | 'api' | 'integration' | 'scanned';

export interface OriginalSender {
  email?: string;
  name?: string;
}

export interface Document {
  id: string;
  name: string;
  description?: string;
  originalSender?: OriginalSender | null;
  type: string;
  status: DocumentStatus;
  priority: Priority;
  source: DocumentSource;
  department?: string;
  departmentId?: string;
  assignedTo?: string;
  assignedToId?: string;
  decisionReason?: string;
  missingFields?: string;
  folder?: string;
  createdAt: string;
  updatedAt: string;
  size: number;
  tags: string[];
  ruleMatches: number;
  workflowId?: string;
  metadata?: Record<string, string | number | boolean | null>;
}

export interface DocumentDetail extends Document {
  description: string;
  contentPreview?: string;
  detectedInfo: DetectedInfo[];
  ruleEvaluations: RuleEvaluation[];
  auditHistory: AuditEntry[];
  notifications: Notification[];
}

export interface DetectedInfo {
  label: string;
  value: string;
  confidence?: number;
}

// --- Rules ---
export type RuleType = 'decision' | 'folder' | 'sorting' | 'routing' | 'communication';
export type RuleStatus = 'active' | 'inactive' | 'draft';
export type ConditionOperator =
  | 'equals'
  | 'not_equals'
  | 'contains'
  | 'not_contains'
  | 'greater_than'
  | 'less_than'
  | 'starts_with'
  | 'ends_with'
  | 'matches'
  | 'in'
  | 'not_in';
export type ConditionLogic = 'AND' | 'OR';

export interface RuleCondition {
  id: string;
  field: string;
  operator: ConditionOperator;
  value: string;
}

export interface ConditionGroup {
  id: string;
  logic: ConditionLogic;
  conditions: RuleCondition[];
}

export type ActionType =
  | 'set_priority'
  | 'assign_user'
  | 'assign_department'
  | 'assign_team'
  | 'assign_queue'
  | 'assign_folder'
  | 'start_workflow'
  | 'send_email'
  | 'send_notification'
  | 'forward_document'
  | 'set_decision'
  | 'add_tag';

export type EmailRecipientType = 'original_sender' | 'assigned_user' | 'department' | 'custom';

export interface EmailActionConfig {
  recipientType: EmailRecipientType;
  customRecipient?: string;
  subject?: string;
  message?: string;
}

export interface RuleAction {
  id: string;
  type: ActionType;
  value: string;
  label?: string;
  emailConfig?: EmailActionConfig;
}

export interface Rule {
  id: string;
  name: string;
  description: string;
  status: RuleStatus;
  ruleType: RuleType;
  evaluationOrder: number;
  conditionGroups: ConditionGroup[];
  actions: RuleAction[];
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  matchCount: number;
  lastTriggered?: string;
}

export interface RuleEvaluation {
  ruleId: string;
  ruleName: string;
  matched: boolean;
  evaluatedAt: string;
  conditionsChecked: number;
  actionsTriggered: string[];
}

// --- Workflows ---
export type WorkflowStatus = 'active' | 'inactive' | 'draft';

export interface WorkflowStep {
  id: string;
  name: string;
  type: 'trigger' | 'evaluation' | 'routing' | 'review' | 'decision' | 'notification' | 'action';
  status: 'pending' | 'in_progress' | 'completed' | 'skipped';
  order: number;
}

export interface Workflow {
  id: string;
  name: string;
  description: string;
  status: WorkflowStatus;
  steps: WorkflowStep[];
  trigger: string;
  owner: string;
  documentsProcessed: number;
  createdAt: string;
  updatedAt: string;
}

// --- Notifications ---
export type NotificationType = 'info' | 'success' | 'warning' | 'error';

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  read: boolean;
  documentId?: string;
  documentName?: string;
  timestamp: string;
  priority?: Priority;
}

// --- Audit ---
export type AuditAction = 'created' | 'updated' | 'deleted' | 'approved' | 'rejected' | 'assigned' | 'routed' | 'reviewed' | 'exported' | 'uploaded' | 'sent';
export type AuditStatus = 'success' | 'failure' | 'warning';

export interface AuditEntry {
  id: string;
  timestamp: string;
  userId: string;
  userName: string;
  action: AuditAction;
  resource: string;
  resourceId: string;
  status: AuditStatus;
  details: string;
  ipAddress?: string;
}

// --- Reports ---
export interface ReportMetric {
  label: string;
  value: number;
  change?: number;
  changeLabel?: string;
}

export interface ChartDataPoint {
  label: string;
  value: number;
  color?: string;
}

// --- Navigation ---
export interface NavItem {
  label: string;
  path: string;
  icon: string;
  badge?: number;
  children?: NavItem[];
  roles?: UserRole[];
}

// --- UI States ---
export interface PaginationState {
  page: number;
  pageSize: number;
  total?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface FilterState {
  search: string;
  status?: string;
  priority?: string;
  source?: string;
  department?: string;
  dateFrom?: string;
  dateTo?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

// --- Settings ---
export interface AppSettings {
  theme: 'light' | 'dark' | 'system';
  language: string;
  notificationsEnabled: boolean;
  emailNotifications: boolean;
  compactView: boolean;
}
