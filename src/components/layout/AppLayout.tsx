// ===========================
// E2EDocs — Application Layout
// ===========================

import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from '../navigation/Sidebar';
import { TopBar } from '../navigation/TopBar';
import { cn } from '../../utils';
import { useLocalStorage, useMediaQuery } from '../../hooks';

const PAGE_TITLES: Record<string, string> = {
  '/dashboard': 'Overview',
  '/documents': 'Documents',
  '/rules': 'Rules',
  '/rules/new': 'Create Rule',
  '/workflows': 'Workflows',
  '/notifications': 'Notifications',
  '/users': 'Users',
  '/audit-logs': 'Audit Logs',
  '/reports': 'Reports',
  '/settings': 'Settings',
};

export function AppLayout() {
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 1024px)');
  const [collapsed, setCollapsed] = useLocalStorage('sidebar-collapsed', false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const pageTitle = PAGE_TITLES[location.pathname] || 'E2EDocs';

  // Mock notification count
  const notificationCount = 4;

  return (
    <div className="app-shell">
      <Sidebar
        collapsed={!isMobile && collapsed}
        onToggle={() => setCollapsed(!collapsed)}
        mobileOpen={mobileOpen}
        onMobileClose={() => setMobileOpen(false)}
        notificationCount={notificationCount}
      />

      <main className={cn('app-main', !isMobile && collapsed && 'sidebar-collapsed')}>
        <TopBar
          title={pageTitle}
          sidebarCollapsed={!isMobile && collapsed}
          onMenuClick={() => setMobileOpen(true)}
          notificationCount={notificationCount}
        />

        <div className="app-content">
          <Outlet />
        </div>

        <footer className="app-footer">
          <span>&copy; 2026 E2EDocs. Document workflow automation platform.</span>
          <span>Developed by AravindRamesh</span>
        </footer>
      </main>
    </div>
  );
}
