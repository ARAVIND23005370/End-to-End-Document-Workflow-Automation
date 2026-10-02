// ===========================
// E2EDocs — Route Configuration
// ===========================

import { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from '../components/layout/AppLayout';
import { LoadingState } from '../components/ui';

// Lazy-loaded pages
const LandingPage = lazy(() => import('../pages/public/LandingPage'));
const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const SignupPage = lazy(() => import('../pages/auth/SignupPage'));
const ForgotPasswordPage = lazy(() => import('../pages/auth/ForgotPasswordPage'));
const DashboardPage = lazy(() => import('../pages/dashboard/DashboardPage'));
const DocumentsPage = lazy(() => import('../pages/documents/DocumentsPage'));
const DocumentDetailPage = lazy(() => import('../pages/documents/DocumentDetailPage'));
const RulesPage = lazy(() => import('../pages/rules/RulesPage'));
const RuleBuilderPage = lazy(() => import('../pages/rules/RuleBuilderPage'));
const WorkflowsPage = lazy(() => import('../pages/workflows/WorkflowsPage'));
const NotificationsPage = lazy(() => import('../pages/notifications/NotificationsPage'));
const UsersPage = lazy(() => import('../pages/users/UsersPage'));
const AuditLogsPage = lazy(() => import('../pages/audit/AuditLogsPage'));
const ReportsPage = lazy(() => import('../pages/reports/ReportsPage'));
const SettingsPage = lazy(() => import('../pages/settings/SettingsPage'));
const NotFoundPage = lazy(() => import('../pages/public/NotFoundPage'));

function SuspenseWrapper({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<LoadingState />}>{children}</Suspense>;
}

export const router = createBrowserRouter([
  // Public
  {
    path: '/',
    element: <SuspenseWrapper><LandingPage /></SuspenseWrapper>,
  },

  // Auth
  {
    path: '/login',
    element: <SuspenseWrapper><LoginPage /></SuspenseWrapper>,
  },
  {
    path: '/signup',
    element: <SuspenseWrapper><SignupPage /></SuspenseWrapper>,
  },
  {
    path: '/forgot-password',
    element: <SuspenseWrapper><ForgotPasswordPage /></SuspenseWrapper>,
  },

  // Application (requires auth — enforcement deferred to backend integration)
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { path: 'dashboard', element: <SuspenseWrapper><DashboardPage /></SuspenseWrapper> },
      { path: 'documents', element: <SuspenseWrapper><DocumentsPage /></SuspenseWrapper> },
      { path: 'documents/:id', element: <SuspenseWrapper><DocumentDetailPage /></SuspenseWrapper> },
      { path: 'rules', element: <SuspenseWrapper><RulesPage /></SuspenseWrapper> },
      { path: 'rules/new', element: <SuspenseWrapper><RuleBuilderPage /></SuspenseWrapper> },
      { path: 'rules/:id', element: <SuspenseWrapper><RuleBuilderPage /></SuspenseWrapper> },
      { path: 'workflows', element: <SuspenseWrapper><WorkflowsPage /></SuspenseWrapper> },
      { path: 'notifications', element: <SuspenseWrapper><NotificationsPage /></SuspenseWrapper> },
      { path: 'users', element: <SuspenseWrapper><UsersPage /></SuspenseWrapper> },
      { path: 'audit-logs', element: <SuspenseWrapper><AuditLogsPage /></SuspenseWrapper> },
      { path: 'reports', element: <SuspenseWrapper><ReportsPage /></SuspenseWrapper> },
      { path: 'settings', element: <SuspenseWrapper><SettingsPage /></SuspenseWrapper> },
    ],
  },

  // Catch-all
  { path: '*', element: <SuspenseWrapper><NotFoundPage /></SuspenseWrapper> },
]);
