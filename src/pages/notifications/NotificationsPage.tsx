// ===========================
// E2EDocs — Notifications Page
// ===========================

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, CheckCheck, AlertCircle, CheckCircle, AlertTriangle, Info, FileText } from 'lucide-react';
import { Button, Card, Badge, EmptyState, LoadingState, ErrorState, Tabs } from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { notificationService } from '../../services/api';
import { formatRelativeTime, cn } from '../../utils';
import type { NotificationType } from '../../types';

const TYPE_ICONS: Record<NotificationType, React.ElementType> = {
  success: CheckCircle,
  warning: AlertTriangle,
  error: AlertCircle,
  info: Info,
};

const TYPE_COLORS: Record<NotificationType, string> = {
  success: 'var(--color-success-500)',
  warning: 'var(--color-warning-500)',
  error: 'var(--color-error-500)',
  info: 'var(--color-info-500)',
};

export default function NotificationsPage() {
  useDocumentTitle('Notifications');
  const navigate = useNavigate();
  const [filter, setFilter] = useState('all');

  const { data: notifications, status, error, refetch } = useAsync(() => notificationService.getAll(), []);

  const filtered = (notifications || []).filter((n) => {
    if (filter === 'unread') return !n.read;
    if (filter === 'read') return n.read;
    return true;
  });

  const unreadCount = (notifications || []).filter((n) => !n.read).length;

  const tabs = [
    { id: 'all', label: `All (${notifications?.length || 0})` },
    { id: 'unread', label: `Unread (${unreadCount})` },
    { id: 'read', label: 'Read' },
  ];

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Notifications</h1>
            <p className="page-description">{unreadCount > 0 ? `You have ${unreadCount} unread notifications.` : 'You\'re all caught up.'}</p>
          </div>
          <div className="page-actions">
            <Button variant="secondary" size="sm"><CheckCheck size={14} /> Mark All Read</Button>
          </div>
        </div>
      </div>

      <Card>
        <div style={{ borderBottom: '1px solid var(--border-secondary)' }}>
          <Tabs tabs={tabs} activeTab={filter} onChange={setFilter} />
        </div>

        {status === 'loading' ? <LoadingState message="Loading notifications…" /> :
         status === 'error' ? <ErrorState message={error || 'Failed to load notifications'} onRetry={refetch} /> :
         filtered.length === 0 ? (
          <EmptyState icon={<Bell size={40} />} title="No notifications" description={filter === 'unread' ? 'No unread notifications.' : 'No notifications to show.'} />
         ) : (
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {filtered.map((notif) => {
              const Icon = TYPE_ICONS[notif.type];
              const iconColor = TYPE_COLORS[notif.type];
              return (
                <div
                  key={notif.id}
                  style={{
                    display: 'flex', alignItems: 'flex-start', gap: 'var(--space-3)',
                    padding: 'var(--space-4) var(--space-5)',
                    borderBottom: '1px solid var(--border-secondary)',
                    backgroundColor: notif.read ? 'transparent' : 'var(--color-brand-50)',
                    cursor: notif.documentId ? 'pointer' : 'default',
                    transition: 'background-color var(--transition-fast)',
                  }}
                  onClick={() => notif.documentId && navigate(`/documents/${notif.documentId}`)}
                  role={notif.documentId ? 'button' : undefined}
                  tabIndex={notif.documentId ? 0 : undefined}
                >
                  <div style={{
                    width: 32, height: 32, borderRadius: 'var(--radius-full)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    backgroundColor: `color-mix(in srgb, ${iconColor} 10%, transparent)`,
                    flexShrink: 0,
                  }}>
                    <Icon size={16} style={{ color: iconColor }} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 2 }}>
                      <span style={{ fontSize: 'var(--text-body-sm)', fontWeight: notif.read ? 'var(--weight-normal)' : 'var(--weight-medium)' } as React.CSSProperties}>
                        {notif.title}
                      </span>
                      {!notif.read && <span style={{ width: 6, height: 6, borderRadius: 'var(--radius-full)', backgroundColor: 'var(--color-brand-500)', flexShrink: 0 }} />}
                    </div>
                    <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-1)' }}>{notif.message}</p>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>
                      <span>{formatRelativeTime(notif.timestamp)}</span>
                      {notif.documentName && (
                        <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-1)' }}>
                          <FileText size={10} /> {notif.documentName}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </Card>
    </div>
  );
}
