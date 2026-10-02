// ===========================
// E2EDocs — Workflows Page
// ===========================

import { useNavigate } from 'react-router-dom';
import { Plus, MoreHorizontal, Eye, Edit, Workflow as WorkflowIcon, CheckCircle, Clock, Circle, SkipForward } from 'lucide-react';
import {
  Button, Card, CardHeader, StatusBadge, Badge, SearchInput,
  EmptyState, LoadingState, ErrorState, Dropdown, DropdownItem,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { workflowService } from '../../services/api';
import { formatRelativeTime, formatNumber } from '../../utils';
import { useState } from 'react';
import type { WorkflowStep } from '../../types';

const STEP_ICONS: Record<string, React.ElementType> = {
  completed: CheckCircle,
  in_progress: Clock,
  pending: Circle,
  skipped: SkipForward,
};

const STEP_COLORS: Record<string, string> = {
  completed: 'var(--color-success-500)',
  in_progress: 'var(--color-brand-500)',
  pending: 'var(--color-gray-300)',
  skipped: 'var(--color-gray-300)',
};

export default function WorkflowsPage() {
  useDocumentTitle('Workflows');
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const { data: workflows, status, error, refetch } = useAsync(() => workflowService.getAll(), []);

  const filtered = (workflows || []).filter((w) =>
    !search || w.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Workflows</h1>
            <p className="page-description">Manage automated document processing workflows.</p>
          </div>
          <div className="page-actions">
            <Button variant="primary" size="sm"><Plus size={14} /> Create Workflow</Button>
          </div>
        </div>
      </div>

      <div style={{ marginBottom: 'var(--space-4)', maxWidth: 360 }}>
        <SearchInput placeholder="Search workflows…" value={search} onChange={(e) => setSearch((e.target as HTMLInputElement).value)} onSearch={setSearch} />
      </div>

      {status === 'loading' ? <LoadingState message="Loading workflows…" /> :
       status === 'error' ? <ErrorState message={error || 'Failed to load workflows'} onRetry={refetch} /> :
       filtered.length === 0 ? (
        <EmptyState icon={<WorkflowIcon size={40} />} title="No workflows" description="Create a workflow to automate document processing." action={<Button variant="primary"><Plus size={14} /> Create Workflow</Button>} />
       ) : (
        <div style={{ display: 'grid', gap: 'var(--space-4)' }}>
          {filtered.map((wf) => (
            <Card key={wf.id}>
              <CardHeader>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                    <span style={{ fontSize: 'var(--text-body)', fontWeight: 'var(--weight-medium)' as React.CSSProperties['fontWeight'] }}>{wf.name}</span>
                    <StatusBadge status={wf.status} />
                  </div>
                  <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>{wf.description}</p>
                </div>
                <Dropdown trigger={<Button variant="ghost" icon size="sm" aria-label="Actions"><MoreHorizontal size={14} /></Button>}>
                  <DropdownItem><Eye size={14} /> View</DropdownItem>
                  <DropdownItem><Edit size={14} /> Edit</DropdownItem>
                </Dropdown>
              </CardHeader>

              {/* Workflow Steps Visualization */}
              <div style={{ padding: 'var(--space-4) var(--space-5)', overflowX: 'auto' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 0, minWidth: 'fit-content' }}>
                  {wf.steps.map((step, i) => {
                    const Icon = STEP_ICONS[step.status];
                    const color = STEP_COLORS[step.status];
                    return (
                      <div key={step.id} style={{ display: 'flex', alignItems: 'center' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-1)', minWidth: 80 }}>
                          <div style={{
                            width: 28, height: 28, borderRadius: 'var(--radius-full)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            backgroundColor: step.status === 'completed' ? 'var(--color-success-50)' : step.status === 'in_progress' ? 'var(--color-brand-50)' : 'var(--color-gray-100)',
                          }}>
                            <Icon size={14} style={{ color }} />
                          </div>
                          <span style={{ fontSize: 'var(--text-label)', color: step.status === 'pending' ? 'var(--text-tertiary)' : 'var(--text-secondary)', textAlign: 'center', maxWidth: 80 }}>
                            {step.name}
                          </span>
                        </div>
                        {i < wf.steps.length - 1 && (
                          <div style={{
                            width: 32, height: 2, margin: '0 var(--space-1)',
                            backgroundColor: step.status === 'completed' ? 'var(--color-success-300)' : 'var(--color-gray-200)',
                            marginBottom: 20,
                          }} />
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Footer info */}
              <div style={{
                padding: 'var(--space-3) var(--space-5)',
                borderTop: '1px solid var(--border-secondary)',
                display: 'flex',
                flexWrap: 'wrap',
                gap: 'var(--space-2) var(--space-6)',
                fontSize: 'var(--text-caption)',
                color: 'var(--text-tertiary)',
              }}>
                <span>Trigger: {wf.trigger}</span>
                <span>Owner: {wf.owner}</span>
                <span>{formatNumber(wf.documentsProcessed)} documents processed</span>
                <span>Updated {formatRelativeTime(wf.updatedAt)}</span>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
