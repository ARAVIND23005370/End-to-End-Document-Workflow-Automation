// ===========================
// E2EDocs — Top Bar
// ===========================

import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Bell, Menu, LogOut, User, Settings } from 'lucide-react';
import { cn } from '../../utils';
import { useAuth } from '../../context/AuthContext';
import { Avatar, Dropdown, DropdownItem, SearchInput } from '../ui';
import { useClickOutside, useKeyboardShortcut } from '../../hooks';
import { USER_ROLE_LABELS } from '../../constants';

interface TopBarProps {
  title: string;
  sidebarCollapsed: boolean;
  onMenuClick: () => void;
  notificationCount?: number;
}

export function TopBar({ title, onMenuClick, notificationCount = 0 }: TopBarProps) {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const navigate = useNavigate();
  const [searchOpen, setSearchOpen] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  useClickOutside(searchRef, () => setSearchOpen(false));
  useKeyboardShortcut('k', () => setSearchOpen(true), { ctrl: true });

  const handleSignOut = async () => {
    try {
      await logout();
    } catch {}
    navigate('/login');
  };

  return (
    <header className="topbar">
      <div className="topbar-left">
        <button className="topbar-btn mobile-menu-btn" onClick={onMenuClick} aria-label="Open menu">
          <Menu size={20} />
        </button>
        <h1 className="topbar-title">{title}</h1>
      </div>

      <div className="topbar-right">
        {/* Search */}
        <div className="topbar-search" ref={searchRef}>
          {searchOpen ? (
            <SearchInput
              placeholder="Search documents, rules…"
              autoFocus
              onBlur={() => setSearchOpen(false)}
            />
          ) : (
            <button
              className="topbar-btn"
              onClick={() => setSearchOpen(true)}
              aria-label="Search (Ctrl+K)"
              title="Search (Ctrl+K)"
            >
              <Search size={18} />
            </button>
          )}
        </div>

        {/* Notifications */}
        <button
          className="topbar-btn"
          onClick={() => navigate('/notifications')}
          aria-label={`Notifications${notificationCount > 0 ? `, ${notificationCount} unread` : ''}`}
        >
          <Bell size={18} />
          {notificationCount > 0 && <span className="topbar-notification-dot" aria-hidden="true" />}
        </button>

        {/* User Profile */}
        {isLoading ? (
          <div className="topbar-user" style={{ opacity: 0.6, cursor: 'default' }}>
            <div style={{ width: 28, height: 28, borderRadius: '50%', backgroundColor: 'var(--color-gray-200)' }} />
          </div>
        ) : (user || isAuthenticated) ? (
          <Dropdown
            trigger={
              <div
                className="topbar-user"
                title={`${user?.name || 'User'} (${(user?.role && USER_ROLE_LABELS[user.role]) || user?.role || 'Member'})`}
              >
                <Avatar name={user?.name || 'User'} size="sm" />
                <div className="topbar-user-info">
                  <span className="topbar-user-name">{user?.name || 'User'}</span>
                  <span className="topbar-user-role">{(user?.role && USER_ROLE_LABELS[user.role]) || user?.role || 'Member'}</span>
                </div>
              </div>
            }
          >
            <DropdownItem onClick={() => navigate('/settings')}>
              <User size={14} /> Profile
            </DropdownItem>
            <DropdownItem onClick={() => navigate('/settings')}>
              <Settings size={14} /> Settings
            </DropdownItem>
            <div className="dropdown-separator" />
            <DropdownItem onClick={handleSignOut}>
              <LogOut size={14} /> Sign Out
            </DropdownItem>
          </Dropdown>
        ) : (
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => navigate('/login')}
          >
            Sign In
          </button>
        )}
      </div>
    </header>
  );
}
