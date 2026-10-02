import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button, Input } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import { useAuth } from '../../context/AuthContext';
import brandLogo from '../../assets/brand/e2edocs-logo.png';

export default function LoginPage() {
  useDocumentTitle('Sign In');
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email.trim(), password);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err?.message || 'Invalid email or password');
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
        {/* Brand Logo & Header */}
        <div style={{ textAlign: 'center', marginBottom: 'var(--space-6)' }}>
          <Link to="/" style={{ display: 'inline-block', marginBottom: 'var(--space-3)' }}>
            <img
              src={brandLogo}
              alt="E2EDocs"
              style={{ width: 130, height: 'auto', objectFit: 'contain' }}
            />
          </Link>
          <h2 style={{ fontSize: 'var(--text-h3)', fontWeight: 'var(--weight-bold)', color: 'var(--text-primary)', marginBottom: 'var(--space-1)' }}>
            Welcome back
          </h2>
          <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
            Sign in to continue to your workspace
          </p>
        </div>

        {/* Form */}
        <div style={{
          backgroundColor: 'var(--bg-elevated)', border: '1px solid var(--border-secondary)',
          borderRadius: 'var(--radius-lg)', padding: 'var(--space-6)',
        }}>
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <Input
                label="Email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
              <Input
                label="Password"
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />

              {error && (
                <div style={{
                  padding: 'var(--space-2) var(--space-3)',
                  backgroundColor: 'var(--color-error-50)',
                  border: '1px solid var(--color-error-200)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: 'var(--text-body-sm)',
                  color: 'var(--color-error-700)',
                }} role="alert">
                  {error}
                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Link to="/forgot-password" style={{
                  fontSize: 'var(--text-body-sm)', color: 'var(--text-link)',
                }}>
                  Forgot password?
                </Link>
              </div>

              <Button variant="primary" size="lg" type="submit" disabled={loading} style={{ width: '100%' }}>
                {loading ? 'Signing in…' : 'Sign In'}
              </Button>
            </div>
          </form>
        </div>

        {/* Sign up link */}
        <div style={{
          marginTop: 'var(--space-5)',
          textAlign: 'center',
          fontSize: 'var(--text-body-sm)',
          color: 'var(--text-secondary)',
        }}>
          Don't have an account?{' '}
          <Link to="/signup" style={{ color: 'var(--text-link)', fontWeight: 'var(--weight-semibold)' }}>
            Create one
          </Link>
        </div>

        <div style={{
          marginTop: 'var(--space-6)',
          textAlign: 'center',
          fontSize: 'var(--text-caption)',
          color: 'var(--text-tertiary)',
        }}>
          <span>&copy; 2026 E2EDocs</span> • <span>Developed by AravindRamesh</span>
        </div>
      </div>
    </div>
  );
}
