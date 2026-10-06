// ===================================================================
// E2EDocs — Standalone Feature Page Template for Automation Rules
// ===================================================================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus, MoreHorizontal, Eye, Trash2, Copy, ToggleLeft, ToggleRight,
  CheckCircle2, FolderTree, ArrowUpDown, SendHorizontal, Mail,
  Search, ShieldAlert, Sparkles, Filter, ChevronRight, FileCheck,
  Tag, Compass, Send, Layers, HelpCircle, ArrowRight
} from 'lucide-react';
import {
  Button, Card, SearchInput, StatusBadge, Badge,
  EmptyState, LoadingState, ErrorState, Dropdown, DropdownItem, PriorityBadge
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService } from '../../services/api';
import { formatDate, formatRelativeTime, formatNumber } from '../../utils';
import type { RuleType, Rule, RuleAction, RuleCondition, RuleStatus } from '../../types';

interface RuleFeaturePageProps {
  ruleType: RuleType;
  title: string;
  description: string;
  badgeLabel: string;
  createButtonLabel: string;
  icon: React.ReactNode;
  accentColor: string;
  accentBg: string;
}

export function RuleFeaturePage({
  ruleType,
  title,
  description,
  badgeLabel,
  createButtonLabel,
  icon,
  accentColor,
  accentBg,
}: RuleFeaturePageProps) {
  useDocumentTitle(`${title} — E2EDocs`);
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | RuleStatus>('all');

  const { data: rules = [], status, error, refetch } = useAsync(
    () => ruleService.getAll(ruleType),
    [ruleType]
  );
  const safeRules = rules || [];

  // Filter and sort rules
  const filteredRules = safeRules
    .filter((r) => {
      if (statusFilter !== 'all' && r.status !== statusFilter) return false;
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

  const handleToggleStatus = async (rule: Rule) => {
    try {
      const nextStatus: RuleStatus = rule.status === 'active' ? 'draft' : 'active';
      await ruleService.update(rule.id, { status: nextStatus });
      refetch();
    } catch {}
  };

  const handleDuplicateRule = async (rule: Rule) => {
    try {
      await ruleService.create({
        name: `${rule.name} (Copy)`,
        description: rule.description,
        status: 'draft',
        ruleType: rule.ruleType || ruleType,
        evaluationOrder: (rule.evaluationOrder || 1) + 1,
        conditionGroups: rule.conditionGroups,
        actions: rule.actions,
      } as any);
      refetch();
    } catch {}
  };

  const handleDeleteRule = async (rule: Rule) => {
    if (window.confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
      try {
        await ruleService.delete(rule.id);
        refetch();
      } catch {}
    }
  };

  return (
    <div style={{ maxWidth: 1240, margin: '0 auto', paddingBottom: 'var(--space-12)' }}>
      {/* 1. Header Bar */}
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
              backgroundColor: accentBg,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
              marginTop: 2,
            }}>
              {icon}
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <h1 className="page-title" style={{ fontSize: '24px', fontWeight: 700, letterSpacing: '-0.02em', margin: 0 }}>
                  {title}
                </h1>
                <span style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  padding: '2px 8px',
                  borderRadius: '12px',
                  backgroundColor: 'var(--color-gray-100, #f1f5f9)',
                  color: 'var(--text-secondary)'
                }}>
                  {filteredRules.length} {filteredRules.length === 1 ? 'Rule' : 'Rules'}
                </span>
              </div>
              <p className="page-description" style={{ fontSize: '14px', maxWidth: 740, color: 'var(--text-secondary)', marginTop: '4px' }}>
                {description}
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Button
              variant="primary"
              onClick={() => navigate(`/rules/new?type=${ruleType}`)}
            >
              <Plus size={16} /> {createButtonLabel}
            </Button>
          </div>
        </div>

        {/* 2. Search and Filter Controls */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 'var(--space-3)',
          padding: '12px 16px',
          backgroundColor: 'var(--bg-elevated, #ffffff)',
          borderRadius: 'var(--radius-lg, 8px)',
          border: '1px solid var(--border-primary, #e2e8f0)',
          boxShadow: 'var(--shadow-xs)'
        }}>
          <div style={{ flex: 1, minWidth: 260, maxWidth: 400 }}>
            <SearchInput
              placeholder={`Search ${title.toLowerCase()} & criteria…`}
              value={search}
              onChange={(e) => setSearch((e.target as HTMLInputElement).value)}
              onSearch={setSearch}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>Status:</span>
            {(['all', 'active', 'draft', 'inactive'] as const).map((st) => (
              <button
                key={st}
                type="button"
                onClick={() => setStatusFilter(st)}
                style={{
                  padding: '4px 10px',
                  borderRadius: 'var(--radius-md, 6px)',
                  fontSize: '12px',
                  fontWeight: 600,
                  textTransform: 'capitalize',
                  border: statusFilter === st ? `1px solid ${accentColor}` : '1px solid var(--border-secondary, #e2e8f0)',
                  backgroundColor: statusFilter === st ? accentBg : 'transparent',
                  color: statusFilter === st ? accentColor : 'var(--text-secondary)',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                {st}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* 3. Main Content List */}
      {status === 'loading' && <LoadingState message={`Loading ${title.toLowerCase()}…`} />}
      {status === 'error' && <ErrorState message={error || 'Failed to load rules.'} onRetry={refetch} />}

      {status === 'success' && (
        <>
          {filteredRules.length === 0 ? (
            <div style={{
              padding: 'var(--space-12) var(--space-6)',
              textAlign: 'center',
              borderRadius: 'var(--radius-xl, 12px)',
              border: '1px dashed var(--border-secondary, #cbd5e1)',
              backgroundColor: 'var(--bg-elevated, #ffffff)'
            }}>
              <div style={{
                width: 52,
                height: 52,
                borderRadius: '50%',
                backgroundColor: accentBg,
                margin: '0 auto var(--space-3)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                {icon}
              </div>
              <h3 style={{ fontSize: '17px', fontWeight: 700, margin: '0 0 6px 0', color: 'var(--text-primary)' }}>
                No {title} Found
              </h3>
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '0 auto var(--space-5)', maxWidth: 460 }}>
                {search || statusFilter !== 'all'
                  ? 'No rules matched the active search and status filters. Try clearing filters.'
                  : `Create your first rule to configure automated ${title.toLowerCase()} for documents.`}
              </p>
              <Button
                variant="primary"
                onClick={() => navigate(`/rules/new?type=${ruleType}`)}
              >
                <Plus size={16} /> {createButtonLabel}
              </Button>
            </div>
          ) : (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))',
              gap: 'var(--space-4)'
            }}>
              {filteredRules.map((rule) => (
                <RuleCardItem
                  key={rule.id}
                  rule={rule}
                  ruleType={ruleType}
                  badgeLabel={badgeLabel}
                  onEdit={() => navigate(`/rules/${rule.id}?type=${ruleType}`)}
                  onDuplicate={() => handleDuplicateRule(rule)}
                  onToggleStatus={() => handleToggleStatus(rule)}
                  onDelete={() => handleDeleteRule(rule)}
                />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

// ===================================================================
// Rule Card Item
// ===================================================================
interface RuleCardItemProps {
  rule: Rule;
  ruleType: RuleType;
  badgeLabel: string;
  onEdit: () => void;
  onDuplicate: () => void;
  onToggleStatus: () => void;
  onDelete: () => void;
}

function RuleCardItem({
  rule,
  ruleType,
  badgeLabel,
  onEdit,
  onDuplicate,
  onToggleStatus,
  onDelete,
}: RuleCardItemProps) {
  const isInactive = rule.status === 'inactive';
  const primaryAction = rule.actions && rule.actions.length > 0 ? rule.actions[0] : null;

  return (
    <div
      style={{
        border: '1px solid var(--border-primary, #e2e8f0)',
        borderRadius: 'var(--radius-lg, 10px)',
        backgroundColor: 'var(--bg-elevated, #ffffff)',
        boxShadow: 'var(--shadow-xs, 0 1px 2px rgba(0,0,0,0.04))',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        transition: 'all 0.15s ease',
        opacity: isInactive ? 0.65 : 1,
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md, 0 4px 6px -1px rgba(0,0,0,0.1))';
        (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-brand-300, #93c5fd)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-xs, 0 1px 2px rgba(0,0,0,0.04))';
        (e.currentTarget as HTMLElement).style.borderColor = 'var(--border-primary, #e2e8f0)';
      }}
    >
      {/* 1. Card Top Bar */}
      <div style={{ padding: '14px 16px 10px', borderBottom: '1px solid var(--border-secondary, #f1f5f9)' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '8px' }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px', flexWrap: 'wrap' }}>
              <span
                onClick={onEdit}
                style={{
                  fontSize: '15px',
                  fontWeight: 700,
                  color: 'var(--text-primary)',
                  cursor: 'pointer',
                  lineHeight: 1.3
                }}
              >
                {rule.name}
              </span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
              <span style={{
                fontSize: '10px',
                fontWeight: 700,
                textTransform: 'uppercase',
                padding: '2px 6px',
                borderRadius: '4px',
                backgroundColor: 'var(--color-gray-100, #f1f5f9)',
                color: 'var(--text-tertiary)',
                letterSpacing: '0.04em'
              }}>
                {badgeLabel}
              </span>

              <StatusBadge status={rule.status} />

              <span style={{
                fontSize: '11px',
                fontWeight: 600,
                color: 'var(--text-secondary)',
                backgroundColor: 'var(--bg-secondary, #f8fafc)',
                padding: '1px 6px',
                borderRadius: '4px',
                border: '1px solid var(--border-secondary, #e2e8f0)'
              }}>
                Evaluation Order: #{rule.evaluationOrder || 1}
              </span>
            </div>
          </div>

          {/* More Action Dropdown */}
          <div onClick={(e) => e.stopPropagation()}>
            <Dropdown
              trigger={
                <Button variant="ghost" icon size="sm" aria-label="Rule options">
                  <MoreHorizontal size={14} />
                </Button>
              }
            >
              <DropdownItem onClick={onEdit}>
                <Eye size={14} /> Edit Rule
              </DropdownItem>
              <DropdownItem onClick={onToggleStatus}>
                {rule.status === 'active' ? (
                  <>
                    <ToggleRight size={14} className="text-warning-600" /> Mark as Draft
                  </>
                ) : (
                  <>
                    <ToggleLeft size={14} className="text-success-600" /> Activate Rule
                  </>
                )}
              </DropdownItem>
              <DropdownItem onClick={onDuplicate}>
                <Copy size={14} /> Duplicate Rule
              </DropdownItem>
              <div className="dropdown-separator" />
              <DropdownItem className="text-error" onClick={onDelete}>
                <Trash2 size={14} /> Delete Rule
              </DropdownItem>
            </Dropdown>
          </div>
        </div>

        {rule.description && (
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: '8px 0 0', lineHeight: 1.4 }}>
            {rule.description}
          </p>
        )}
      </div>

      {/* 2. Rule Logic Body: IF Conditions -> THEN Outcomes */}
      <div style={{ padding: '12px 16px', flex: 1, display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {/* IF Block */}
        <div style={{
          backgroundColor: 'var(--bg-secondary, #f8fafc)',
          borderRadius: 'var(--radius-md, 6px)',
          padding: '8px 12px',
          border: '1px solid var(--border-secondary, #e2e8f0)',
          fontSize: '12px'
        }}>
          <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '4px' }}>
            IF CONDITIONS
          </div>
          {rule.conditionGroups && rule.conditionGroups.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              {rule.conditionGroups.map((group, gi) => (
                <div key={group.id} style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                  {gi > 0 && (
                    <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--color-brand-600)' }}>
                      OR
                    </span>
                  )}
                  {group.conditions.map((cond, ci) => (
                    <div key={cond.id} style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--text-primary)', fontFamily: 'var(--font-mono)', fontSize: '11px', flexWrap: 'wrap' }}>
                      {ci > 0 && <span style={{ color: 'var(--text-tertiary)' }}>AND</span>}
                      <span style={{ color: 'var(--color-brand-700, #1d4ed8)', fontWeight: 600 }}>{cond.field}</span>
                      <span style={{ color: 'var(--text-tertiary)' }}>{cond.operator}</span>
                      <span style={{ backgroundColor: 'var(--bg-elevated, #ffffff)', padding: '1px 5px', borderRadius: '3px', border: '1px solid var(--border-primary, #cbd5e1)', fontWeight: 600 }}>
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

        {/* THEN Block (Section-Isolated Outcome) */}
        <div style={{
          backgroundColor: 'var(--bg-primary, #ffffff)',
          borderRadius: 'var(--radius-md, 6px)',
          padding: '8px 12px',
          border: '1px solid var(--border-primary, #e2e8f0)',
          fontSize: '12px'
        }}>
          <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '4px' }}>
            THEN OUTCOME
          </div>

          {/* Section 1: DECISION OUTCOME */}
          {ruleType === 'decision' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Decision Outcome:</span>
              {primaryAction?.value?.toLowerCase() === 'approved' ? (
                <Badge variant="success">APPROVE</Badge>
              ) : primaryAction?.value?.toLowerCase() === 'rejected' ? (
                <Badge variant="error">REJECT</Badge>
              ) : (
                <Badge variant="warning">MANUAL REVIEW</Badge>
              )}
            </div>
          )}

          {/* Section 2: FOLDER OUTCOME */}
          {ruleType === 'folder' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <FolderTree size={14} style={{ color: '#9333ea' }} />
              <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Folder →</span>
              <span style={{
                fontSize: '12px',
                fontWeight: 700,
                color: '#7e22ce',
                backgroundColor: 'rgba(147, 51, 234, 0.1)',
                padding: '2px 8px',
                borderRadius: '4px'
              }}>
                {primaryAction?.value || 'Dynamic Category'}
              </span>
            </div>
          )}

          {/* Section 3: SORTING OUTCOME */}
          {ruleType === 'sorting' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Document Sorting Priority →</span>
              <PriorityBadge priority={primaryAction?.value || 'high'} />
            </div>
          )}

          {/* Section 4: ROUTING OUTCOME */}
          {ruleType === 'routing' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
              <SendHorizontal size={14} style={{ color: '#0284c7' }} />
              <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Route To →</span>
              <span style={{ fontWeight: 600, color: '#0369a1' }}>
                {primaryAction?.value || 'Configured Target'}
              </span>
              <span style={{ fontSize: '10px', color: 'var(--text-tertiary)' }}>
                ({(primaryAction?.type || 'assign_user').replace('assign_', '').replace('_', ' ')})
              </span>
            </div>
          )}

          {/* Section 5: COMMUNICATION OUTCOME */}
          {ruleType === 'communication' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '11px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', flexWrap: 'wrap' }}>
                <Mail size={12} style={{ color: '#059669' }} />
                <span style={{ color: 'var(--text-secondary)' }}>Send Email TO:</span>
                <span style={{ fontWeight: 600, color: '#047857' }}>
                  {primaryAction?.emailConfig?.recipientType || primaryAction?.value || 'original_sender'}
                </span>
              </div>
              {primaryAction?.emailConfig?.subject && (
                <div style={{ color: 'var(--text-tertiary)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                  Subject: "{primaryAction.emailConfig.subject}"
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* 3. Card Footer Action Bar */}
      <div style={{
        padding: '10px 16px',
        backgroundColor: 'var(--bg-secondary, #f8fafc)',
        borderTop: '1px solid var(--border-secondary, #f1f5f9)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '11px',
        color: 'var(--text-tertiary)'
      }}>
        <div>
          <span>{formatNumber(rule.matchCount || 0)} matches</span>
          {rule.lastTriggered && (
            <span style={{ marginLeft: '8px' }}>• {formatRelativeTime(rule.lastTriggered)}</span>
          )}
        </div>

        <div style={{ display: 'flex', gap: '6px' }}>
          <Button variant="secondary" size="sm" onClick={onEdit}>
            Edit
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={onToggleStatus}
            style={{ color: rule.status === 'active' ? 'var(--color-warning-700)' : 'var(--color-success-700)' }}
          >
            {rule.status === 'active' ? 'Disable' : 'Enable'}
          </Button>
        </div>
      </div>
    </div>
  );
}
