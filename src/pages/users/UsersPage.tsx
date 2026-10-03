// ===========================
// E2EDocs — Users Page (Real Backend API Integration)
// ===========================

import { useState } from 'react';
import {
  Plus, MoreHorizontal, Eye, Shield, Trash2, CheckCircle2,
  Users as UsersIcon, X, KeyRound, AlertTriangle, Mail
} from 'lucide-react';
import {
  Button, Card, SearchInput, SelectField,
  StatusBadge, Badge, Avatar, EmptyState, LoadingState, ErrorState,
  Dropdown, DropdownItem, Input
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { useAuth } from '../../context/AuthContext';
import { userService, authService } from '../../services/api';
import { formatRelativeTime } from '../../utils';
import { USER_ROLE_LABELS, USER_STATUS_LABELS } from '../../constants';
import type { User, UserRole } from '../../types';

export default function UsersPage() {
  useDocumentTitle('Users');
  const { user: currentUser } = useAuth();
  const isSuperAdmin = currentUser?.role === 'super_admin';

  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Modals state
  const [isInviteOpen, setIsInviteOpen] = useState(false);
  const [inviteName, setInviteName] = useState('');
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<UserRole>('user');
  const [inviteDept, setInviteDept] = useState('');
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState('');

  const [viewUser, setViewUser] = useState<User | null>(null);
  const [roleChangeUser, setRoleChangeUser] = useState<User | null>(null);
  const [selectedRole, setSelectedRole] = useState<UserRole>('user');
  const [roleChangeLoading, setRoleChangeLoading] = useState(false);
  const [roleChangeError, setRoleChangeError] = useState('');

  const [deleteConfirmUser, setDeleteConfirmUser] = useState<User | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Fetch real users from backend API
  const { data: users, status, error, refetch } = useAsync(() => userService.getAll(), []);

  const showNotification = (message: string, type: 'success' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 4000);
  };

  const filtered = (users || []).filter((u) => {
    if (search && !u.name.toLowerCase().includes(search.toLowerCase()) && !u.email.toLowerCase().includes(search.toLowerCase())) return false;
    if (roleFilter && u.role !== roleFilter) return false;
    if (statusFilter && u.status !== statusFilter) return false;
    return true;
  });

  // Handle Invite User submission
  const handleInviteSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setInviteError('');
    setInviteLoading(true);

    try {
      await userService.invite({
        name: inviteName.trim(),
        email: inviteEmail.trim().toLowerCase(),
        role: inviteRole,
        department: inviteDept.trim() || undefined,
      });
      setIsInviteOpen(false);
      setInviteName('');
      setInviteEmail('');
      setInviteRole('user');
      setInviteDept('');
      showNotification('User invited successfully! Invitation email dispatched.');
      refetch();
    } catch (err: any) {
      setInviteError(err?.message || 'Failed to invite user');
    } finally {
      setInviteLoading(false);
    }
  };

  // Handle Resend Invite
  const handleResendInvite = async (user: User) => {
    try {
      await userService.resendInvite(user.id);
      showNotification(`Invitation email resent to ${user.email}!`);
    } catch (err: any) {
      showNotification(err?.message || 'Failed to resend invitation email', 'error');
    }
  };

  // Handle Remove / Delete User submission
  const handleDeleteUserSubmit = async () => {
    if (!deleteConfirmUser) return;
    setDeleteLoading(true);

    try {
      await userService.delete(deleteConfirmUser.id);
      showNotification(`User ${deleteConfirmUser.name} removed successfully.`);
      setDeleteConfirmUser(null);
      refetch();
    } catch (err: any) {
      showNotification(err?.message || 'Failed to remove user', 'error');
    } finally {
      setDeleteLoading(false);
    }
  };

  // Handle Role Change submission
  const handleRoleChangeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roleChangeUser) return;
    setRoleChangeError('');
    setRoleChangeLoading(true);

    try {
      await userService.update(roleChangeUser.id, { role: selectedRole });
      setRoleChangeUser(null);
      showNotification(`Role updated to ${USER_ROLE_LABELS[selectedRole]} successfully!`);
      refetch();
    } catch (err: any) {
      setRoleChangeError(err?.message || 'Failed to change user role');
    } finally {
      setRoleChangeLoading(false);
    }
  };

  // Handle Activate / Deactivate Toggle
  const handleToggleStatus = async (user: User) => {
    if (user.id === currentUser?.id) {
      showNotification('You cannot deactivate your own active user account.', 'error');
      return;
    }

    const newStatus = user.status === 'active' ? 'inactive' : 'active';
    try {
      await userService.update(user.id, { status: newStatus });
      showNotification(`User ${user.name} is now ${newStatus}.`);
      refetch();
    } catch (err: any) {
      showNotification(err?.message || 'Failed to update user status', 'error');
    }
  };

  // Handle Password Reset Request
  const handleResetPassword = async (user: User) => {
    try {
      await authService.forgotPassword(user.email);
      showNotification(`Password reset instructions sent for ${user.email}.`);
    } catch (err: any) {
      showNotification(err?.message || 'Failed to trigger password reset', 'error');
    }
  };

  return (
    <div>
      {/* Toast Notification */}
      {notification && (
        <div style={{
          position: 'fixed', bottom: 24, right: 24, zIndex: 1000,
          padding: '12px 18px', borderRadius: 'var(--radius-md)',
          backgroundColor: notification.type === 'success' ? 'var(--color-success-600, #16a34a)' : 'var(--color-error-600, #dc2626)',
          color: '#ffffff', boxShadow: 'var(--shadow-lg)',
          display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
          fontSize: 'var(--text-body-sm)', fontWeight: 500, animation: 'fadeIn 0.2s ease-out'
        }}>
          {notification.type === 'success' ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
          <span>{notification.message}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1 className="page-title">Users</h1>
            <p className="page-description">Manage real organization user accounts, roles, and access control.</p>
          </div>
          <div className="page-actions">
            <Button variant="primary" size="sm" onClick={() => setIsInviteOpen(true)}>
              <Plus size={14} /> Invite User
            </Button>
          </div>
        </div>
      </div>

      <Card>
        {/* Filter Bar */}
        <div className="filter-bar" style={{ borderBottom: '1px solid var(--border-secondary)' }}>
          <div style={{ flex: 1, maxWidth: 300 }}>
            <SearchInput
              placeholder="Search users…"
              value={search}
              onChange={(e) => setSearch((e.target as HTMLInputElement).value)}
              onSearch={setSearch}
            />
          </div>
          <SelectField
            options={Object.entries(USER_ROLE_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            placeholder="All roles"
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
          />
          <SelectField
            options={Object.entries(USER_STATUS_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            placeholder="All statuses"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          />
        </div>

        {/* Dynamic States */}
        {status === 'loading' ? (
          <LoadingState message="Loading users from database…" />
        ) : status === 'error' ? (
          <ErrorState message={error || 'Failed to load organization users'} onRetry={refetch} />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<UsersIcon size={40} />}
            title="No users found"
            description={search || roleFilter || statusFilter ? "Adjust your filters to see results." : "Invite your team members to collaborate."}
            action={!search && !roleFilter && !statusFilter ? (
              <Button variant="primary" size="sm" onClick={() => setIsInviteOpen(true)}>
                <Plus size={14} /> Invite User
              </Button>
            ) : undefined}
          />
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Department</th>
                  <th>Status</th>
                  <th>Last Active</th>
                  <th className="cell-actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((user) => {
                  const isSelf = user.id === currentUser?.id;
                  return (
                    <tr key={user.id}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                          <Avatar name={user.name} size="sm" />
                          <span className="cell-primary" style={{ fontWeight: 500 }}>{user.name}</span>
                          {isSelf && (
                            <Badge variant="neutral" className="text-xs">You</Badge>
                          )}
                        </div>
                      </td>
                      <td className="cell-secondary">{user.email}</td>
                      <td>
                        <Badge variant={user.role === 'super_admin' || user.role === 'admin' ? 'brand' : 'neutral'}>
                          {USER_ROLE_LABELS[user.role] || user.role}
                        </Badge>
                      </td>
                      <td className="cell-secondary">{user.department || '—'}</td>
                      <td><StatusBadge status={user.status} /></td>
                      <td className="cell-secondary">{user.lastActive ? formatRelativeTime(user.lastActive) : '—'}</td>
                      <td className="cell-actions">
                        <Dropdown trigger={<Button variant="ghost" icon size="sm" aria-label="Actions"><MoreHorizontal size={14} /></Button>}>
                          <DropdownItem onClick={() => setViewUser(user)}>
                            <Eye size={14} /> View Details
                          </DropdownItem>

                          {isSuperAdmin ? (
                            <DropdownItem onClick={() => { setRoleChangeUser(user); setSelectedRole(user.role); }}>
                              <Shield size={14} /> Change Role
                            </DropdownItem>
                          ) : (
                            <DropdownItem className="opacity-50 cursor-not-allowed">
                              <Shield size={14} /> Change Role (Super Admin Only)
                            </DropdownItem>
                          )}

                          <DropdownItem onClick={() => handleResendInvite(user)}>
                            <Mail size={14} /> Resend Invitation
                          </DropdownItem>

                          <DropdownItem onClick={() => handleResetPassword(user)}>
                            <KeyRound size={14} /> Reset Password
                          </DropdownItem>

                          <div className="dropdown-separator" />

                          {user.status === 'active' ? (
                            <DropdownItem
                              onClick={() => !isSelf && handleToggleStatus(user)}
                              className={isSelf ? 'opacity-50 cursor-not-allowed' : 'text-error'}
                            >
                              <Shield size={14} /> Deactivate
                            </DropdownItem>
                          ) : (
                            <DropdownItem onClick={() => handleToggleStatus(user)} className="text-success">
                              <CheckCircle2 size={14} /> Activate User
                            </DropdownItem>
                          )}

                          {!isSelf && (
                            <DropdownItem
                              onClick={() => setDeleteConfirmUser(user)}
                              className="text-error"
                            >
                              <Trash2 size={14} /> Remove User
                            </DropdownItem>
                          )}
                        </Dropdown>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* ====================================================== */}
      {/* 1. INVITE USER MODAL */}
      {/* ====================================================== */}
      {isInviteOpen && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 460, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>Invite New User</h3>
              <button
                type="button"
                onClick={() => setIsInviteOpen(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleInviteSubmit} style={{ padding: 'var(--space-5)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {inviteError && (
                  <div style={{
                    padding: '8px 12px', backgroundColor: 'var(--color-error-50, #fef2f2)',
                    border: '1px solid var(--color-error-200, #fecaca)', borderRadius: 'var(--radius-sm, 4px)',
                    color: 'var(--color-error-700, #b91c1c)', fontSize: 'var(--text-body-sm)'
                  }}>
                    {inviteError}
                  </div>
                )}

                <Input
                  label="Full Name"
                  placeholder="e.g. Jane Doe"
                  value={inviteName}
                  onChange={(e) => setInviteName(e.target.value)}
                  required
                />

                <Input
                  label="Email Address"
                  type="email"
                  placeholder="jane.doe@example.com"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  required
                />

                <SelectField
                  label="Assigned Role"
                  value={inviteRole}
                  onChange={(e) => setInviteRole(e.target.value as UserRole)}
                  options={[
                    { value: 'user', label: 'User — Standard access' },
                    { value: 'reviewer', label: 'Reviewer — Document review' },
                    { value: 'manager', label: 'Manager — Department rules & workflows' },
                    { value: 'admin', label: 'Admin — Full user & org administration' },
                    ...(isSuperAdmin ? [{ value: 'super_admin', label: 'Super Admin — Global platform authority' }] : [])
                  ]}
                />

                <Input
                  label="Department (Optional)"
                  placeholder="e.g. Operations, Legal, Finance"
                  value={inviteDept}
                  onChange={(e) => setInviteDept(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-6)' }}>
                <Button variant="secondary" type="button" onClick={() => setIsInviteOpen(false)} disabled={inviteLoading}>
                  Cancel
                </Button>
                <Button variant="primary" type="submit" disabled={inviteLoading}>
                  {inviteLoading ? 'Inviting…' : 'Send Invite'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ====================================================== */}
      {/* 2. VIEW USER MODAL */}
      {/* ====================================================== */}
      {viewUser && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 440, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>User Details</h3>
              <button
                type="button"
                onClick={() => setViewUser(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <div style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-2)' }}>
                <Avatar name={viewUser.name} size="lg" />
                <div>
                  <h4 style={{ margin: 0, fontSize: 'var(--text-h4)', color: 'var(--text-primary)' }}>{viewUser.name}</h4>
                  <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: 'var(--text-body-sm)' }}>{viewUser.email}</p>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)', fontSize: 'var(--text-body-sm)' }}>
                <div>
                  <span style={{ color: 'var(--text-tertiary)', display: 'block', marginBottom: 2 }}>Role</span>
                  <Badge variant={viewUser.role === 'super_admin' || viewUser.role === 'admin' ? 'brand' : 'neutral'}>
                    {USER_ROLE_LABELS[viewUser.role]}
                  </Badge>
                </div>
                <div>
                  <span style={{ color: 'var(--text-tertiary)', display: 'block', marginBottom: 2 }}>Status</span>
                  <StatusBadge status={viewUser.status} />
                </div>
                <div>
                  <span style={{ color: 'var(--text-tertiary)', display: 'block', marginBottom: 2 }}>Department</span>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{viewUser.department || '—'}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-tertiary)', display: 'block', marginBottom: 2 }}>User ID</span>
                  <code style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{viewUser.id}</code>
                </div>
              </div>

              <div style={{ borderTop: '1px solid var(--border-secondary)', paddingTop: 'var(--space-3)', fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>
                <div>Created: {new Date(viewUser.createdAt).toLocaleDateString()}</div>
                <div>Last Active: {viewUser.lastActive ? formatRelativeTime(viewUser.lastActive) : 'Never'}</div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 'var(--space-2)' }}>
                <Button variant="secondary" onClick={() => setViewUser(null)}>Close</Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ====================================================== */}
      {/* 3. CHANGE ROLE MODAL (SUPER_ADMIN Only) */}
      {/* ====================================================== */}
      {roleChangeUser && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 420, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>Change User Role</h3>
              <button
                type="button"
                onClick={() => setRoleChangeUser(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleRoleChangeSubmit} style={{ padding: 'var(--space-5)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {roleChangeError && (
                  <div style={{
                    padding: '8px 12px', backgroundColor: 'var(--color-error-50)',
                    border: '1px solid var(--color-error-200)', borderRadius: 'var(--radius-sm)',
                    color: 'var(--color-error-700)', fontSize: 'var(--text-body-sm)'
                  }}>
                    {roleChangeError}
                  </div>
                )}

                <div>
                  <span style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>Target User:</span>
                  <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>{roleChangeUser.name} ({roleChangeUser.email})</div>
                </div>

                <SelectField
                  label="Select New Role"
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value as UserRole)}
                  options={[
                    { value: 'user', label: 'User — Standard access' },
                    { value: 'reviewer', label: 'Reviewer — Review documents' },
                    { value: 'manager', label: 'Manager — Rules & workflows' },
                    { value: 'admin', label: 'Admin — User & Org Administration' },
                    { value: 'super_admin', label: 'Super Admin — Global platform authority' }
                  ]}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-6)' }}>
                <Button variant="secondary" type="button" onClick={() => setRoleChangeUser(null)} disabled={roleChangeLoading}>
                  Cancel
                </Button>
                <Button variant="primary" type="submit" disabled={roleChangeLoading}>
                  {roleChangeLoading ? 'Saving…' : 'Update Role'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ====================================================== */}
      {/* 4. DELETE USER CONFIRMATION MODAL */}
      {/* ====================================================== */}
      {deleteConfirmUser && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 999, padding: 'var(--space-4)'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-elevated, #ffffff)', borderRadius: 'var(--radius-lg, 8px)',
            width: '100%', maxWidth: 440, border: '1px solid var(--border-primary, #e2e8f0)',
            boxShadow: 'var(--shadow-xl)', overflow: 'hidden'
          }}>
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--border-secondary, #e2e8f0)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <AlertTriangle size={20} style={{ color: 'var(--color-error-600, #dc2626)' }} />
                <h3 style={{ fontSize: 'var(--text-h4, 18px)', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
                  Remove User
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setDeleteConfirmUser(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)' }}
              >
                <X size={18} />
              </button>
            </div>

            <div style={{ padding: 'var(--space-5)' }}>
              <p style={{ margin: '0 0 var(--space-3) 0', color: 'var(--text-secondary)', fontSize: 'var(--text-body-sm)', lineHeight: 1.5 }}>
                Are you sure you want to permanently remove <strong style={{ color: 'var(--text-primary)' }}>{deleteConfirmUser.name}</strong> (<code>{deleteConfirmUser.email}</code>)?
              </p>
              <p style={{ margin: 0, color: 'var(--color-error-600, #dc2626)', fontSize: 'var(--text-caption)', lineHeight: 1.4 }}>
                This user account will be permanently deleted and all pending invitation/reset tokens will be revoked.
              </p>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-6)' }}>
                <Button
                  variant="secondary"
                  type="button"
                  onClick={() => setDeleteConfirmUser(null)}
                  disabled={deleteLoading}
                >
                  Cancel
                </Button>
                <Button
                  variant="danger"
                  type="button"
                  onClick={handleDeleteUserSubmit}
                  disabled={deleteLoading}
                >
                  {deleteLoading ? 'Removing…' : 'Yes, Remove User'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
