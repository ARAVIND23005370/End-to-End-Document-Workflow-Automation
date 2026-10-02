// ===========================
// E2EDocs — Reports Page
// ===========================

import { Card, CardHeader, CardBody, Tabs } from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { dashboardService } from '../../services/api';
import { formatNumber } from '../../utils';
import { useState } from 'react';

export default function ReportsPage() {
  useDocumentTitle('Reports');
  const [activeTab, setActiveTab] = useState('overview');

  const { data: stats } = useAsync(() => dashboardService.getStats(), []);
  const { data: statusDist } = useAsync(() => dashboardService.getStatusDistribution(), []);
  const { data: deptWorkload } = useAsync(() => dashboardService.getDepartmentWorkload(), []);

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'documents', label: 'Document Processing' },
    { id: 'departments', label: 'Department Workload' },
    { id: 'rules', label: 'Rule Activity' },
  ];

  const totalDocs = statusDist?.reduce((a, b) => a + b.value, 0) || 1;
  const maxDept = deptWorkload ? Math.max(...deptWorkload.map((d) => d.value)) : 1;

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Reports</h1>
            <p className="page-description">Analytics and insights for document workflows.</p>
          </div>
        </div>
      </div>

      <div style={{ marginBottom: 'var(--space-4)' }}>
        <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />
      </div>

      {activeTab === 'overview' && (
        <div className="form-grid-2">
          <Card>
            <CardHeader><div className="card-title">Document Status Summary</div></CardHeader>
            <CardBody>
              {statusDist?.map((s) => (
                <div key={s.label} style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: 'var(--space-2) 0', borderBottom: '1px solid var(--border-secondary)',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <span style={{ width: 8, height: 8, borderRadius: 'var(--radius-full)', backgroundColor: s.color }} />
                    <span style={{ fontSize: 'var(--text-body-sm)' }}>{s.label}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)' }}>{formatNumber(s.value)}</span>
                    <span style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', width: 40, textAlign: 'right' }}>
                      {((s.value / totalDocs) * 100).toFixed(1)}%
                    </span>
                  </div>
                </div>
              ))}
            </CardBody>
          </Card>

          <Card>
            <CardHeader><div className="card-title">Platform Summary</div></CardHeader>
            <CardBody>
              {stats && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
                  {[
                    { label: 'Total Documents', value: formatNumber(stats.totalDocuments) },
                    { label: 'Active Rules', value: stats.activeRules.toString() },
                    { label: 'Active Workflows', value: stats.activeWorkflows.toString() },
                    { label: 'Critical Items', value: stats.criticalItems.toString() },
                    { label: 'Approval Rate', value: `${((stats.approved / stats.totalDocuments) * 100).toFixed(1)}%` },
                    { label: 'Rejection Rate', value: `${((stats.rejected / stats.totalDocuments) * 100).toFixed(1)}%` },
                  ].map((item) => (
                    <div key={item.label} style={{ padding: 'var(--space-3)', border: '1px solid var(--border-secondary)', borderRadius: 'var(--radius-md)' }}>
                      <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-widest)', marginBottom: 'var(--space-1)' }}>{item.label}</div>
                      <div style={{ fontSize: 'var(--text-h3)', fontWeight: 'var(--weight-bold)' } as React.CSSProperties}>{item.value}</div>
                    </div>
                  ))}
                </div>
              )}
            </CardBody>
          </Card>

          <Card style={{ gridColumn: '1 / -1' }}>
            <CardHeader><div className="card-title">Department Workload</div></CardHeader>
            <CardBody>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                {deptWorkload?.map((d) => (
                  <div key={d.label} className="chart-bar-row">
                    <span className="chart-bar-label">{d.label}</span>
                    <div className="chart-bar-track">
                      <div className="chart-bar-fill" style={{ width: `${(d.value / maxDept) * 100}%` }} />
                    </div>
                    <span className="chart-bar-value">{d.value}</span>
                  </div>
                ))}
              </div>
            </CardBody>
          </Card>
        </div>
      )}

      {activeTab !== 'overview' && (
        <Card>
          <CardBody>
            <div style={{ textAlign: 'center', padding: 'var(--space-16)', color: 'var(--text-secondary)' }}>
              <p style={{ fontSize: 'var(--text-body-sm)' }}>Detailed {tabs.find(t => t.id === activeTab)?.label} report will be available when backend data is connected.</p>
            </div>
          </CardBody>
        </Card>
      )}
    </div>
  );
}
