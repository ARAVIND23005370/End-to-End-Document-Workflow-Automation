// ===========================
// E2EDocs — Forgot Password Page
// ===========================

import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { Button, Input } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';

import brandIcon from '../../assets/brand/e2edocs-icon.png';

export default function ForgotPasswordPage() {
  useDocumentTitle('Forgot Password');
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSent(true);
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      backgroundColor: 'var(--bg-secondary)', padding: 'var(--space-4)',
    }}>
      <div style={{ width: '100%', maxWidth: 380 }}>
        <div style={{ textAlign: 'center', marginBottom: 'var(--space-8)' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-3)' }}>
            <img
              src={brandIcon}
              alt="E2EDocs"
              style={{ width: 36, height: 36, objectFit: 'contain' }}
            />
            <span style={{ fontSize: 'var(--text-h3)', fontWeight: 'var(--weight-bold)', letterSpacing: 'var(--tracking-tight)' }}>
              E2E<span style={{ color: 'var(--color-brand-600)' }}>Docs</span>
            </span>
          </div>
          <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
            {sent ? 'Check your email' : 'Reset your password'}
          </p>
        </div>

        <div style={{
          backgroundColor: 'var(--bg-elevated)', border: '1px solid var(--border-secondary)',
          borderRadius: 'var(--radius-lg)', padding: 'var(--space-6)',
        }}>
          {sent ? (
            <div style={{ textAlign: 'center' }}>
              <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-4)' }}>
                If an account with <strong>{email}</strong> exists, we've sent password reset instructions.
              </p>
              <Link to="/login">
                <Button variant="secondary" style={{ width: '100%' }}>
                  <ArrowLeft size={14} /> Back to Sign In
                </Button>
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
                  Enter your email address and we'll send you a link to reset your password.
                </p>
                <Input
                  label="Email"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
                <Button variant="primary" size="lg" type="submit" style={{ width: '100%' }}>
                  Send Reset Link
                </Button>
                <Link to="/login" style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 'var(--space-1)',
                  fontSize: 'var(--text-body-sm)', color: 'var(--text-link)',
                }}>
                  <ArrowLeft size={14} /> Back to Sign In
                </Link>
              </div>
            </form>
          )}
        </div>

        <div style={{ marginTop: 'var(--space-6)', textAlign: 'center', fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>
          Developed by AravindRamesh
        </div>
      </div>
    </div>
  );
}
