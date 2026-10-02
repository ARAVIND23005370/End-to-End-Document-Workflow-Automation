// ===========================
// E2EDocs — Sidebar Navigation
// ===========================

import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FileText,
  GitBranch,
  Workflow,
  Bell,
  Users,
  ScrollText,
  BarChart3,
  Settings,
  PanelLeftClose,
  PanelLeft,
} from 'lucide-react';
import { cn } from '../../utils';
import { NAVIGATION_ITEMS } from '../../constants';
import brandIcon from '../../assets/brand/e2edocs-icon.png';

const ICON_MAP: Record<string, React.ElementType> = {
  LayoutDashboard,
  FileText,
  GitBranch,
  Workflow,
  Bell,
  Users,
  ScrollText,
  BarChart3,
  Settings,
};

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
  mobileOpen: boolean;
  onMobileClose: () => void;
  notificationCount?: number;
}

export function Sidebar({ collapsed, onToggle, mobileOpen, onMobileClose, notificationCount = 0 }: SidebarProps) {
  return (
    <>
      {mobileOpen && <div className="sidebar-overlay" onClick={onMobileClose} aria-hidden="true" />}
      <aside
        className={cn('sidebar', collapsed && 'sidebar-collapsed', mobileOpen && 'sidebar-mobile-open')}
        aria-label="Main navigation"
      >
        <div className="sidebar-header">
          <NavLink to="/dashboard" className="sidebar-logo" onClick={onMobileClose}>
            <img
              src={brandIcon}
              alt="E2EDocs"
              style={{ width: 28, height: 28, objectFit: 'contain', flexShrink: 0 }}
            />
            {!collapsed && (
              <span className="sidebar-logo-text">
                E2E<span style={{ color: 'var(--color-brand-600)' }}>Docs</span>
              </span>
            )}
          </NavLink>
        </div>

        <nav className="sidebar-nav">
          <div className="sidebar-nav-group">
            {NAVIGATION_ITEMS.map((item) => {
              const Icon = ICON_MAP[item.icon] || FileText;
              const badge = item.label === 'Notifications' ? notificationCount : item.badge;

              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    cn('sidebar-nav-item', isActive && 'sidebar-nav-item-active')
                  }
                  onClick={onMobileClose}
                  title={collapsed ? item.label : undefined}
                >
                  <Icon className="sidebar-nav-icon" aria-hidden="true" />
                  <span className="sidebar-nav-label">{item.label}</span>
                  {badge ? <span className="sidebar-nav-badge" aria-label={`${badge} unread`}>{badge}</span> : null}
                </NavLink>
              );
            })}
          </div>
        </nav>

        <div className="sidebar-footer">
          <button
            className="sidebar-collapse-btn"
            onClick={onToggle}
            aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <PanelLeft size={18} /> : <PanelLeftClose size={18} />}
          </button>
        </div>
      </aside>
    </>
  );
}
