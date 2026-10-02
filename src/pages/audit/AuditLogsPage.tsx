// ===========================
// E2EDocs — Audit Logs Page
// ===========================

import { useState } from 'react';
import { Download, ScrollText } from 'lucide-react';
import {
  Button, Card, CardHeader, SearchInput, SelectField,
  Badge, Avatar, EmptyState, LoadingState, ErrorState, Pagination,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { auditService } from '../../services/api';
import { formatDate } from '../../utils';
import type { AuditStatus } from '../../types';

const STATUS_VARIANT: Record<AuditStatus, 'success' | 'error' | 'warning'> = {
  success: 'success',
  failure: 'error',
  warning: 'warning',
};

export default function AuditLogsPage() {
  useDocumentTitle('Audit Logs');
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState('');

  const { data, status, error, refetch } = useAsync(() => auditService.getAll(), []);

  const entries = data?.data || [];
  const filtered = entries.filter((e) => {
    if (search && !e.userName.toLowerCase().includes(search.toLowerCase()) && !e.details.toLowerCase().includes(search.toLowerCase())) return false;
    if (actionFilter && e.action !== actionFilter) return false;
    return true;
  });

  const actionOptions = [...new Set(entries.map((e) => e.action))].map((a) => ({
    value: a, label: a.charAt(0).toUpperCase() + a.slice(1),
  }));

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Audit Logs</h1>
            <p className="page-description">Complete audit trail of platform activity.</p>
          </div>
          <div className="page-actions">
            <Button variant="secondary" size="sm"><Download size={14} /> Export</Button>
          </div>
        </div>
      </div>

      <Card>
        <div className="filter-bar" style={{ borderBottom: '1px solid var(--border-secondary)' }}>
          <div style={{ flex: 1, maxWidth: 300 }}>
            <SearchInput placeholder="Search audit logs…" value={search} onChange={(e) => setSearch((e.target as HTMLInputElement).value)} onSearch={setSearch} />
          </div>
          <SelectField options={actionOptions} placeholder="All actions" value={actionFilter} onChange={(e) => setActionFilter(e.target.value)} />
        </div>

        {status === 'loading' ? <LoadingState message="Loading audit logs…" /> :
         status === 'error' ? <ErrorState message={error || 'Failed to load audit logs'} onRetry={refetch} /> :
         filtered.length === 0 ? (
          <EmptyState icon={<ScrollText size={40} />} title="No audit entries" description="No audit log entries match your filters." />
         ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Resource</th>
                  <th>Status</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((entry) => (
                  <tr key={entry.id}>
                    <td>
                      <span style={{ fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)', color: 'var(--text-secondary)' }}>
                        {formatDate(entry.timestamp, { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <Avatar name={entry.userName} size="sm" />
                        <span>{entry.userName}</span>
                      </div>
                    </td>
                    <td>
                      <Badge variant="neutral">
                        {entry.action.charAt(0).toUpperCase() + entry.action.slice(1)}
                      </Badge>
                    </td>
                    <td>
                      <span className="cell-secondary">{entry.resource}</span>
                      <span className="cell-id" style={{ display: 'block' }}>{entry.resourceId}</span>
                    </td>
                    <td>
                      <Badge variant={STATUS_VARIANT[entry.status]} dot>
                        {entry.status.charAt(0).toUpperCase() + entry.status.slice(1)}
                      </Badge>
                    </td>
                    <td style={{ maxWidth: 300, fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
                      {entry.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
