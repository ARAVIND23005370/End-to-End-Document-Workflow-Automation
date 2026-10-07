// ===================================================================
// E2EDocs — First-Class Feature Page: Decision Automation
// ===================================================================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  CheckCircle2, Plus, Search, Filter, Play, CheckCircle, XCircle,
  FileText, SendHorizontal, Mail, Eye, Download, Trash2, Copy,
  MoreHorizontal, RefreshCw, AlertTriangle, AlertCircle, Sparkles,
  Layers, Clock, ShieldCheck, Check, ArrowRight, UserCheck
} from 'lucide-react';
import {
  Button, Card, CardHeader, CardBody, SearchInput, SelectField,
  Badge, StatusBadge, PriorityBadge, EmptyState, LoadingState, ErrorState,
  Dropdown, DropdownItem, Textarea
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService, documentService } from '../../services/api';
import { formatDate, formatRelativeTime, formatNumber, formatFileSize } from '../../utils';
import type { Rule, RuleStatus, Document } from '../../types';

export default function DecisionRulesPage() {
  useDocumentTitle('Decision Automation — E2EDocs');
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<'all' | 'approved' | 'rejected' | 'review' | 'manual_queue' | 'simulator'>('all');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | RuleStatus>('all');

  // Fetch all decision rules from backend
  const { data: rules = [], status: rulesStatus, error: rulesError, refetch: refetchRules } = useAsync(
    () => ruleService.getAll('decision'),
    []
  );

  // Fetch pending review documents for the Manual Review Queue tab
  const { data: reviewDocsResult, status: docsStatus, refetch: refetchDocs } = useAsync(
    () => documentService.getAll({ status: 'review' }, { page: 1, pageSize: 50, sortBy: 'updatedAt', sortOrder: 'desc' }),
    []
  );

  const reviewDocuments: Document[] = reviewDocsResult?.data || [];
  const safeRules: Rule[] = rules || [];

  // Filter rules by search & outcome tab
  const filteredRules = safeRules
    .filter((r) => {
      if (statusFilter !== 'all' && r.status !== statusFilter) return false;
      if (activeTab === 'approved' || activeTab === 'rejected' || activeTab === 'review') {
        const primaryOutcome = r.actions?.find((a) => a.type === 'set_decision')?.value?.toLowerCase();
        if (primaryOutcome !== activeTab) return false;
      }
      if (!search.trim()) return true;
      const q = search.toLowerCase();
      return (
        r.name.toLowerCase().includes(q) ||
        (r.description && r.description.toLowerCase().includes(q)) ||
        (r.actions && r.actions.some((a) => a.value?.toLowerCase().includes(q))) ||
        (r.conditionGroups &&
          r.conditionGroups.some((g) =>
            g.conditions.some((c) =>
              c.field.toLowerCase().includes(q) ||
              c.value.toLowerCase().includes(q) ||
              c.operator.toLowerCase().includes(q)
            )
          ))
      );
    })
    .sort((a, b) => (a.evaluationOrder || 1) - (b.evaluationOrder || 1));

  // Count stats
  const totalRulesCount = safeRules.length;
  const approvedRulesCount = safeRules.filter((r) => r.actions?.some((a) => a.type === 'set_decision' && a.value?.toLowerCase() === 'approved')).length;
  const rejectedRulesCount = safeRules.filter((r) => r.actions?.some((a) => a.type === 'set_decision' && a.value?.toLowerCase() === 'rejected')).length;
  const reviewRulesCount = safeRules.filter((r) => r.actions?.some((a) => a.type === 'set_decision' && a.value?.toLowerCase() === 'review')).length;
  const pendingManualReviewsCount = reviewDocuments.length;

  // Simulator state
  const [simText, setSimText] = useState('');
  const [simResult, setSimResult] = useState<any>(null);
  const [simLoading, setSimLoading] = useState(false);

  // Notifications
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showNotification = (message: string, type: 'success' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 4000);
  };

  const handleToggleStatus = async (rule: Rule) => {
    try {
      const nextStatus: RuleStatus = rule.status === 'active' ? 'draft' : 'active';
      await ruleService.update(rule.id, { status: nextStatus });
      showNotification(`Rule "${rule.name}" is now ${nextStatus}.`);
      refetchRules();
    } catch {}
  };

  const handleDuplicateRule = async (rule: Rule) => {
    try {
      await ruleService.create({
        name: `${rule.name} (Copy)`,
        description: rule.description,
        status: 'draft',
        ruleType: 'decision',
        evaluationOrder: (rule.evaluationOrder || 1) + 1,
        conditionGroups: rule.conditionGroups,
        actions: rule.actions,
      } as any);
      showNotification(`Duplicated rule "${rule.name}".`);
      refetchRules();
    } catch {}
  };

  const handleDeleteRule = async (rule: Rule) => {
    if (window.confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
      try {
        await ruleService.delete(rule.id);
        showNotification(`Deleted rule "${rule.name}".`);
        refetchRules();
      } catch {}
    }
  };

  // Reviewer actions on manual review documents
  const handleReviewDecision = async (doc: Document, nextStatus: 'approved' | 'rejected') => {
    try {
      await documentService.update(doc.id, { status: nextStatus });
      showNotification(`Document "${doc.name}" has been ${nextStatus.toUpperCase()}.`);
      refetchDocs();
    } catch (e: any) {
      showNotification(e.message || 'Failed to update document status', 'error');
    }
  };

  const handleRunSimulation = async () => {
    if (!simText.trim()) {
      showNotification('Please enter sample document text for simulation.', 'error');
      return;
    }
    setSimLoading(true);
    try {
      const activeDecisionRules = safeRules.filter((r) => r.status === 'active').sort((a, b) => (a.evaluationOrder || 1) - (b.evaluationOrder || 1));
      if (activeDecisionRules.length === 0) {
        setSimResult({ matched: false, logs: ['No active decision rules found to evaluate.'] });
        return;
      }
      
      const firstRule = activeDecisionRules[0];
      const result = await ruleService.testRule({
        rule: firstRule,
        testContext: {
          'extracted.text': simText,
          'document.name': 'Simulator_Document.pdf',
          'sender.email': 'sender@example.org',
          'file.extension': '.pdf',
          'document.status': 'processing'
        }
      });
      setSimResult({
        ...result,
        evaluatedRuleName: firstRule.name,
        evaluationOrder: firstRule.evaluationOrder
      });
    } catch (err: any) {
      setSimResult({ matched: false, logs: ['Simulation error: ' + err.message] });
    } finally {
      setSimLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 1240, margin: '0 auto', paddingBottom: 'var(--space-12)' }}>
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

      {/* 1. Page Header */}
      <div style={{ marginBottom: 'var(--space-6)' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: 'var(--space-4)',
          marginBottom: 'var(--space-4)'
        }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--space-3)' }}>
            <div style={{
              width: 44,
              height: 44,
              borderRadius: 'var(--radius-lg, 8px)',
              backgroundColor: 'rgba(37, 99, 235, 0.08)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
              marginTop: 2,
            }}>
              <CheckCircle2 size={24} style={{ color: 'var(--color-brand-600, #2563eb)' }} />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <h1 className="page-title" style={{ fontSize: '24px', fontWeight: 700, letterSpacing: '-0.02em', margin: 0 }}>
                  Decision Automation
                </h1>
                <Badge variant="brand">DECISION RULES</Badge>
              </div>
              <p className="page-description" style={{ fontSize: '14px', maxWidth: 740, color: 'var(--text-secondary)', marginTop: '4px' }}>
                Configure rules that evaluate extracted documents in priority order and determine outcomes (APPROVE, REJECT, or MANUAL REVIEW) with integrated routing and communication.
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            <Button
              variant="primary"
              onClick={() => navigate('/rules/new?type=decision')}
              style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Plus size={16} /> Create Decision Rule
            </Button>
          </div>
        </div>

        {/* 2. Top Stats Overview Bar */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: 'var(--space-3)',
          marginBottom: 'var(--space-5)'
        }}>
          <div style={{ padding: '12px 16px', backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-primary)' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontWeight: 600, textTransform: 'uppercase' }}>Total Decision Rules</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)', marginTop: 2 }}>{totalRulesCount}</div>
          </div>
          <div style={{ padding: '12px 16px', backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-primary)' }}>
            <div style={{ fontSize: '11px', color: 'var(--color-success-700)', fontWeight: 600, textTransform: 'uppercase' }}>Auto-Approve Rules</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-success-700)', marginTop: 2 }}>{approvedRulesCount}</div>
          </div>
          <div style={{ padding: '12px 16px', backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-primary)' }}>
            <div style={{ fontSize: '11px', color: 'var(--color-error-700)', fontWeight: 600, textTransform: 'uppercase' }}>Rejection Rules</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-error-700)', marginTop: 2 }}>{rejectedRulesCount}</div>
          </div>
          <div style={{ padding: '12px 16px', backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-primary)' }}>
            <div style={{ fontSize: '11px', color: 'var(--color-warning-700)', fontWeight: 600, textTransform: 'uppercase' }}>Manual Review Rules</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-warning-700)', marginTop: 2 }}>{reviewRulesCount}</div>
          </div>
          <div
            onClick={() => setActiveTab('manual_queue')}
            style={{
              padding: '12px 16px',
              backgroundColor: pendingManualReviewsCount > 0 ? 'var(--color-warning-50, #fffbeb)' : 'var(--bg-elevated, #ffffff)',
              borderRadius: 'var(--radius-md)',
              border: `1px solid ${pendingManualReviewsCount > 0 ? 'var(--color-warning-300, #fcd34d)' : 'var(--border-primary)'}`,
              cursor: 'pointer'
            }}
          >
            <div style={{ fontSize: '11px', color: 'var(--color-warning-800)', fontWeight: 600, textTransform: 'uppercase', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span>Manual Review Queue</span>
              {pendingManualReviewsCount > 0 && <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: 'var(--color-warning-600)' }} />}
            </div>
            <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-warning-800)', marginTop: 2 }}>
              {pendingManualReviewsCount} {pendingManualReviewsCount === 1 ? 'doc' : 'docs'}
            </div>
          </div>
        </div>

        {/* 3. Navigation Tabs */}
        <div style={{
          display: 'flex',
          borderBottom: '1px solid var(--border-primary, #e2e8f0)',
          gap: 'var(--space-2)',
          overflowX: 'auto'
        }}>
          {[
            { id: 'all', label: `All Decision Rules (${safeRules.length})` },
            { id: 'approved', label: `Approve (${approvedRulesCount})` },
            { id: 'rejected', label: `Reject (${rejectedRulesCount})` },
            { id: 'review', label: `Manual Review (${reviewRulesCount})` },
            { id: 'manual_queue', label: `Manual Review Queue (${pendingManualReviewsCount})` },
            { id: 'simulator', label: 'Test & Simulate Rules' },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id as any)}
              style={{
                padding: '10px 16px',
                border: 'none',
                borderBottom: activeTab === tab.id ? '2px solid var(--color-brand-600, #2563eb)' : '2px solid transparent',
                backgroundColor: 'transparent',
                fontWeight: activeTab === tab.id ? 700 : 500,
                fontSize: '13px',
                color: activeTab === tab.id ? 'var(--color-brand-600, #2563eb)' : 'var(--text-secondary)',
                cursor: 'pointer',
                whiteSpace: 'nowrap',
                transition: 'all 0.15s ease'
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* ========================================================= */}
      {/* TAB: MANUAL REVIEW QUEUE (Reviewer Workspace) */}
      {/* ========================================================= */}
      {activeTab === 'manual_queue' && (
        <Card>
          <CardHeader>
            <div>
              <div className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <UserCheck size={18} style={{ color: 'var(--color-warning-700)' }} />
                <span>Documents Pending Manual Review</span>
              </div>
              <div className="card-subtitle">
                Inspect documents flagged for human verification, missing fields, or rule discrepancies
              </div>
            </div>
            <Button variant="ghost" size="sm" onClick={() => refetchDocs()}>
              <RefreshCw size={14} /> Refresh
            </Button>
          </CardHeader>

          {reviewDocuments.length === 0 ? (
            <EmptyState
              icon={<CheckCircle2 size={40} />}
              title="Manual Review Queue is Empty"
              description="All uploaded documents have been conclusively processed or approved."
            />
          ) : (
            <div className="table-wrapper">
              <table className="table" aria-label="Manual review queue">
                <thead>
                  <tr>
                    <th>Document</th>
                    <th>Decision Reason & Missing Fields</th>
                    <th>Priority</th>
                    <th>Assigned To</th>
                    <th>Uploaded</th>
                    <th style={{ textAlign: 'right' }}>Reviewer Action</th>
                  </tr>
                </thead>
                <tbody>
                  {reviewDocuments.map((doc) => (
                    <tr key={doc.id} onClick={() => navigate(`/documents/${doc.id}`)} style={{ cursor: 'pointer' }}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                          <div style={{
                            width: 32, height: 32, borderRadius: 'var(--radius-md)',
                            backgroundColor: 'var(--color-warning-50, #fffbeb)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center'
                          }}>
                            <FileText size={16} style={{ color: 'var(--color-warning-700, #b45309)' }} />
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{doc.name}</div>
                            <div style={{ fontSize: '11px', color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                              {doc.id} {doc.size > 0 && `• ${formatFileSize(doc.size)}`}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td style={{ maxWidth: 320 }}>
                        {doc.decisionReason && (
                          <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)' }}>
                            {doc.decisionReason}
                          </div>
                        )}
                        {doc.missingFields && (
                          <div style={{ fontSize: '11px', color: 'var(--color-error-700)', marginTop: 2 }}>
                            <strong>Missing:</strong> {doc.missingFields}
                          </div>
                        )}
                      </td>
                      <td><PriorityBadge priority={doc.priority} /></td>
                      <td>
                        <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                          {doc.assignedTo || doc.department || 'Unassigned'}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: '11px', color: 'var(--text-tertiary)' }}>
                          {formatRelativeTime(doc.updatedAt)}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '6px' }}>
                          <Button
                            variant="primary"
                            size="sm"
                            style={{ backgroundColor: 'var(--color-success-600, #16a34a)', borderColor: 'var(--color-success-600, #16a34a)' }}
                            onClick={() => handleReviewDecision(doc, 'approved')}
                          >
                            <Check size={14} /> Approve
                          </Button>
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleReviewDecision(doc, 'rejected')}
                          >
                            <XCircle size={14} /> Reject
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      )}

      {/* ========================================================= */}
      {/* TAB: RULE SIMULATOR */}
      {/* ========================================================= */}
      {activeTab === 'simulator' && (
        <Card>
          <CardHeader>
            <div>
              <div className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Play size={18} style={{ color: 'var(--color-brand-600)' }} />
                <span>Decision Rule Simulator</span>
              </div>
              <div className="card-subtitle">
                Evaluate test document text against all active decision rules in configured priority order
              </div>
            </div>
            <Button variant="primary" size="sm" onClick={handleRunSimulation} disabled={simLoading}>
              <Play size={14} /> {simLoading ? 'Evaluating…' : 'Run Decision Simulation'}
            </Button>
          </CardHeader>
          <CardBody>
            <Textarea
              label="Sample Document Extracted Text"
              placeholder="Paste extracted document text (e.g. INVOICE #1024, TOTAL: $14,500, VENDOR: ACME SUPPLIES)..."
              value={simText}
              onChange={(e) => setSimText(e.target.value)}
              rows={5}
            />

            {simResult && (
              <div style={{
                marginTop: 'var(--space-4)',
                padding: 'var(--space-4)',
                borderRadius: 'var(--radius-md)',
                border: `1px solid ${simResult.matched ? 'var(--color-success-300)' : 'var(--border-primary)'}`,
                backgroundColor: simResult.matched ? 'var(--color-success-50, #f0fdf4)' : 'var(--bg-secondary, #f8fafc)',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--space-2)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    {simResult.matched ? <CheckCircle2 size={18} className="text-success-600" /> : <AlertCircle size={18} className="text-warning-600" />}
                    <strong style={{ fontSize: '14px' }}>
                      {simResult.matched ? `Matched Rule: "${simResult.evaluatedRuleName || 'Decision Rule'}"` : 'No Definite Rule Matched (Fallback to Manual Review)'}
                    </strong>
                  </div>
                  {simResult.matched && (
                    <Badge variant="success">Rule Matched</Badge>
                  )}
                </div>

                {simResult.logs && simResult.logs.length > 0 && (
                  <div style={{ marginTop: 'var(--space-2)', fontSize: '12px', fontFamily: 'var(--font-mono)' }}>
                    <div style={{ fontWeight: 600, marginBottom: 4, color: 'var(--text-secondary)' }}>Evaluation Trace:</div>
                    <ul style={{ margin: 0, paddingLeft: 'var(--space-4)' }}>
                      {simResult.logs.map((log: string, idx: number) => (
                        <li key={idx} style={{ color: 'var(--text-secondary)' }}>{log}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </CardBody>
        </Card>
      )}

      {/* ========================================================= */}
      {/* TAB: DECISION RULES LIST (All / Approved / Rejected / Review) */}
      {/* ========================================================= */}
      {(activeTab === 'all' || activeTab === 'approved' || activeTab === 'rejected' || activeTab === 'review') && (
        <>
          {/* Search & Filter Bar */}
          <div style={{
            display: 'flex',
            gap: 'var(--space-3)',
            marginBottom: 'var(--space-4)',
            backgroundColor: 'var(--bg-elevated, #ffffff)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-primary, #e2e8f0)',
            alignItems: 'center',
            flexWrap: 'wrap'
          }}>
            <div style={{ flex: '1 1 240px' }}>
              <SearchInput
                placeholder="Search decision rules by name, condition, value, destination…"
                value={search}
                onChange={(e) => setSearch((e.target as HTMLInputElement).value)}
                onSearch={setSearch}
              />
            </div>
            <SelectField
              options={[
                { value: 'all', label: 'All Statuses' },
                { value: 'active', label: 'Active Only' },
                { value: 'draft', label: 'Draft Only' },
                { value: 'inactive', label: 'Inactive Only' },
              ]}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as any)}
            />
            {search && (
              <Button variant="ghost" size="sm" onClick={() => setSearch('')}>
                Clear
              </Button>
            )}
            <Button variant="ghost" size="sm" onClick={() => refetchRules()} title="Refresh rules list">
              <RefreshCw size={14} />
            </Button>
          </div>

          {/* List Content */}
          {rulesStatus === 'loading' && <LoadingState message="Loading decision rules…" />}
          {rulesStatus === 'error' && <ErrorState message={rulesError || 'Failed to load rules'} onRetry={refetchRules} />}
          {rulesStatus === 'success' && filteredRules.length === 0 && (
            <EmptyState
              icon={<CheckCircle2 size={40} />}
              title="No Decision Rules Configured"
              description={search ? 'No decision rules match your filter criteria.' : 'Create a decision rule to automatically evaluate and approve, reject, or route incoming documents.'}
              action={
                <Button variant="primary" size="sm" onClick={() => navigate('/rules/new?type=decision')}>
                  <Plus size={14} /> Create Decision Rule
                </Button>
              }
            />
          )}

          {rulesStatus === 'success' && filteredRules.length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              {filteredRules.map((rule) => {
                const decisionAction = rule.actions?.find((a) => a.type === 'set_decision');
                const routingAction = rule.actions?.find((a) => a.type === 'assign_user' || a.type === 'assign_department' || a.type === 'assign_team' || a.type === 'assign_queue' || a.type === 'start_workflow');
                const commAction = rule.actions?.find((a) => a.type === 'send_email');

                const outcomeVal = decisionAction?.value?.toLowerCase() || 'approved';

                return (
                  <div
                    key={rule.id}
                    style={{
                      backgroundColor: 'var(--bg-elevated, #ffffff)',
                      borderRadius: 'var(--radius-md, 8px)',
                      border: '1px solid var(--border-primary, #e2e8f0)',
                      boxShadow: 'var(--shadow-xs, 0 1px 2px rgba(0,0,0,0.04))',
                      overflow: 'hidden',
                      display: 'flex',
                      flexDirection: 'column'
                    }}
                  >
                    {/* 1. Header Bar */}
                    <div style={{
                      padding: '12px 16px',
                      borderBottom: '1px solid var(--border-secondary, #f1f5f9)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'flex-start',
                      backgroundColor: 'var(--bg-secondary, #f8fafc)'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                        <div style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          backgroundColor: 'var(--color-brand-50, #eff6ff)',
                          border: '1px solid var(--color-brand-200, #bfdbfe)',
                          color: 'var(--color-brand-700, #1d4ed8)',
                          fontSize: '11px',
                          fontWeight: 700,
                          fontFamily: 'var(--font-mono)'
                        }}>
                          Priority #{rule.evaluationOrder || 1}
                        </div>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                            <h3 style={{ fontSize: '15px', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
                              {rule.name}
                            </h3>
                            <span style={{
                              fontSize: '10px',
                              fontWeight: 700,
                              textTransform: 'uppercase',
                              padding: '1px 6px',
                              borderRadius: '4px',
                              backgroundColor: rule.status === 'active' ? 'var(--color-success-50, #f0fdf4)' : 'var(--color-gray-100, #f1f5f9)',
                              color: rule.status === 'active' ? 'var(--color-success-700, #15803d)' : 'var(--text-tertiary)',
                              border: `1px solid ${rule.status === 'active' ? 'var(--color-success-200)' : 'var(--border-secondary)'}`
                            }}>
                              {rule.status}
                            </span>
                          </div>
                          {rule.description && (
                            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: 2 }}>
                              {rule.description}
                            </div>
                          )}
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Button variant="secondary" size="sm" onClick={() => navigate(`/rules/${rule.id}?type=decision`)}>
                          Edit
                        </Button>
                        <Dropdown
                          trigger={
                            <Button variant="ghost" size="sm" icon aria-label="Rule options">
                              <MoreHorizontal size={14} />
                            </Button>
                          }
                        >
                          <DropdownItem onClick={() => handleToggleStatus(rule)}>
                            {rule.status === 'active' ? 'Disable Rule' : 'Enable Rule'}
                          </DropdownItem>
                          <DropdownItem onClick={() => handleDuplicateRule(rule)}>
                            <Copy size={14} /> Duplicate Rule
                          </DropdownItem>
                          <div className="dropdown-separator" />
                          <DropdownItem className="text-error" onClick={() => handleDeleteRule(rule)}>
                            <Trash2 size={14} /> Delete Rule
                          </DropdownItem>
                        </Dropdown>
                      </div>
                    </div>

                    {/* 2. Logic Body: IF Conditions -> THEN Outcomes + Actions */}
                    <div style={{ padding: '14px 16px', display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: 'var(--space-4)' }}>
                      {/* IF CONDITIONS BLOCK */}
                      <div style={{
                        backgroundColor: 'var(--bg-secondary, #f8fafc)',
                        padding: '10px 12px',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border-secondary, #e2e8f0)',
                        fontSize: '12px'
                      }}>
                        <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-tertiary)', textTransform: 'uppercase', marginBottom: 6 }}>
                          IF CONDITIONS
                        </div>
                        {rule.conditionGroups && rule.conditionGroups.length > 0 ? (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            {rule.conditionGroups.map((group, gi) => (
                              <div key={group.id} style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                {gi > 0 && <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--color-brand-600)' }}>OR</span>}
                                {group.conditions.map((cond, ci) => (
                                  <div key={cond.id} style={{ fontFamily: 'var(--font-mono)', fontSize: '11px', display: 'flex', alignItems: 'center', gap: 4, flexWrap: 'wrap' }}>
                                    {ci > 0 && <span style={{ color: 'var(--text-tertiary)' }}>AND</span>}
                                    <span style={{ color: 'var(--color-brand-700, #1d4ed8)', fontWeight: 600 }}>{cond.field}</span>
                                    <span style={{ color: 'var(--text-tertiary)' }}>{cond.operator}</span>
                                    <span style={{ backgroundColor: 'var(--bg-elevated, #ffffff)', padding: '1px 5px', borderRadius: '3px', border: '1px solid var(--border-primary)', fontWeight: 600 }}>
                                      "{cond.value}"
                                    </span>
                                  </div>
                                ))}
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span style={{ color: 'var(--text-tertiary)', fontStyle: 'italic' }}>Always applies (No conditions)</span>
                        )}
                      </div>

                      {/* THEN DECISION OUTCOME & ACTIONS BLOCK */}
                      <div style={{
                        backgroundColor: 'var(--bg-elevated, #ffffff)',
                        padding: '10px 12px',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border-primary, #e2e8f0)',
                        fontSize: '12px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 8
                      }}>
                        <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-tertiary)', textTransform: 'uppercase', marginBottom: 2 }}>
                          THEN DECISION OUTCOME & ACTIONS
                        </div>

                        {/* Primary Outcome Badge */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Decision:</span>
                          {outcomeVal === 'approved' && <Badge variant="success">APPROVE</Badge>}
                          {outcomeVal === 'rejected' && <Badge variant="error">REJECT</Badge>}
                          {outcomeVal === 'review' && <Badge variant="warning">MANUAL REVIEW</Badge>}
                        </div>

                        {/* Attached Routing Action */}
                        {routingAction && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '11px' }}>
                            <SendHorizontal size={13} style={{ color: '#0284c7' }} />
                            <span style={{ color: 'var(--text-secondary)' }}>Route To:</span>
                            <span style={{ fontWeight: 600, color: '#0369a1' }}>{routingAction.value}</span>
                            <span style={{ fontSize: '10px', color: 'var(--text-tertiary)' }}>({routingAction.type.replace('assign_', '')})</span>
                          </div>
                        )}

                        {/* Attached Communication Action */}
                        {commAction && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '11px' }}>
                            <Mail size={13} style={{ color: '#059669' }} />
                            <span style={{ color: 'var(--text-secondary)' }}>Automated Email:</span>
                            <span style={{ fontWeight: 600, color: '#047857' }}>
                              {commAction.emailConfig?.recipientType || commAction.value || 'Original Sender'}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* 3. Footer Stats Bar */}
                    <div style={{
                      padding: '8px 16px',
                      backgroundColor: 'var(--bg-secondary, #f8fafc)',
                      borderTop: '1px solid var(--border-secondary, #f1f5f9)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      fontSize: '11px',
                      color: 'var(--text-tertiary)'
                    }}>
                      <div>
                        <span>{formatNumber(rule.matchCount || 0)} matches</span>
                        {rule.lastTriggered && <span style={{ marginLeft: 8 }}>• Last triggered {formatRelativeTime(rule.lastTriggered)}</span>}
                      </div>
                      <div>Evaluation Order: #{rule.evaluationOrder || 1}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
