// ===========================
// E2EDocs — 404 Not Found Page
// ===========================

import { Link } from 'react-router-dom';
import { ArrowLeft, Home } from 'lucide-react';
import { Button } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import brandIcon from '../../assets/brand/e2edocs-icon.png';

export default function NotFoundPage() {
  useDocumentTitle('Page Not Found');

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--bg-secondary)',
      padding: 'var(--space-6)',
      textAlign: 'center',
    }}>
      <div style={{
        maxWidth: 440,
        width: '100%',
        backgroundColor: 'var(--bg-elevated)',
        border: '1px solid var(--border-secondary)',
        borderRadius: 'var(--radius-lg)',
        padding: 'var(--space-8)',
        boxShadow: 'var(--shadow-md)',
      }}>
        <div style={{ marginBottom: 'var(--space-4)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
          <img
            src={brandIcon}
            alt="E2EDocs"
            style={{ width: 44, height: 44, objectFit: 'contain' }}
          />
        </div>

        <h1 style={{
          fontSize: 'var(--text-h1)',
          fontWeight: 'var(--weight-bold)',
          color: 'var(--text-primary)',
          letterSpacing: 'var(--tracking-tight)',
          marginBottom: 'var(--space-2)',
        }}>
          404
        </h1>

        <h2 style={{
          fontSize: 'var(--text-h3)',
          fontWeight: 'var(--weight-semibold)',
          color: 'var(--text-secondary)',
          marginBottom: 'var(--space-3)',
        }}>
          Page not found
        </h2>

        <p style={{
          fontSize: 'var(--text-body-sm)',
          color: 'var(--text-tertiary)',
          marginBottom: 'var(--space-6)',
          lineHeight: 1.5,
        }}>
          The page you are looking for doesn't exist or has been moved.
        </p>

        <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center' }}>
          <Link to="/">
            <Button variant="secondary" size="md">
              <ArrowLeft size={14} /> Back
            </Button>
          </Link>
          <Link to="/dashboard">
            <Button variant="primary" size="md">
              <Home size={14} /> Dashboard
            </Button>
          </Link>
        </div>
      </div>

      <div style={{ marginTop: 'var(--space-6)', fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>
        E2EDocs &bull; Developed by AravindRamesh
      </div>
    </div>
  );
}
