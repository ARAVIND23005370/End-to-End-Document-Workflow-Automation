// ===========================
// E2EDocs — Documents Page (Real API Integration, Upload, Export, Manual Email)
// ===========================

import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus, Upload, Download, MoreHorizontal, Eye, Trash2,
  FileText, Mail, Send, X, AlertTriangle, CheckCircle2,
  Paperclip, RefreshCw
} from 'lucide-react';
import {
  Button, Card, SearchInput, SelectField,
  StatusBadge, PriorityBadge, Badge, EmptyState, LoadingState, ErrorState,
  Pagination, Dropdown, DropdownItem, Avatar, Input, Textarea
} from '../../components/ui';
import { useDocumentTitle, useAsync, useDebouncedValue } from '../../hooks';
import { documentService, organizationService } from '../../services/api';
import { formatDate, formatRelativeTime, formatFileSize } from '../../utils';
import { DOCUMENT_STATUS_LABELS, PRIORITY_LABELS, DOCUMENT_SOURCE_LABELS } from '../../constants';
import type { Document, DocumentStatus, Priority, DocumentSource } from '../../types';

export default function DocumentsPage() {
  useDocumentTitle('Documents');
  const navigate = useNavigate();

  // Search & Filter State
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');
  const [sourceFilter, setSourceFilter] = useState('');
  const [page, setPage] = useState(1);
  const pageSize = 10;
  const debouncedSearch = useDebouncedValue(search);

  // Departments for optional upload routing
  const { data: departments = [] } = useAsync(() => organizationService.getDepartments(), []);

  // Fetch real documents from backend
  const { data: result, status, error, refetch } = useAsync(
    () => documentService.getAll(
      {
        search: debouncedSearch,
        status: statusFilter,
        priority: priorityFilter,
        source: sourceFilter,
      },
      { page, pageSize, sortBy: 'createdAt', sortOrder: 'desc' }
    ),
    [debouncedSearch, statusFilter, priorityFilter, sourceFilter, page]
  );

  const documents = result?.data || [];
  const totalDocuments = result?.total || 0;

  // --- Upload Modal State ---
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [uploadFiles, setUploadFiles] = useState<File[]>([]);
  const [uploadName, setUploadName] = useState('');
  const [uploadType, setUploadType] = useState('General Document');
  const [uploadPriority, setUploadPriority] = useState<Priority>('medium');
  const [uploadDept, setUploadDept] = useState('');
  const [uploadDesc, setUploadDesc] = useState('');
  const [uploadLoading, setUploadLoading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  // --- Export State ---
  const [exportLoading, setExportLoading] = useState(false);

  // --- Manual Email Modal State ---
  const [emailDoc, setEmailDoc] = useState<Document | null>(null);
  const [recipient, setRecipient] = useState('');
  const [emailSubject, setEmailSubject] = useState('');
  const [emailMessage, setEmailMessage] = useState('');
  const [emailLoading, setEmailLoading] = useState(false);
  const [emailError, setEmailError] = useState('');

  // --- Global Notification Toast ---
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showNotification = (message: string, type: 'success' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 4000);
  };

  // --- Handle Real Upload ---
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const selected = Array.from(e.target.files);
      setUploadFiles(selected);
      if (selected.length === 1 && !uploadName) {
        setUploadName(selected[0].name);
      }
      setUploadError('');
    }
  };

  const handleUploadSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (uploadFiles.length === 0) {
      setUploadError('Please select at least one document file to upload.');
      return;
    }

    setUploadLoading(true);
    setUploadError('');

    try {
      if (uploadFiles.length === 1) {
        await documentService.upload(uploadFiles[0], {
          name: uploadName.trim() || uploadFiles[0].name,
          type: uploadType,
          priority: uploadPriority,
          department: uploadDept,
          description: uploadDesc,
        });
        showNotification('Document uploaded and ingested through Rule Engine successfully.');
      } else {
        const batchResult = await documentService.uploadBatch(uploadFiles, {
          type: uploadType,
          priority: uploadPriority,
          department: uploadDept,
          description: uploadDesc,
        });
        showNotification(`Batch complete: ${batchResult.successful} of ${batchResult.totalFiles} documents ingested.`);
      }

      setIsUploadOpen(false);
      setUploadFiles([]);
      setUploadName('');
      setUploadDesc('');
      setUploadDept('');
      refetch();
    } catch (err: any) {
      setUploadError(err.message || 'Document upload failed. Please verify file format.');
    } finally {
      setUploadLoading(false);
    }
  };

  // --- Handle Real CSV Export ---
  const handleExport = async () => {
    setExportLoading(true);
    try {
      await documentService.export({
        search: debouncedSearch,
        status: statusFilter,
        priority: priorityFilter,
        source: sourceFilter,
      });
      showNotification('Document list CSV exported successfully.');
    } catch (err: any) {
      showNotification(err.message || 'Failed to export document list.', 'error');
    } finally {
      setExportLoading(false);
    }
  };

  // --- Handle Real Manual Email Delivery ---
  const openEmailModal = (doc: Document) => {
    setEmailDoc(doc);
    setRecipient(doc.originalSender?.email || '');
    setEmailSubject(`Document Delivery: ${doc.name}`);
    setEmailMessage(`Hello,\n\nPlease find attached the document: ${doc.name} (${doc.id}).\n\nBest regards,\nE2EDocs Team`);
    setEmailError('');
  };

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailDoc) return;

    if (!recipient || !recipient.includes('@')) {
      setEmailError('Please enter a valid recipient email address.');
      return;
    }
    if (!emailSubject.trim()) {
      setEmailError('Email subject cannot be empty.');
      return;
    }

    setEmailLoading(true);
    setEmailError('');

    try {
      const res = await documentService.sendEmail(emailDoc.id, {
        recipient: recipient.trim(),
        subject: emailSubject.trim(),
        message: emailMessage.trim(),
      });

      setEmailDoc(null);
      showNotification(res.message || `Document sent via email to ${recipient}.`);
    } catch (err: any) {
      setEmailError(err.message || 'Failed to send document via email. Check SMTP settings.');
    } finally {
      setEmailLoading(false);
    }
  };

  // --- Handle Real Binary File Download ---
  const handleDownload = async (doc: Document) => {
    try {
      showNotification(`Downloading ${doc.name}…`);
      await documentService.download(doc.id, doc.name);
    } catch (err: any) {
      showNotification(err.message || 'Failed to download file.', 'error');
    }
  };

  // --- Handle Real Delete ---
  const handleDelete = async (doc: Document) => {
    if (window.confirm(`Are you sure you want to delete document "${doc.name}"? This action cannot be undone.`)) {
      try {
        await documentService.delete(doc.id);
        showNotification(`Document "${doc.name}" deleted.`);
        refetch();
      } catch (err: any) {
        showNotification(err.message || 'Failed to delete document.', 'error');
      }
    }
  };

  return (
    <div>
      {/* Toast Notification */}
      {notification && (
        <div style={{
          position: 'fixed', bottom: 24, right: 24, zIndex: 1100,
          padding: '12px 18px', borderRadius: 'var(--radius-md, 6px)',
          backgroundColor: notification.type === 'success' ? 'var(--color-success-700, #15803d)' : 'var(--color-error-700, #b91c1c)',
          color: '#ffffff', boxShadow: 'var(--shadow-lg)',
          display: 'flex', alignItems: 'center', gap: 10, fontSize: 'var(--text-body-sm)'
        }}>
          {notification.type === 'success' ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
          <span>{notification.message}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Documents</h1>
            <p className="page-description">Manage, review, evaluate, and deliver documents across configurable workflow rules.</p>
          </div>
          <div className="page-actions" style={{ display: 'flex', gap: 'var(--space-2)' }}>
            <Button variant="secondary" size="sm" onClick={handleExport} disabled={exportLoading}>
              <Download size={14} /> {exportLoading ? 'Exporting…' : 'Export'}
            </Button>
            <Button variant="primary" size="sm" onClick={() => setIsUploadOpen(true)}>
              <Upload size={14} /> Upload Document
            </Button>
          </div>
        </div>
      </div>

      <Card>
        {/* Filter Bar */}
        <div className="filter-bar" style={{ borderBottom: '1px solid var(--border-secondary)' }}>
          <div style={{ flex: '1 1 220px' }}>
            <SearchInput
              placeholder="Search documents, IDs, senders…"
              value={search}
              onChange={(e) => { setSearch((e.target as HTMLInputElement).value); setPage(1); }}
              onSearch={(v) => { setSearch(v); setPage(1); }}
            />
          </div>
          <SelectField
            options={Object.entries(DOCUMENT_STATUS_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            placeholder="All statuses"
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }}
          />
          <SelectField
            options={Object.entries(PRIORITY_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            placeholder="All priorities"
            value={priorityFilter}
            onChange={(e) => { setPriorityFilter(e.target.value); setPage(1); }}
          />
          <SelectField
            options={Object.entries(DOCUMENT_SOURCE_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            placeholder="All sources"
            value={sourceFilter}
            onChange={(e) => { setSourceFilter(e.target.value); setPage(1); }}
          />
          {(statusFilter || priorityFilter || sourceFilter || search) && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setSearch('');
                setStatusFilter('');
                setPriorityFilter('');
                setSourceFilter('');
                setPage(1);
              }}
            >
              Clear
            </Button>
          )}
        </div>

        {/* Real Document List Content */}
        {status === 'loading' ? (
          <LoadingState message="Loading documents…" />
        ) : status === 'error' ? (
          <ErrorState message={error || 'Failed to load documents'} onRetry={refetch} />
        ) : documents.length === 0 ? (
          <EmptyState
            icon={<FileText size={40} />}
            title="No documents found"
            description={search || statusFilter || priorityFilter || sourceFilter
              ? 'Try adjusting your search or filters.'
              : 'Upload your first PDF, DOCX, or text document to get started.'}
            action={
              <Button variant="primary" onClick={() => setIsUploadOpen(true)}>
                <Plus size={14} /> Upload Document
              </Button>
            }
          />
        ) : (
          <>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Document</th>
                    <th>Type</th>
                    <th>Source</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Department</th>
                    <th>Assigned To</th>
                    <th>Updated</th>
                    <th className="cell-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {documents.map((doc) => {
                    const sourceText = DOCUMENT_SOURCE_LABELS[doc.source] || doc.source;
                    return (
                      <tr key={doc.id}>
                        <td>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <span
                              className="cell-primary"
                              style={{ cursor: 'pointer', fontWeight: 500 }}
                              onClick={() => navigate(`/documents/${doc.id}`)}
                            >
                              {doc.name}
                            </span>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                              <span className="cell-id">{doc.id}</span>
                              {doc.originalSender?.email && (
                                <span
                                  style={{
                                    fontSize: 'var(--text-caption)',
                                    color: 'var(--text-tertiary)',
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: 3,
                                  }}
                                  title={`Original Sender: ${doc.originalSender.name ? `${doc.originalSender.name} <${doc.originalSender.email}>` : doc.originalSender.email}`}
                                >
                                  <Mail size={11} />
                                  {doc.originalSender.email}
                                </span>
                              )}
                              {doc.size > 0 && (
                                <span style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>
                                  ({formatFileSize(doc.size)})
                                </span>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="cell-secondary">{doc.type}</td>
                        <td>
                          <Badge variant={doc.source === 'email' ? 'brand' : 'neutral'}>
                            {sourceText}
                          </Badge>
                        </td>
                        <td><StatusBadge status={doc.status} /></td>
                        <td><PriorityBadge priority={doc.priority} /></td>
                        <td className="cell-secondary">{doc.department || '—'}</td>
                        <td>
                          {doc.assignedTo ? (
                            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                              <Avatar name={doc.assignedTo} size="sm" />
                              <span className="cell-secondary">{doc.assignedTo}</span>
                            </div>
                          ) : (
                            <span className="cell-secondary" style={{ fontStyle: 'italic', color: 'var(--text-tertiary)' }}>Unassigned</span>
                          )}
                        </td>
                        <td className="cell-secondary" title={formatDate(doc.updatedAt)}>{formatRelativeTime(doc.updatedAt)}</td>
                        <td className="cell-actions">
                          <Dropdown
                            trigger={<Button variant="ghost" icon size="sm" aria-label="Actions"><MoreHorizontal size={14} /></Button>}
                          >
                            <DropdownItem onClick={() => navigate(`/documents/${doc.id}`)}>
                              <Eye size={14} /> View Details
                            </DropdownItem>
                            <DropdownItem onClick={() => openEmailModal(doc)}>
                              <Send size={14} /> Send via Email
                            </DropdownItem>
                            <DropdownItem onClick={() => handleDownload(doc)}>
                              <Download size={14} /> Download
                            </DropdownItem>
                            <div className="dropdown-separator" />
                            <DropdownItem onClick={() => handleDelete(doc)} className="text-error">
                              <Trash2 size={14} /> Delete
                            </DropdownItem>
                          </Dropdown>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination
              page={page}
              pageSize={pageSize}
              total={totalDocuments}
              onPageChange={setPage}
            />
          </>
        )}
      </Card>

      {/* ====================================================== */}
      {/* 1. DOCUMENT UPLOAD MODAL */}
      {/* ====================================================== */}
      {isUploadOpen && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 520, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>Upload Document</h3>
              <button
                type="button"
                onClick={() => setIsUploadOpen(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleUploadSubmit} style={{ padding: 'var(--space-5)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {uploadError && (
                  <div style={{
                    padding: '8px 12px', backgroundColor: 'var(--color-error-50, #fef2f2)',
                    border: '1px solid var(--color-error-200, #fecaca)', borderRadius: 'var(--radius-sm, 4px)',
                    color: 'var(--color-error-700, #b91c1c)', fontSize: 'var(--text-body-sm)'
                  }}>
                    {uploadError}
                  </div>
                )}

                {/* File Dropzone */}
                <div
                  onClick={() => fileInputRef.current?.click()}
                  style={{
                    border: '2px dashed var(--border-secondary, #cbd5e1)',
                    borderRadius: 'var(--radius-md, 6px)',
                    padding: 'var(--space-5)',
                    textAlign: 'center',
                    cursor: 'pointer',
                    backgroundColor: uploadFiles.length > 0 ? 'var(--color-brand-50, #f0fdf4)' : 'var(--bg-secondary, #f8fafc)',
                    transition: 'border-color 0.2s'
                  }}
                >
                  <input
                    ref={fileInputRef}
                    type="file"
                    multiple
                    accept=".pdf,.docx,.txt,.png,.jpg,.jpeg,.tiff"
                    onChange={handleFileSelect}
                    style={{ display: 'none' }}
                  />
                  {uploadFiles.length > 0 ? (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 'var(--space-2)' }}>
                      <Paperclip size={20} style={{ color: 'var(--color-brand-600, #16a34a)' }} />
                      <div style={{ textAlign: 'left' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: 'var(--text-body-sm)' }}>
                          {uploadFiles.length === 1
                            ? uploadFiles[0].name
                            : `${uploadFiles.length} files selected for Batch Processing`}
                        </div>
                        <div style={{ color: 'var(--text-secondary)', fontSize: 'var(--text-caption)' }}>
                          {uploadFiles.length === 1
                            ? `${formatFileSize(uploadFiles[0].size)} • Click to change`
                            : `${formatFileSize(uploadFiles.reduce((a, f) => a + f.size, 0))} total • Click to change`}
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div>
                      <Upload size={28} style={{ color: 'var(--color-brand-600, #2563eb)', margin: '0 auto var(--space-2)' }} />
                      <div style={{ fontWeight: 500, color: 'var(--text-primary)', fontSize: 'var(--text-body-sm)' }}>
                        Click to select or drag & drop single or batch documents
                      </div>
                      <div style={{ color: 'var(--text-tertiary)', fontSize: 'var(--text-caption)', marginTop: 4 }}>
                        Supported formats: PDF, DOCX, TXT, PNG, JPG (Select multiple for batch ingestion)
                      </div>
                    </div>
                  )}
                </div>

                {uploadFiles.length <= 1 && (
                  <Input
                    label="Document Name (Optional)"
                    placeholder="e.g. Q3 Financial Statement"
                    value={uploadName}
                    onChange={(e) => setUploadName(e.target.value)}
                  />
                )}

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                  <SelectField
                    label="Document Type / Folder"
                    value={uploadType}
                    onChange={(e) => setUploadType(e.target.value)}
                    options={[
                      { value: '', label: 'Auto-Classify (Folder Rules)' },
                      { value: 'Invoice', label: 'Invoice' },
                      { value: 'Contract', label: 'Contract' },
                      { value: 'Receipt', label: 'Receipt' },
                      { value: 'Report', label: 'Report' },
                      { value: 'General Document', label: 'General Document' }
                    ]}
                  />

                  <SelectField
                    label="Priority"
                    value={uploadPriority}
                    onChange={(e) => setUploadPriority(e.target.value as Priority)}
                    options={[
                      { value: 'medium', label: 'Auto-Sort / Medium' },
                      { value: 'critical', label: 'Critical' },
                      { value: 'high', label: 'High' },
                      { value: 'medium', label: 'Medium' },
                      { value: 'low', label: 'Low' }
                    ]}
                  />
                </div>

                <SelectField
                  label="Department (Optional)"
                  value={uploadDept}
                  onChange={(e) => setUploadDept(e.target.value)}
                  options={[
                    { value: '', label: 'None / Auto-Route' },
                    ...(departments || []).map((d) => ({ value: d.name, label: d.name }))
                  ]}
                />

                <Textarea
                  label="Description / Notes (Optional)"
                  placeholder="Additional context about this document…"
                  value={uploadDesc}
                  onChange={(e) => setUploadDesc(e.target.value)}
                  rows={2}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-6)' }}>
                <Button variant="secondary" type="button" onClick={() => setIsUploadOpen(false)} disabled={uploadLoading}>
                  Cancel
                </Button>
                <Button variant="primary" type="submit" disabled={uploadLoading || uploadFiles.length === 0}>
                  {uploadLoading ? 'Ingesting Document…' : uploadFiles.length > 1 ? `Upload & Process ${uploadFiles.length} Documents` : 'Upload & Process'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ====================================================== */}
      {/* 2. SEND DOCUMENT VIA EMAIL MODAL (Manual Delivery) */}
      {/* ====================================================== */}
      {emailDoc && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 480, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <Send size={18} style={{ color: 'var(--color-brand-600, #2563eb)' }} />
                <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>Send Document via Email</h3>
              </div>
              <button
                type="button"
                onClick={() => setEmailDoc(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleEmailSubmit} style={{ padding: 'var(--space-5)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {emailError && (
                  <div style={{
                    padding: '8px 12px', backgroundColor: 'var(--color-error-50, #fef2f2)',
                    border: '1px solid var(--color-error-200, #fecaca)', borderRadius: 'var(--radius-sm, 4px)',
                    color: 'var(--color-error-700, #b91c1c)', fontSize: 'var(--text-body-sm)'
                  }}>
                    {emailError}
                  </div>
                )}

                {/* Document Attached Preview */}
                <div style={{
                  padding: '10px 14px', backgroundColor: 'var(--bg-secondary, #f8fafc)',
                  borderRadius: 'var(--radius-md, 6px)', border: '1px solid var(--border-secondary, #e2e8f0)',
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <Paperclip size={16} style={{ color: 'var(--color-brand-600, #2563eb)' }} />
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 'var(--text-body-sm)', color: 'var(--text-primary)' }}>
                        {emailDoc.name}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>
                        Attachment ID: {emailDoc.id} {emailDoc.size > 0 ? `(${formatFileSize(emailDoc.size)})` : ''}
                      </div>
                    </div>
                  </div>
                  <Badge variant="brand">Attached</Badge>
                </div>

                <Input
                  label="Recipient Email Address"
                  type="email"
                  placeholder="recipient@example.com"
                  value={recipient}
                  onChange={(e) => setRecipient(e.target.value)}
                  required
                />

                <Input
                  label="Subject"
                  placeholder="e.g. Document Delivery: Contract Agreement"
                  value={emailSubject}
                  onChange={(e) => setEmailSubject(e.target.value)}
                  required
                />

                <Textarea
                  label="Message / Notes (Optional)"
                  placeholder="Enter a message to include in the email body…"
                  value={emailMessage}
                  onChange={(e) => setEmailMessage(e.target.value)}
                  rows={4}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-6)' }}>
                <Button variant="secondary" type="button" onClick={() => setEmailDoc(null)} disabled={emailLoading}>
                  Cancel
                </Button>
                <Button variant="primary" type="submit" disabled={emailLoading}>
                  {emailLoading ? 'Sending Email…' : 'Send Document'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
