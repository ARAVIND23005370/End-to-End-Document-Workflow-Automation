import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Check, AlertCircle, ShieldCheck } from 'lucide-react';
import { Button, Input } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import { useAuth } from '../../context/AuthContext';
import { authService } from '../../services/api';
import brandLogo from '../../assets/brand/e2edocs-logo.png';

export default function SignupPage() {
  useDocumentTitle('Create an E2EDocs Account');
  const navigate = useNavigate();
  const { register } = useAuth();

  const [signupAvailable, setSignupAvailable] = useState<boolean | null>(null);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [organizationName, setOrganizationName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [agreeTerms, setAgreeTerms] = useState(false);

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    authService.getSignupStatus()
      .then((res) => setSignupAvailable(res.available))
      .catch(() => setSignupAvailable(true));
  }, []);

  const validateForm = (): string | null => {
    if (!name.trim()) return 'Full name is required';
    if (!email.trim()) return 'Email address is required';
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.trim())) return 'Please enter a valid email address';
    if (!organizationName.trim()) return 'Organization name is required';
    if (!password) return 'Password is required';
    if (password.length < 8) return 'Password must be at least 8 characters long';
    if (password !== confirmPassword) return 'Passwords do not match';
    if (!agreeTerms) return 'You must agree to the Terms of Service and Privacy Policy';
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      await register({
        name: name.trim(),
        email: email.trim().toLowerCase(),
        organizationName: organizationName.trim(),
        password,
      });
      setLoading(false);
      navigate('/dashboard');
    } catch (err: any) {
      setLoading(false);
      setError(err?.message || 'Failed to create account. Please try again.');
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
            {signupAvailable === false ? 'Administrator Setup Complete' : 'Create your E2EDocs account'}
          </h2>
          <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)' }}>
            {signupAvailable === false
              ? 'Public registration is disabled for this system'
              : 'Start automating document workflows for your organization'}
          </p>
        </div>

        {/* Form Card or Disabled Notice */}
        {signupAvailable === false ? (
          <div style={{
            backgroundColor: 'var(--bg-elevated)',
            border: '1px solid var(--border-secondary)',
            borderRadius: 'var(--radius-lg)',
            padding: 'var(--space-6)',
            boxShadow: 'var(--shadow-sm)',
            textAlign: 'center',
          }}>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 48,
              height: 48,
              borderRadius: '50%',
              backgroundColor: 'var(--color-brand-50)',
              color: 'var(--color-brand-600)',
              marginBottom: 'var(--space-4)',
            }}>
              <ShieldCheck size={24} />
            </div>
            <h3 style={{ fontSize: 'var(--text-body-lg)', fontWeight: 'var(--weight-semibold)', marginBottom: 'var(--space-2)' }}>
              Initial Main Admin Configured
            </h3>
            <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', marginBottom: 'var(--space-6)', lineHeight: 1.5 }}>
              The initial administrator account has already been registered. Additional users must be invited directly by the administrator.
            </p>
            <Link to="/login" style={{ display: 'block', width: '100%' }}>
              <Button variant="primary" size="lg" style={{ width: '100%' }}>
                Sign In to Your Account
              </Button>
            </Link>
          </div>
        ) : (
        <div style={{
          backgroundColor: 'var(--bg-elevated)',
          border: '1px solid var(--border-secondary)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-sm)',
        }}>
          <form onSubmit={handleSubmit} noValidate>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              {/* Full Name */}
              <Input
                label="Full Name"
                type="text"
                placeholder="Jane Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoComplete="name"
                disabled={loading}
              />

              {/* Work Email */}
              <Input
                label="Work Email Address"
                type="email"
                placeholder="jane@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                disabled={loading}
              />

              {/* Organization Name */}
              <Input
                label="Organization / Company Name"
                type="text"
                placeholder="Acme Corp"
                value={organizationName}
                onChange={(e) => setOrganizationName(e.target.value)}
                required
                autoComplete="organization"
                disabled={loading}
              />

              {/* Password */}
              <div className="input-group">
                <label className="input-label" htmlFor="signup-password">Password</label>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <input
                    id="signup-password"
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
                <label className="input-label" htmlFor="signup-confirm-password">Confirm Password</label>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <input
                    id="signup-confirm-password"
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

              {/* Password Helpers */}
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

              {/* Terms Checkbox */}
              <label style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 'var(--space-2)',
                fontSize: 'var(--text-body-sm)',
                color: 'var(--text-secondary)',
                cursor: 'pointer',
                marginTop: 'var(--space-1)',
              }}>
                <input
                  type="checkbox"
                  checked={agreeTerms}
                  onChange={(e) => setAgreeTerms(e.target.checked)}
                  disabled={loading}
                  style={{ marginTop: 3 }}
                />
                <span>
                  I agree to the <span style={{ color: 'var(--text-link)', textDecoration: 'underline' }}>Terms of Service</span> and{' '}
                  <span style={{ color: 'var(--text-link)', textDecoration: 'underline' }}>Privacy Policy</span>
                </span>
              </label>

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
                {loading ? 'Creating account…' : 'Create Account'}
              </Button>
            </div>
          </form>
        </div>
        )}

        {/* Navigation to Sign In */}
        {signupAvailable !== false && (
          <div style={{
            marginTop: 'var(--space-5)',
            textAlign: 'center',
            fontSize: 'var(--text-body-sm)',
            color: 'var(--text-secondary)',
          }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--text-link)', fontWeight: 'var(--weight-semibold)' }}>
              Sign in
            </Link>
          </div>
        )}

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
