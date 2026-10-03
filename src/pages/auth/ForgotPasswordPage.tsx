import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Button, Input } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import { authService } from '../../services/api';

import brandIcon from '../../assets/brand/e2edocs-icon.png';

export default function ForgotPasswordPage() {
  useDocumentTitle('Forgot Password');
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;

    setError('');
    setLoading(true);
    try {
      await authService.forgotPassword(email.trim().toLowerCase());
      setSent(true);
    } catch (err: any) {
      setError(err?.message || 'Failed to send reset link. Please try again.');
    } finally {
      setLoading(false);
    }
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
              <div style={{
                display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                width: 44, height: 44, borderRadius: '50%',
                backgroundColor: 'var(--color-success-50, #f0fdf4)', color: 'var(--color-success-600, #16a34a)',
                marginBottom: 'var(--space-3)'
              }}>
                <CheckCircle2 size={24} />
              </div>
              <h3 style={{ fontSize: 'var(--text-body-lg)', fontWeight: 'var(--weight-semibold)', marginBottom: 'var(--space-2)' }}>
                Instructions Sent
              </h3>
              <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-5)', lineHeight: 1.5 }}>
                If an account with <strong>{email}</strong> exists, password reset instructions and link have been dispatched.
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

                {error && (
                  <div style={{
                    display: 'flex', alignItems: 'center', gap: 'var(--space-2)',
                    padding: 'var(--space-2) var(--space-3)',
                    backgroundColor: 'var(--color-error-50, #fef2f2)',
                    border: '1px solid var(--color-error-200, #fecaca)',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: 'var(--text-body-sm)',
                    color: 'var(--color-error-700, #b91c1c)',
                  }}>
                    <AlertCircle size={16} />
                    <span>{error}</span>
                  </div>
                )}

                <Input
                  label="Email Address"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  disabled={loading}
                  autoComplete="email"
                />
                <Button variant="primary" size="lg" type="submit" disabled={loading} style={{ width: '100%' }}>
                  {loading ? 'Sending link…' : 'Send Reset Link'}
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
