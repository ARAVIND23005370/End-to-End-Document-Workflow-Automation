// ===========================
// E2EDocs — Reset Password Page
// ===========================

import { useState, useEffect } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Check, AlertCircle, CheckCircle2, ArrowRight } from 'lucide-react';
import { Button, Input } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import { authService } from '../../services/api';

import brandLogo from '../../assets/brand/e2edocs-logo.png';

export default function ResetPasswordPage() {
  useDocumentTitle('Reset Password');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const tokenParam = searchParams.get('token') || '';
  const emailParam = searchParams.get('email') || '';

  const [token, setToken] = useState(tokenParam);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (tokenParam) {
      setToken(tokenParam);
    }
  }, [tokenParam]);

  const validate = (): string | null => {
    if (!token.trim()) return 'Reset token is required';
    if (!password) return 'Password is required';
    if (password.length < 8) return 'Password must be at least 8 characters long';
    if (password !== confirmPassword) return 'Passwords do not match';
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      await authService.resetPassword(token.trim(), password);
      setSuccess(true);
    } catch (err: any) {
      setError(err?.message || 'Failed to reset password. The link or token may be invalid or expired.');
    } finally {
      setLoading(false);
    }
  };

  const isPasswordLongEnough = password.length >= 8;
  const doPasswordsMatch = confirmPassword.length > 0 && password === confirmPassword;

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--bg-secondary)',
      padding: 'var(--space-6) var(--space-4)',
    }}>
      <div style={{ width: '100%', maxWidth: 440 }}>
        {/* Brand Logo & Header */}
        <div style={{ textAlign: 'center', marginBottom: 'var(--space-6)' }}>
          <Link to="/" style={{ display: 'inline-block', marginBottom: 'var(--space-3)' }}>
            <img
              src={brandLogo}
              alt="E2EDocs"
              style={{ width: 130, height: 'auto', objectFit: 'contain' }}
            />
          </Link>
          <h2 style={{
            fontSize: 'var(--text-h3)',
            fontWeight: 'var(--weight-bold)',
            color: 'var(--text-primary)',
            marginBottom: 'var(--space-1)',
          }}>
            {success ? 'Password Reset Complete' : 'Set Your New Password'}
          </h2>
          <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
            {emailParam ? `Account: ${emailParam}` : 'Enter your new credentials below'}
          </p>
        </div>

        {/* Card */}
        <div style={{
          backgroundColor: 'var(--bg-elevated)',
          border: '1px solid var(--border-secondary)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-sm)',
        }}>
          {success ? (
            <div style={{ textAlign: 'center' }}>
              <div style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 48,
                height: 48,
                borderRadius: '50%',
                backgroundColor: 'var(--color-success-50, #f0fdf4)',
                color: 'var(--color-success-600, #16a34a)',
                marginBottom: 'var(--space-4)',
              }}>
                <CheckCircle2 size={26} />
              </div>
              <h3 style={{ fontSize: 'var(--text-body-lg)', fontWeight: 'var(--weight-semibold)', marginBottom: 'var(--space-2)' }}>
                Your password has been updated
              </h3>
              <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-6)', lineHeight: 1.5 }}>
                You can now sign in with your new password to access your documents and workflows.
              </p>
              <Button
                variant="primary"
                size="lg"
                onClick={() => navigate('/login')}
                style={{ width: '100%' }}
              >
                Sign In Now <ArrowRight size={16} />
              </Button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} noValidate>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                {/* Reset Token (if not in URL, allow manual pasting) */}
                {!tokenParam && (
                  <Input
                    label="Reset Token"
                    placeholder="Enter security token from email"
                    value={token}
                    onChange={(e) => setToken(e.target.value)}
                    required
                    disabled={loading}
                  />
                )}

                {/* New Password */}
                <div className="input-group">
                  <label className="input-label" htmlFor="reset-new-password">New Password</label>
                  <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                    <input
                      id="reset-new-password"
                      type={showPassword ? 'text' : 'password'}
                      placeholder="Minimum 8 characters"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      autoComplete="new-password"
                      disabled={loading}
                      className="input"
                      style={{ paddingRight: 40 }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      tabIndex={-1}
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                      style={{
                        position: 'absolute',
                        right: 8,
                        background: 'none',
                        border: 'none',
                        color: 'var(--text-tertiary)',
                        cursor: 'pointer',
                        padding: 4,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                {/* Confirm Password */}
                <div className="input-group">
                  <label className="input-label" htmlFor="reset-confirm-password">Confirm New Password</label>
                  <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                    <input
                      id="reset-confirm-password"
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="Re-enter your password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                      autoComplete="new-password"
                      disabled={loading}
                      className="input"
                      style={{ paddingRight: 40 }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      tabIndex={-1}
                      aria-label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
                      style={{
                        position: 'absolute',
                        right: 8,
                        background: 'none',
                        border: 'none',
                        color: 'var(--text-tertiary)',
                        cursor: 'pointer',
                        padding: 4,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                {/* Helper validation checks */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', marginTop: '-var(--space-2)' }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 'var(--space-1)',
                    fontSize: 'var(--text-caption)',
                    color: isPasswordLongEnough ? 'var(--color-success-600)' : 'var(--text-tertiary)',
                  }}>
                    {isPasswordLongEnough ? <Check size={12} /> : <span style={{ width: 12, height: 12, display: 'inline-block' }}>•</span>}
                    <span>At least 8 characters</span>
                  </div>
                  {confirmPassword && (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 'var(--space-1)',
                      fontSize: 'var(--text-caption)',
                      color: doPasswordsMatch ? 'var(--color-success-600)' : 'var(--color-error-600)',
                    }}>
                      {doPasswordsMatch ? <Check size={12} /> : <span style={{ width: 12, height: 12, display: 'inline-block' }}>•</span>}
                      <span>{doPasswordsMatch ? 'Passwords match' : 'Passwords do not match'}</span>
                    </div>
                  )}
                </div>

                {/* Error Banner */}
                {error && (
                  <div style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: 'var(--space-2)',
                    padding: 'var(--space-2) var(--space-3)',
                    backgroundColor: 'var(--color-error-50)',
                    border: '1px solid var(--color-error-200)',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: 'var(--text-body-sm)',
                    color: 'var(--color-error-700)',
                  }} role="alert">
                    <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 2 }} />
                    <span>{error}</span>
                  </div>
                )}

                {/* Submit Button */}
                <Button
                  variant="primary"
                  size="lg"
                  type="submit"
                  disabled={loading}
                  style={{ width: '100%', marginTop: 'var(--space-2)' }}
                >
                  {loading ? 'Saving new password…' : 'Save & Set Password'}
                </Button>

                <div style={{ textAlign: 'center', marginTop: 'var(--space-2)' }}>
                  <Link to="/login" style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-link)' }}>
                    Back to Sign In
                  </Link>
                </div>
              </div>
            </form>
          )}
        </div>

        {/* Footer Credit */}
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
