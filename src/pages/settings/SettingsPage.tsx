// ===========================
// E2EDocs — Settings Page
// ===========================

import { useState, useEffect } from 'react';
import { Card, CardHeader, CardBody, Input, Button, Tabs, SelectField, Avatar } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import { useAuth } from '../../context/AuthContext';
import { USER_ROLE_LABELS } from '../../constants';
import { CheckCircle2, AlertCircle } from 'lucide-react';

export default function SettingsPage() {
  useDocumentTitle('Settings');
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');

  // Profile Form State
  const [fullName, setFullName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [profileSuccess, setProfileSuccess] = useState(false);

  // Security Form State
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [securitySuccess, setSecuritySuccess] = useState(false);
  const [securityError, setSecurityError] = useState('');

  // Org Form State
  const [orgName, setOrgName] = useState('Enterprise Workspace');
  const [orgSuccess, setOrgSuccess] = useState(false);

  useEffect(() => {
    if (user) {
      setFullName(user.name || '');
      setEmail(user.email || '');
    }
  }, [user]);

  const handleProfileSave = (e: React.FormEvent) => {
    e.preventDefault();
    setProfileSuccess(true);
    setTimeout(() => setProfileSuccess(false), 3000);
  };

  const handleSecuritySave = (e: React.FormEvent) => {
    e.preventDefault();
    setSecurityError('');
    if (!currentPassword) {
      setSecurityError('Current password is required');
      return;
    }
    if (newPassword.length < 8) {
      setSecurityError('New password must be at least 8 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      setSecurityError('New passwords do not match');
      return;
    }
    setSecuritySuccess(true);
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setTimeout(() => setSecuritySuccess(false), 3000);
  };

  const handleOrgSave = (e: React.FormEvent) => {
    e.preventDefault();
    setOrgSuccess(true);
    setTimeout(() => setOrgSuccess(false), 3000);
  };

  const tabs = [
    { id: 'profile', label: 'Profile' },
    { id: 'security', label: 'Security' },
    { id: 'notifications', label: 'Notifications' },
    { id: 'preferences', label: 'Preferences' },
    { id: 'organization', label: 'Organization' },
  ];

  const roleDisplay = user?.role ? (USER_ROLE_LABELS[user.role] || user.role) : 'Administrator';
  const deptDisplay = user?.department || 'Operations';

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Settings</h1>
        <p className="page-description">Manage your account and application preferences.</p>
      </div>

      <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />

      <div className="settings-container">
        {activeTab === 'profile' && (
          <Card>
            <CardHeader><div className="card-title">Profile Information</div></CardHeader>
            <CardBody>
              <div className="profile-avatar-row">
                <Avatar name={user?.name || fullName || 'User'} size="lg" />
                <div style={{ minWidth: 0 }}>
                  <div style={{ fontWeight: 'var(--weight-semibold)', fontSize: 'var(--text-body)', color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {user?.name || fullName || 'User'}
                  </div>
                  <div style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {roleDisplay} · {deptDisplay}
                  </div>
                </div>
              </div>

              {profileSuccess && (
                <div style={{
                  display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                  padding: 'var(--space-3)', backgroundColor: 'var(--color-success-50)',
                  border: '1px solid var(--color-success-200)', borderRadius: 'var(--radius-md)',
                  color: 'var(--color-success-700)', fontSize: 'var(--text-body-sm)', marginBottom: 'var(--space-4)'
                }}>
                  <CheckCircle2 size={16} />
                  <span>Profile changes saved successfully.</span>
                </div>
              )}

              <form onSubmit={handleProfileSave}>
                <div className="form-grid-2">
                  <Input
                    label="Full Name"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                  />
                  <Input
                    label="Email Address"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                  <Input
                    label="Department"
                    value={deptDisplay}
                    disabled
                  />
                  <Input
                    label="Assigned Role"
                    value={roleDisplay}
                    disabled
                  />
                </div>
                <div style={{ marginTop: 'var(--space-6)', display: 'flex', justifyContent: 'flex-end' }}>
                  <Button variant="primary" size="sm" type="submit">Save Changes</Button>
                </div>
              </form>
            </CardBody>
          </Card>
        )}

        {activeTab === 'security' && (
          <Card>
            <CardHeader><div className="card-title">Security &amp; Authentication</div></CardHeader>
            <CardBody>
              {securitySuccess && (
                <div style={{
                  display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                  padding: 'var(--space-3)', backgroundColor: 'var(--color-success-50)',
                  border: '1px solid var(--color-success-200)', borderRadius: 'var(--radius-md)',
                  color: 'var(--color-success-700)', fontSize: 'var(--text-body-sm)', marginBottom: 'var(--space-4)'
                }}>
                  <CheckCircle2 size={16} />
                  <span>Password updated successfully.</span>
                </div>
              )}

              {securityError && (
                <div style={{
                  display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                  padding: 'var(--space-3)', backgroundColor: 'var(--color-error-50)',
                  border: '1px solid var(--color-error-200)', borderRadius: 'var(--radius-md)',
                  color: 'var(--color-error-700)', fontSize: 'var(--text-body-sm)', marginBottom: 'var(--space-4)'
                }}>
                  <AlertCircle size={16} />
                  <span>{securityError}</span>
                </div>
              )}

              <form onSubmit={handleSecuritySave}>
                <div style={{ maxWidth: 540, display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                  <Input
                    label="Current Password"
                    type="password"
                    placeholder="Enter current password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    required
                  />
                  <Input
                    label="New Password"
                    type="password"
                    placeholder="Minimum 8 characters"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />
                  <Input
                    label="Confirm New Password"
                    type="password"
                    placeholder="Confirm new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                  <div style={{ marginTop: 'var(--space-2)', display: 'flex', justifyContent: 'flex-start' }}>
                    <Button variant="primary" size="sm" type="submit">Update Password</Button>
                  </div>
                </div>
              </form>
            </CardBody>
          </Card>
        )}

        {activeTab === 'notifications' && (
          <Card>
            <CardHeader><div className="card-title">Notification Preferences</div></CardHeader>
            <CardBody>
              {[
                { label: 'Email notifications', desc: 'Receive email for important events' },
                { label: 'Document assignments', desc: 'Notify when documents are assigned to you' },
                { label: 'Rule triggers', desc: 'Notify when rules match documents' },
                { label: 'Workflow updates', desc: 'Notify on workflow status changes' },
                { label: 'Weekly digest', desc: 'Receive a weekly summary report' },
              ].map((pref) => (
                <div key={pref.label} style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--space-4)',
                  padding: 'var(--space-3) 0', borderBottom: '1px solid var(--border-secondary)',
                }}>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 'var(--text-body-sm)', fontWeight: 'var(--weight-medium)', color: 'var(--text-primary)' }}>{pref.label}</div>
                    <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>{pref.desc}</div>
                  </div>
                  <label style={{ position: 'relative', display: 'inline-block', width: 40, height: 22, flexShrink: 0 }}>
                    <input type="checkbox" defaultChecked style={{ opacity: 0, width: 0, height: 0 }} />
                    <span style={{
                      position: 'absolute', cursor: 'pointer', inset: 0,
                      backgroundColor: 'var(--color-brand-500)', borderRadius: 'var(--radius-full)',
                      transition: 'var(--transition-fast)',
                    }}>
                      <span style={{
                        position: 'absolute', content: '""', height: 16, width: 16,
                        left: 20, bottom: 3, backgroundColor: 'white',
                        borderRadius: 'var(--radius-full)', transition: 'var(--transition-fast)',
                      }} />
                    </span>
                  </label>
                </div>
              ))}
            </CardBody>
          </Card>
        )}

        {activeTab === 'preferences' && (
          <Card>
            <CardHeader><div className="card-title">Preferences</div></CardHeader>
            <CardBody>
              <div style={{ display: 'grid', gap: 'var(--space-4)', maxWidth: 440 }}>
                <SelectField label="Language" options={[{ value: 'en', label: 'English' }]} value="en" />
                <SelectField label="Date Format" options={[
                  { value: 'mdy', label: 'MM/DD/YYYY' },
                  { value: 'dmy', label: 'DD/MM/YYYY' },
                  { value: 'ymd', label: 'YYYY-MM-DD' },
                ]} value="mdy" />
                <SelectField label="Default Page Size" options={[
                  { value: '10', label: '10 items' },
                  { value: '20', label: '20 items' },
                  { value: '50', label: '50 items' },
                ]} value="20" />
              </div>
            </CardBody>
          </Card>
        )}

        {activeTab === 'organization' && (
          <Card>
            <CardHeader><div className="card-title">Organization Settings</div></CardHeader>
            <CardBody>
              {orgSuccess && (
                <div style={{
                  display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                  padding: 'var(--space-3)', backgroundColor: 'var(--color-success-50)',
                  border: '1px solid var(--color-success-200)', borderRadius: 'var(--radius-md)',
                  color: 'var(--color-success-700)', fontSize: 'var(--text-body-sm)', marginBottom: 'var(--space-4)'
                }}>
                  <CheckCircle2 size={16} />
                  <span>Organization settings saved.</span>
                </div>
              )}
              <form onSubmit={handleOrgSave}>
                <div className="form-grid-2">
                  <Input
                    label="Organization Name"
                    value={orgName}
                    onChange={(e) => setOrgName(e.target.value)}
                    required
                  />
                  <Input
                    label="Assigned Domain"
                    defaultValue="app.e2edocs.com"
                    disabled
                  />
                </div>
                <div style={{ marginTop: 'var(--space-6)', display: 'flex', justifyContent: 'flex-end' }}>
                  <Button variant="primary" size="sm" type="submit">Save Changes</Button>
                </div>
              </form>
            </CardBody>
          </Card>
        )}
      </div>
    </div>
  );
}
