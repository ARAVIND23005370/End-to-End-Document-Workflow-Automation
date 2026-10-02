// ===========================
// E2EDocs — Dashboard Page
// ===========================

import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, ArrowDownRight, FileText, GitBranch, Workflow as WorkflowIcon, AlertTriangle } from 'lucide-react';
import { Card, CardHeader, CardBody, StatusBadge, PriorityBadge, Button, Skeleton } from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { dashboardService } from '../../services/api';
import { formatRelativeTime, formatNumber } from '../../utils';
import '../../styles/dashboard.css';

export default function DashboardPage() {
  useDocumentTitle('Dashboard');
  const navigate = useNavigate();

  const { data: stats } = useAsync(() => dashboardService.getStats(), []);
  const { data: statusDist } = useAsync(() => dashboardService.getStatusDistribution(), []);
  const { data: deptWorkload } = useAsync(() => dashboardService.getDepartmentWorkload(), []);
  const { data: weeklyActivity } = useAsync(() => dashboardService.getWeeklyActivity(), []);
  const { data: recentDocs } = useAsync(() => dashboardService.getRecentDocuments(), []);
  const { data: recentActivity } = useAsync(() => dashboardService.getRecentActivity(), []);

  const statCards = stats ? [
    { label: 'Total Documents', value: stats.totalDocuments, change: 12.5, icon: FileText },
    { label: 'Processing', value: stats.processing, change: -3.2, icon: WorkflowIcon },
    { label: 'In Review', value: stats.review, change: 8.1, icon: GitBranch },
    { label: 'Approved', value: stats.approved, change: 15.3, icon: FileText },
    { label: 'Rejected', value: stats.rejected, change: -5.0, icon: AlertTriangle },
    { label: 'Critical Items', value: stats.criticalItems, change: 0, icon: AlertTriangle },
  ] : [];

  const maxDeptValue = deptWorkload ? Math.max(...deptWorkload.map((d) => d.value)) : 1;
  const maxWeeklyValue = weeklyActivity ? Math.max(...weeklyActivity.map((d) => d.value)) : 1;

  const AUDIT_DOT_MAP: Record<string, string> = {
    approved: 'activity-dot-success',
    uploaded: 'activity-dot-info',
    reviewed: 'activity-dot-info',
    created: 'activity-dot-info',
    rejected: 'activity-dot-error',
    routed: 'activity-dot-warning',
    assigned: 'activity-dot-warning',
  };

  return (
    <div>
      {/* Stats row */}
      <div className="dashboard-stats">
        {!stats
          ? Array.from({ length: 6 }).map((_, i) => (
              <Card key={i}>
                <div className="stat-card">
                  <Skeleton width="60%" height={12} />
                  <div style={{ marginTop: 'var(--space-3)' }}><Skeleton width={80} height={28} /></div>
                  <div style={{ marginTop: 'var(--space-2)' }}><Skeleton width={60} height={12} /></div>
                </div>
              </Card>
            ))
          : statCards.map((stat) => (
              <Card key={stat.label}>
                <div className="stat-card">
                  <div className="stat-label">{stat.label}</div>
                  <div className="stat-value">{formatNumber(stat.value)}</div>
                  {stat.change !== 0 && (
                    <div className={`stat-change ${stat.change > 0 ? 'stat-change-positive' : 'stat-change-negative'}`}>
                      {stat.change > 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                      {Math.abs(stat.change)}% vs last month
                    </div>
                  )}
                </div>
              </Card>
            ))}
      </div>

      {/* Charts row */}
      <div className="dashboard-grid">
        {/* Status Distribution */}
        <Card>
          <CardHeader>
            <div>
              <div className="card-title">Status Distribution</div>
              <div className="card-subtitle">Documents by current status</div>
            </div>
          </CardHeader>
          <div className="status-distribution">
            {statusDist ? statusDist.map((s) => {
              const total = statusDist.reduce((a, b) => a + b.value, 0);
              const pct = ((s.value / total) * 100).toFixed(1);
              return (
                <div key={s.label} className="status-row">
                  <span className="status-dot" style={{ backgroundColor: s.color }} aria-hidden="true" />
                  <span className="status-label-col">{s.label}</span>
                  <span className="status-value-col">{formatNumber(s.value)}</span>
                  <div className="status-bar-mini">
                    <div
                      className="status-bar-mini-fill"
                      style={{ width: `${pct}%`, backgroundColor: s.color }}
                    />
                  </div>
                </div>
              );
            }) : Array.from({ length: 5 }).map((_, i) => (
              <div key={i} style={{ padding: '4px 0' }}><Skeleton height={20} /></div>
            ))}
          </div>
        </Card>

        {/* Weekly Activity */}
        <Card>
          <CardHeader>
            <div>
              <div className="card-title">Weekly Activity</div>
              <div className="card-subtitle">Documents processed this week</div>
            </div>
          </CardHeader>
          <div className="weekly-chart">
            {weeklyActivity ? weeklyActivity.map((d) => (
              <div key={d.label} className="weekly-bar-col">
                <span className="weekly-bar-value">{d.value}</span>
                <div
                  className="weekly-bar"
                  style={{ height: `${(d.value / maxWeeklyValue) * 80}%` }}
                  aria-label={`${d.label}: ${d.value} documents`}
                />
                <span className="weekly-bar-label">{d.label}</span>
              </div>
            )) : <Skeleton width="100%" height={100} />}
          </div>
        </Card>

        {/* Department Workload */}
        <Card>
          <CardHeader>
            <div>
              <div className="card-title">Department Workload</div>
              <div className="card-subtitle">Documents by department</div>
            </div>
          </CardHeader>
          <div className="chart-container">
            <div className="chart-bar-group">
              {deptWorkload ? deptWorkload.slice(0, 7).map((d) => (
                <div key={d.label} className="chart-bar-row">
                  <span className="chart-bar-label">{d.label}</span>
                  <div className="chart-bar-track">
                    <div
                      className="chart-bar-fill"
                      style={{ width: `${(d.value / maxDeptValue) * 100}%` }}
                    />
                  </div>
                  <span className="chart-bar-value">{d.value}</span>
                </div>
              )) : Array.from({ length: 5 }).map((_, i) => (
                <div key={i} style={{ padding: '4px 0' }}><Skeleton height={24} /></div>
              ))}
            </div>
          </div>
        </Card>

        {/* Needs Attention */}
        <Card>
          <CardHeader>
            <div>
              <div className="card-title">Needs Attention</div>
              <div className="card-subtitle">Critical and high-priority items</div>
            </div>
          </CardHeader>
          <div className="attention-list">
            {recentDocs ? recentDocs
              .filter((d) => d.priority === 'critical' || d.priority === 'high')
              .slice(0, 5)
              .map((doc) => (
                <div key={doc.id} className="attention-item" onClick={() => navigate(`/documents/${doc.id}`)} role="button" tabIndex={0}>
                  <div className="attention-info">
                    <div className="attention-name">{doc.name}</div>
                    <div className="attention-detail">{doc.department || 'Unassigned'} · {doc.type}</div>
                  </div>
                  <PriorityBadge priority={doc.priority} />
                  <StatusBadge status={doc.status} />
                </div>
              )) : Array.from({ length: 3 }).map((_, i) => (
              <div key={i} style={{ padding: 'var(--space-3) var(--space-4)' }}><Skeleton height={36} /></div>
            ))}
          </div>
        </Card>

        {/* Recent Activity */}
        <Card className="dashboard-grid-full">
          <CardHeader>
            <div>
              <div className="card-title">Recent Activity</div>
              <div className="card-subtitle">Latest actions across the platform</div>
            </div>
            <Button variant="ghost" size="sm" onClick={() => navigate('/audit-logs')}>
              View All
            </Button>
          </CardHeader>
          <div className="activity-list">
            {recentActivity ? recentActivity.map((entry) => (
              <div key={entry.id} className="activity-item">
                <span className={`activity-dot ${AUDIT_DOT_MAP[entry.action] || 'activity-dot-info'}`} aria-hidden="true" />
                <div className="activity-content">
                  <div className="activity-text">
                    <strong>{entry.userName}</strong> {entry.action} {entry.resource.toLowerCase()} — {entry.details}
                  </div>
                  <div className="activity-time">{formatRelativeTime(entry.timestamp)}</div>
                </div>
              </div>
            )) : Array.from({ length: 5 }).map((_, i) => (
              <div key={i} style={{ padding: 'var(--space-3) var(--space-4)' }}><Skeleton height={36} /></div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
