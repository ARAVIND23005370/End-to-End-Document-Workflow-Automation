// ===========================
// E2EDocs — Rules Page
// ===========================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, MoreHorizontal, Eye, Edit, Trash2, Copy, GitBranch, Zap } from 'lucide-react';
import {
  Button, Card, CardHeader, SearchInput, StatusBadge, Badge,
  EmptyState, LoadingState, ErrorState,
  Dropdown, DropdownItem,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService } from '../../services/api';
import { formatDate, formatRelativeTime, formatNumber } from '../../utils';

export default function RulesPage() {
  useDocumentTitle('Rules');
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const { data: rules, status, error, refetch } = useAsync(() => ruleService.getAll(), []);

  const filtered = (rules || []).filter((r) =>
    !search || r.name.toLowerCase().includes(search.toLowerCase()) || r.description.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Rules</h1>
            <p className="page-description">Configure business logic to classify, route, and process documents.</p>
          </div>
          <div className="page-actions">
            <Button variant="primary" size="sm" onClick={() => navigate('/rules/new')}>
              <Plus size={14} /> Create Rule
            </Button>
          </div>
        </div>
      </div>

      <Card>
        <div className="filter-bar" style={{ borderBottom: '1px solid var(--border-secondary)' }}>
          <div style={{ flex: 1, maxWidth: 360 }}>
            <SearchInput placeholder="Search rules…" value={search} onChange={(e) => setSearch((e.target as HTMLInputElement).value)} onSearch={setSearch} />
          </div>
        </div>

        {status === 'loading' ? (
          <LoadingState message="Loading rules…" />
        ) : status === 'error' ? (
          <ErrorState message={error || 'Failed to load rules'} onRetry={refetch} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<GitBranch size={40} />}
            title="No rules configured"
            description={search ? 'No rules match your search.' : 'Create your first rule to start automating document workflows.'}
            action={<Button variant="primary" onClick={() => navigate('/rules/new')}><Plus size={14} /> Create Rule</Button>}
          />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {filtered.map((rule) => (
              <div
                key={rule.id}
                style={{
                  display: 'flex', alignItems: 'flex-start', gap: 'var(--space-4)',
                  padding: 'var(--space-4) var(--space-5)',
                  borderBottom: '1px solid var(--border-secondary)',
                  cursor: 'pointer',
                  transition: 'background-color var(--transition-fast)',
                }}
                onClick={() => navigate(`/rules/${rule.id}`)}
                onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.backgroundColor = 'var(--color-gray-50)'; }}
                onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.backgroundColor = ''; }}
                role="button"
                tabIndex={0}
              >
                {/* Order indicator */}
                <div style={{
                  width: 36, height: 36, borderRadius: 'var(--radius-md)',
                  backgroundColor: 'var(--color-gray-100)', display: 'flex',
                  alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                  fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)',
                  color: 'var(--text-secondary)', fontWeight: 'var(--weight-medium)' as React.CSSProperties['fontWeight'],
                }}>
                  {rule.evaluationOrder}
                </div>

                {/* Info */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                    <span style={{ fontSize: 'var(--text-body)', fontWeight: 'var(--weight-medium)' as React.CSSProperties['fontWeight'] }}>{rule.name}</span>
                    <StatusBadge status={rule.status} />
                  </div>
                  <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
                    {rule.description}
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--space-2) var(--space-4)', fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>
                    <span>{rule.conditionGroups.reduce((a, g) => a + g.conditions.length, 0)} conditions</span>
                    <span>{rule.actions.length} actions</span>
                    <span>{formatNumber(rule.matchCount)} matches</span>
                    {rule.lastTriggered && <span>Last triggered {formatRelativeTime(rule.lastTriggered)}</span>}
                  </div>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', gap: 'var(--space-1)', flexShrink: 0 }} onClick={(e) => e.stopPropagation()}>
                  <Dropdown
                    trigger={<Button variant="ghost" icon size="sm" aria-label="Actions"><MoreHorizontal size={14} /></Button>}
                  >
                    <DropdownItem onClick={() => navigate(`/rules/${rule.id}`)}><Eye size={14} /> View</DropdownItem>
                    <DropdownItem><Edit size={14} /> Edit</DropdownItem>
                    <DropdownItem><Copy size={14} /> Duplicate</DropdownItem>
                    <div className="dropdown-separator" />
                    <DropdownItem><Trash2 size={14} /> Delete</DropdownItem>
                  </Dropdown>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
