// ===========================
// E2EDocs — API Service Layer
// ===========================
//
// This module provides a clean abstraction for all API calls.
// Currently returns mock data. Replace implementations with
// real fetch/axios calls when the backend is ready.
//
// The UI should NEVER import mock data directly — always go through services.

import type {
  Document,
  DocumentDetail,
  DocumentStatus,
  Priority,
  DocumentSource,
  Rule,
  RuleType,
  Workflow,
  User,
  UserRole,
  UserStatus,
  Department,
  Team,
  DocumentCategory,
  Notification,
  AuditEntry,
  FilterState,
  PaginationState,
} from '../types';
import {
  mockDocuments,
  mockDocumentDetail,
  mockRules,
  mockWorkflows,
  mockDepartments,
  mockTeams,
  mockDocumentCategories,
  mockNotifications,
  mockAuditEntries,
  mockDashboardStats,
  mockStatusDistribution,
  mockDepartmentWorkload,
  mockWeeklyActivity,
} from '../data/mockData';

// --- Config ---
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'https://docflow-backend-fhv4.onrender.com/api';

// Simulate network latency for realistic UX
const delay = (ms = 400) => new Promise((r) => setTimeout(r, ms));

// ===========================
// Organization Service (Configurable Org Data)
// ===========================
export const organizationService = {
  async getDepartments(): Promise<Department[]> {
    await delay(150);
    return mockDepartments;
  },

  async getTeams(): Promise<Team[]> {
    await delay(150);
    return mockTeams;
  },

  async getDocumentCategories(): Promise<DocumentCategory[]> {
    await delay(150);
    return mockDocumentCategories;
  },
};

function getAuthToken(): string | null {
  return localStorage.getItem('e2edocs_token') || localStorage.getItem('e2edocs_auth_token');
}

// --- Generic fetch wrapper ---
async function apiFetch<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options?.headers as Record<string, string>),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });
  if (!res.ok) {
    let errorMsg = `API error: ${res.status}`;
    try {
      const data = await res.json();
      if (data.message) errorMsg = data.message;
      else if (data.error) errorMsg = data.error;
    } catch {}
    throw new Error(errorMsg);
  }
  if (res.status === 204) return {} as T;
  return res.json();
}

function mapDoc(d: any): Document {
  return {
    id: d.id,
    name: d.name,
    description: d.description || '',
    originalSender: d.originalSender ? {
      email: d.originalSender.email,
      name: d.originalSender.name,
    } : null,
    type: d.type || 'General Document',
    status: (d.status?.toLowerCase() || 'draft') as DocumentStatus,
    priority: (d.priority?.toLowerCase() || 'medium') as Priority,
    source: (d.source?.toLowerCase() || 'manual_upload') as DocumentSource,
    department: d.department || '',
    departmentId: d.departmentId || '',
    assignedTo: d.assignedTo || '',
    assignedToId: d.assignedToId || '',
    decisionReason: d.decisionReason || '',
    missingFields: d.missingFields || '',
    folder: d.folder || d.type || 'General Document',
    createdAt: d.createdAt || new Date().toISOString(),
    updatedAt: d.updatedAt || d.createdAt || new Date().toISOString(),
    size: d.size || 0,
    tags: d.tags || [],
    ruleMatches: d.ruleMatches || 0,
    workflowId: d.workflowId,
    metadata: d.metadata || {},
  };
}

// ===========================
// Document Service
// ===========================
export const documentService = {
  async getAll(filters?: FilterState, pagination?: PaginationState): Promise<{ data: Document[]; total: number }> {
    const params = new URLSearchParams();
    if (filters?.search) params.set('search', filters.search);
    if (filters?.status) params.set('status', filters.status.toUpperCase());
    if (filters?.priority) params.set('priority', filters.priority.toUpperCase());
    if (filters?.source) params.set('source', filters.source.toUpperCase());
    if (filters?.department) params.set('department', filters.department);
    if (pagination?.page) params.set('page', String(pagination.page));
    if (pagination?.pageSize) params.set('pageSize', String(pagination.pageSize));
    if (pagination?.sortBy) params.set('sortBy', pagination.sortBy);
    if (pagination?.sortOrder) params.set('sortOrder', pagination.sortOrder);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await apiFetch<any>(`/documents${query}`);
    return {
      data: (res.data || []).map(mapDoc),
      total: res.total || 0,
    };
  },

  async getById(id: string): Promise<DocumentDetail> {
    const d = await apiFetch<any>(`/documents/${id}`);
    const base = mapDoc(d);
    return {
      ...base,
      description: d.description || '',
      contentPreview: d.contentPreview || '',
      detectedInfo: (d.detectedInfo || []).map((info: any) => ({
        label: info.label || info.field || '',
        value: String(info.value || ''),
        confidence: info.confidence || 0.95,
      })),
      ruleEvaluations: (d.ruleEvaluations || []).map((re: any) => ({
        ruleId: re.ruleId || '',
        ruleName: re.ruleName || '',
        status: re.status || 'passed',
        evaluatedAt: re.evaluatedAt || new Date().toISOString(),
        details: re.details || '',
      })),
      auditHistory: (d.auditHistory || []).map((ah: any) => ({
        id: ah.id || '',
        timestamp: ah.timestamp || '',
        userId: ah.userId || '',
        userName: ah.userName || '',
        action: ah.action || '',
        resource: ah.resource || 'Document',
        resourceId: ah.resourceId || d.id,
        status: ah.status || 'success',
        details: ah.details || '',
      })),
      notifications: [],
    };
  },

  async upload(file: File, meta?: { name?: string; type?: string; priority?: string; department?: string; departmentId?: string; description?: string }): Promise<Document> {
    const formData = new FormData();
    formData.append('file', file);
    if (meta?.name) formData.append('name', meta.name);
    if (meta?.type) formData.append('type', meta.type);
    if (meta?.priority) formData.append('priority', meta.priority.toUpperCase());
    if (meta?.department) formData.append('department', meta.department);
    if (meta?.departmentId) formData.append('departmentId', meta.departmentId);
    if (meta?.description) formData.append('description', meta.description);

    const token = getAuthToken();
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}/documents`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!res.ok) {
      let errorMsg = `Upload failed (${res.status})`;
      try {
        const data = await res.json();
        if (data.message) errorMsg = data.message;
        else if (data.error) errorMsg = data.error;
      } catch {}
      throw new Error(errorMsg);
    }

    const d = await res.json();
    return mapDoc(d);
  },

  async uploadBatch(files: File[], meta?: { type?: string; priority?: string; department?: string; description?: string }): Promise<{ totalFiles: number; successful: number; failed: number; documents: Document[]; errors: Record<string, string> }> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });
    if (meta?.type) formData.append('type', meta.type);
    if (meta?.priority) formData.append('priority', meta.priority.toUpperCase());
    if (meta?.department) formData.append('department', meta.department);
    if (meta?.description) formData.append('description', meta.description);

    const token = getAuthToken();
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}/documents/batch`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!res.ok) {
      let errorMsg = `Batch upload failed (${res.status})`;
      try {
        const data = await res.json();
        if (data.message) errorMsg = data.message;
        else if (data.error) errorMsg = data.error;
      } catch {}
      throw new Error(errorMsg);
    }

    const result = await res.json();
    return {
      totalFiles: result.totalFiles || files.length,
      successful: result.successful || 0,
      failed: result.failed || 0,
      documents: (result.documents || []).map(mapDoc),
      errors: result.errors || {},
    };
  },

  async create(data: Partial<Document>): Promise<Document> {
    const res = await apiFetch<any>('/documents', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return mapDoc(res);
  },

  async export(filters?: { search?: string; status?: string; priority?: string; source?: string; department?: string }): Promise<void> {
    const params = new URLSearchParams();
    if (filters?.search) params.set('search', filters.search);
    if (filters?.status) params.set('status', filters.status.toUpperCase());
    if (filters?.priority) params.set('priority', filters.priority.toUpperCase());
    if (filters?.source) params.set('source', filters.source.toUpperCase());
    if (filters?.department) params.set('department', filters.department);

    const query = params.toString() ? `?${params.toString()}` : '';
    const token = getAuthToken();
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}/documents/export${query}`, {
      method: 'GET',
      headers,
    });

    if (!res.ok) {
      throw new Error(`Export failed: ${res.status}`);
    }

    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'e2edocs-documents.csv';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },

  async sendEmail(id: string, payload: { recipient: string; subject: string; message?: string }): Promise<{ success: boolean; message: string }> {
    return apiFetch<{ success: boolean; message: string }>(`/documents/${id}/send-email`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async download(id: string, defaultFilename?: string): Promise<void> {
    const token = getAuthToken();
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}/documents/${id}/download`, {
      method: 'GET',
      headers,
    });

    if (!res.ok) {
      throw new Error(`Download failed: ${res.status}`);
    }

    let filename = defaultFilename || `document-${id}.pdf`;
    const disposition = res.headers.get('content-disposition');
    if (disposition && disposition.includes('filename=')) {
      const match = disposition.match(/filename="?([^";]+)"?/);
      if (match && match[1]) filename = match[1];
    }

    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },

  async update(id: string, data: Partial<Document>): Promise<Document> {
    const res = await apiFetch<any>(`/documents/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return mapDoc(res);
  },

  async delete(id: string): Promise<void> {
    await apiFetch(`/documents/${id}`, {
      method: 'DELETE',
    });
  },
};

// ===========================
// Rule Service
// ===========================
export const ruleService = {
  async getAll(ruleType?: RuleType): Promise<Rule[]> {
    const url = ruleType ? `/rules?ruleType=${ruleType}` : '/rules';
    return apiFetch<Rule[]>(url);
  },

  async getById(id: string): Promise<Rule> {
    return apiFetch<Rule>(`/rules/${id}`);
  },

  async create(data: Partial<Rule>): Promise<Rule> {
    return apiFetch<Rule>('/rules', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  async update(id: string, data: Partial<Rule>): Promise<Rule> {
    return apiFetch<Rule>(`/rules/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  async delete(id: string): Promise<void> {
    await apiFetch(`/rules/${id}`, {
      method: 'DELETE',
    });
  },

  async testRule(payload: any): Promise<any> {
    return apiFetch('/rules/test', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};

// ===========================
// Workflow Service
// ===========================
export const workflowService = {
  async getAll(): Promise<Workflow[]> {
    await delay();
    return mockWorkflows;
  },

  async getById(id: string): Promise<Workflow> {
    await delay(300);
    const wf = mockWorkflows.find((w) => w.id === id);
    if (!wf) throw new Error('Workflow not found');
    return wf;
  },
};

// ===========================
// User Service (Real Backend API)
// ===========================
export const userService = {
  async getAll(): Promise<User[]> {
    const res = await apiFetch<any[]>('/users');
    return res.map((u) => ({
      id: u.id,
      name: u.name,
      email: u.email,
      role: (u.role?.toLowerCase() || 'user') as UserRole,
      department: u.department || '—',
      departmentId: u.departmentId,
      status: (u.status?.toLowerCase() || 'active') as UserStatus,
      avatar: u.avatar,
      lastActive: u.lastActive || u.lastActiveAt || u.createdAt || new Date().toISOString(),
      createdAt: u.createdAt || new Date().toISOString(),
    }));
  },

  async getById(id: string): Promise<User> {
    const u = await apiFetch<any>(`/users/${id}`);
    return {
      id: u.id,
      name: u.name,
      email: u.email,
      role: (u.role?.toLowerCase() || 'user') as UserRole,
      department: u.department || '—',
      departmentId: u.departmentId,
      status: (u.status?.toLowerCase() || 'active') as UserStatus,
      avatar: u.avatar,
      lastActive: u.lastActive || u.lastActiveAt || u.createdAt || new Date().toISOString(),
      createdAt: u.createdAt || new Date().toISOString(),
    };
  },

  async create(data: { name: string; email: string; role: UserRole; departmentId?: string; department?: string; password?: string }): Promise<User> {
    const payload: any = {
      name: data.name,
      email: data.email,
      role: data.role.toUpperCase(),
      departmentId: data.departmentId,
      department: data.department,
      password: data.password,
    };
    const u = await apiFetch<any>('/users/invite', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return {
      id: u.id,
      name: u.name,
      email: u.email,
      role: (u.role?.toLowerCase() || 'user') as UserRole,
      department: u.department || '—',
      departmentId: u.departmentId,
      status: (u.status?.toLowerCase() || 'active') as UserStatus,
      avatar: u.avatar,
      lastActive: u.lastActive || u.lastActiveAt || u.createdAt || new Date().toISOString(),
      createdAt: u.createdAt || new Date().toISOString(),
    };
  },

  async invite(data: { name: string; email: string; role: UserRole; departmentId?: string; department?: string; password?: string }): Promise<User> {
    return this.create(data);
  },

  async update(id: string, data: Partial<User>): Promise<User> {
    const payload: any = {};
    if (data.name !== undefined) payload.name = data.name;
    if (data.email !== undefined) payload.email = data.email;
    if (data.role !== undefined) payload.role = data.role.toUpperCase();
    if (data.status !== undefined) payload.status = data.status.toUpperCase();
    if (data.departmentId !== undefined) payload.departmentId = data.departmentId;
    if (data.department !== undefined) payload.department = data.department;
    if (data.avatar !== undefined) payload.avatar = data.avatar;

    const u = await apiFetch<any>(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    return {
      id: u.id,
      name: u.name,
      email: u.email,
      role: (u.role?.toLowerCase() || 'user') as UserRole,
      department: u.department || '—',
      departmentId: u.departmentId,
      status: (u.status?.toLowerCase() || 'active') as UserStatus,
      avatar: u.avatar,
      lastActive: u.lastActive || u.lastActiveAt || u.createdAt || new Date().toISOString(),
      createdAt: u.createdAt || new Date().toISOString(),
    };
  },

  async delete(id: string): Promise<void> {
    await apiFetch(`/users/${id}`, {
      method: 'DELETE',
    });
  },

  async resendInvite(id: string): Promise<{ message: string }> {
    return apiFetch<{ message: string }>(`/users/${id}/resend-invite`, {
      method: 'POST',
    });
  },
};

// ===========================
// Notification Service
// ===========================
export const notificationService = {
  async getAll(): Promise<Notification[]> {
    await delay();
    return mockNotifications;
  },

  async markAsRead(_id: string): Promise<void> {
    await delay(200);
  },

  async markAllRead(): Promise<void> {
    await delay(200);
  },

  async getUnreadCount(): Promise<number> {
    await delay(100);
    return mockNotifications.filter((n) => !n.read).length;
  },
};

// ===========================
// Audit Service
// ===========================
export const auditService = {
  async getAll(_filters?: FilterState): Promise<{ data: AuditEntry[]; total: number }> {
    await delay();
    void _filters;
    return { data: mockAuditEntries, total: mockAuditEntries.length };
  },
};

// ===========================
// Dashboard Service
// ===========================
export const dashboardService = {
  async getStats() {
    await delay(300);
    return mockDashboardStats;
  },

  async getStatusDistribution() {
    await delay(300);
    return mockStatusDistribution;
  },

  async getDepartmentWorkload() {
    await delay(300);
    return mockDepartmentWorkload;
  },

  async getWeeklyActivity() {
    await delay(300);
    return mockWeeklyActivity;
  },

  async getRecentDocuments(): Promise<Document[]> {
    await delay(300);
    return mockDocuments.slice(0, 5);
  },

  async getRecentActivity(): Promise<AuditEntry[]> {
    await delay(300);
    return mockAuditEntries.slice(0, 5);
  },
};

// ===========================
// Auth Service
// ===========================
export const authService = {
  async getSignupStatus(): Promise<{ available: boolean }> {
    try {
      return await apiFetch<{ available: boolean }>('/auth/signup/status');
    } catch {
      return { available: true };
    }
  },

  async register(data: { name: string; email: string; password: string; organizationName: string }): Promise<{ user: User; token: string }> {
    const res = await apiFetch<{ user: any; token: string }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    const user: User = {
      id: res.user.id,
      name: res.user.name,
      email: res.user.email,
      role: (res.user.role?.toLowerCase() || 'admin') as UserRole,
      department: res.user.department || 'Executive',
      departmentId: res.user.departmentId,
      status: (res.user.status?.toLowerCase() || 'active') as UserStatus,
      avatar: res.user.avatar,
      lastActive: res.user.lastActive || new Date().toISOString(),
      createdAt: res.user.createdAt || new Date().toISOString(),
    };
    return { user, token: res.token };
  },

  async login(email: string, password: string): Promise<{ user: User; token: string }> {
    const res = await apiFetch<{ user: any; token: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    const user: User = {
      id: res.user.id,
      name: res.user.name,
      email: res.user.email,
      role: (res.user.role?.toLowerCase() || 'admin') as UserRole,
      department: res.user.department || 'Executive',
      departmentId: res.user.departmentId,
      status: (res.user.status?.toLowerCase() || 'active') as UserStatus,
      avatar: res.user.avatar,
      lastActive: res.user.lastActive || new Date().toISOString(),
      createdAt: res.user.createdAt || new Date().toISOString(),
    };
    return { user, token: res.token };
  },

  async logout(): Promise<void> {
    try {
      await apiFetch<void>('/auth/logout', { method: 'POST' });
    } catch {}
    localStorage.removeItem('e2edocs_token');
    localStorage.removeItem('e2edocs_user');
  },

  async forgotPassword(email: string): Promise<void> {
    await apiFetch<{ message: string }>('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  async resetPassword(token: string, password: string): Promise<void> {
    await apiFetch<{ message: string }>('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, password }),
    });
  },

  async getCurrentUser(): Promise<User> {
    const res = await apiFetch<any>('/auth/me');
    return {
      id: res.id,
      name: res.name,
      email: res.email,
      role: (res.role?.toLowerCase() || 'admin') as UserRole,
      department: res.department || 'Executive',
      departmentId: res.departmentId,
      status: (res.status?.toLowerCase() || 'active') as UserStatus,
      avatar: res.avatar,
      lastActive: res.lastActive || new Date().toISOString(),
      createdAt: res.createdAt || new Date().toISOString(),
    };
  },
};

export { apiFetch };
