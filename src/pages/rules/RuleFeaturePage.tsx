// ===================================================================
// E2EDocs — Feature Page Template for Organization Rules (Folder & Sorting)
// ===================================================================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus, MoreHorizontal, Trash2, Copy,
  FolderTree, ArrowUpDown, RefreshCw, Layers
} from 'lucide-react';
import {
  Button, Card, SearchInput, SelectField,
  Badge, EmptyState, LoadingState, ErrorState, Dropdown, DropdownItem, PriorityBadge
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService } from '../../services/api';
import { formatRelativeTime, formatNumber } from '../../utils';
import type { RuleType, Rule, RuleStatus } from '../../types';

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

          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            <Button
              variant="primary"
              onClick={() => navigate(`/rules/new?type=${ruleType}`)}
              style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Plus size={16} /> {createButtonLabel}
            </Button>
          </div>
        </div>

        {/* 2. Filter Bar */}
        <div style={{
          display: 'flex',
          gap: 'var(--space-3)',
          backgroundColor: 'var(--bg-elevated, #ffffff)',
          padding: '10px 14px',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-primary, #e2e8f0)',
          alignItems: 'center',
          flexWrap: 'wrap'
        }}>
          <div style={{ flex: '1 1 240px' }}>
            <SearchInput
              placeholder={`Search ${title.toLowerCase()} rules…`}
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
          <Button variant="ghost" size="sm" onClick={() => refetch()} title="Refresh list">
            <RefreshCw size={14} />
          </Button>
        </div>
      </div>

      {/* Content */}
      {status === 'loading' && <LoadingState message={`Loading ${title.toLowerCase()} rules…`} />}
      {status === 'error' && <ErrorState message={error || 'Failed to load rules'} onRetry={refetch} />}
      {status === 'success' && filteredRules.length === 0 && (
        <EmptyState
          icon={ruleType === 'folder' ? <FolderTree size={40} /> : <ArrowUpDown size={40} />}
          title={`No ${title} Rules Found`}
          description={search ? 'Try adjusting your search query.' : `Create a ${title.toLowerCase()} rule to automate document organization.`}
          action={
            <Button variant="primary" size="sm" onClick={() => navigate(`/rules/new?type=${ruleType}`)}>
              <Plus size={14} /> {createButtonLabel}
            </Button>
          }
        />
      )}

      {status === 'success' && filteredRules.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          {filteredRules.map((rule) => {
            const primaryAction = rule.actions?.[0];

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
                {/* Header */}
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
                      backgroundColor: 'var(--bg-elevated, #ffffff)',
                      border: '1px solid var(--border-primary, #cbd5e1)',
                      color: 'var(--text-secondary)',
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
                    <Button variant="secondary" size="sm" onClick={() => navigate(`/rules/${rule.id}?type=${ruleType}`)}>
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

                {/* Body: IF -> THEN */}
                <div style={{ padding: '14px 16px', display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: 'var(--space-4)' }}>
                  {/* IF Block */}
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

                  {/* THEN Block */}
                  <div style={{
                    backgroundColor: 'var(--bg-elevated, #ffffff)',
                    padding: '10px 12px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-primary, #e2e8f0)',
                    fontSize: '12px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center'
                  }}>
                    <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-tertiary)', textTransform: 'uppercase', marginBottom: 4 }}>
                      THEN OUTCOME
                    </div>

                    {ruleType === 'folder' && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <FolderTree size={16} style={{ color: '#9333ea' }} />
                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Assign Folder / Category:</span>
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

                    {ruleType === 'sorting' && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <ArrowUpDown size={16} style={{ color: '#d97706' }} />
                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>Document Sorting Priority:</span>
                        <PriorityBadge priority={primaryAction?.value || 'high'} />
                      </div>
                    )}
                  </div>
                </div>

                {/* Footer */}
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
    </div>
  );
}
