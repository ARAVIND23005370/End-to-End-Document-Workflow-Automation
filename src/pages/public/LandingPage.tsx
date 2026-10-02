// ===========================
// E2EDocs — Landing Page
// ===========================

import { Link } from 'react-router-dom';
import {
  FileText, GitBranch, Workflow, Bell, Shield, BarChart3,
  ArrowRight, Upload, Search, Users, CheckCircle, Lock,
  Settings, Zap, Eye, Route,
} from 'lucide-react';
import { Button } from '../../components/ui';
import { useDocumentTitle } from '../../hooks';
import brandIcon from '../../assets/brand/e2edocs-icon.png';
import '../../styles/landing.css';

export default function LandingPage() {
  useDocumentTitle('');

  return (
    <div className="landing">
      {/* Navigation */}
      <nav className="landing-nav" aria-label="Main navigation">
        <div className="landing-nav-inner">
          <Link to="/" className="landing-nav-logo">
            <img
              src={brandIcon}
              alt="E2EDocs"
              style={{ width: 28, height: 28, objectFit: 'contain' }}
            />
            <span style={{ fontSize: 'var(--text-body)', fontWeight: 'var(--weight-bold)', letterSpacing: 'var(--tracking-tight)' }}>
              E2E<span style={{ color: 'var(--color-brand-600)' }}>Docs</span>
            </span>
          </Link>
          <div className="landing-nav-links">
            <a href="#features" className="landing-nav-link">Features</a>
            <a href="#how-it-works" className="landing-nav-link">How It Works</a>
            <a href="#rule-engine" className="landing-nav-link">Rule Engine</a>
            <a href="#security" className="landing-nav-link">Security</a>
          </div>
          <div className="landing-nav-actions">
            <Link to="/login"><Button variant="ghost" size="sm">Sign In</Button></Link>
            <Link to="/signup"><Button variant="primary" size="sm">Get Started</Button></Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="hero">
        <span className="hero-eyebrow">Document Workflow Automation</span>
        <h1 className="hero-heading">
          A smarter way to move documents through your business
        </h1>
        <p className="hero-description">
          E2EDocs lets teams define rules that classify, route, prioritize and process documents through configurable workflows — no manual sorting required.
        </p>
        <div className="hero-actions">
          <Link to="/signup"><Button variant="primary" size="lg">Get Started <ArrowRight size={16} /></Button></Link>
          <a href="#features"><Button variant="secondary" size="lg">Explore E2EDocs</Button></a>
        </div>

        {/* Product Visualization */}
        <div className="product-viz">
          <div className="product-viz-header">
            <span className="product-viz-dot" /><span className="product-viz-dot" /><span className="product-viz-dot" />
          </div>
          <div className="product-viz-content">
            <div className="flow-pipeline">
              <div className="flow-step">
                <div className="flow-step-icon"><Upload size={20} style={{ color: 'var(--color-brand-600)' }} /></div>
                <span className="flow-step-label">Document<br />Upload</span>
              </div>
              <div className="flow-arrow" />
              <div className="flow-step">
                <div className="flow-step-icon"><Search size={20} style={{ color: 'var(--color-brand-600)' }} /></div>
                <span className="flow-step-label">Content<br />Extraction</span>
              </div>
              <div className="flow-arrow" />
              <div className="flow-step">
                <div className="flow-step-icon" style={{ borderColor: 'var(--color-brand-300)', backgroundColor: 'var(--color-brand-50)' }}>
                  <GitBranch size={20} style={{ color: 'var(--color-brand-600)' }} />
                </div>
                <span className="flow-step-label" style={{ color: 'var(--color-brand-600)', fontWeight: 'var(--weight-semibold)' }}>Rule<br />Engine</span>
              </div>
              <div className="flow-arrow" />
              <div className="flow-step">
                <div className="flow-step-icon"><Route size={20} style={{ color: 'var(--color-brand-600)' }} /></div>
                <span className="flow-step-label">Department<br />Routing</span>
              </div>
              <div className="flow-arrow" />
              <div className="flow-step">
                <div className="flow-step-icon"><Workflow size={20} style={{ color: 'var(--color-brand-600)' }} /></div>
                <span className="flow-step-label">Workflow<br />Execution</span>
              </div>
              <div className="flow-arrow" />
              <div className="flow-step">
                <div className="flow-step-icon"><CheckCircle size={20} style={{ color: 'var(--color-success-500)' }} /></div>
                <span className="flow-step-label">Decision &amp;<br />Notification</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="landing-section section-center" style={{ backgroundColor: 'var(--color-gray-50)' }}>
        <span className="section-eyebrow">Features</span>
        <h2 className="section-heading" style={{ textAlign: 'center' }}>Everything you need to automate document workflows</h2>
        <p className="section-description" style={{ textAlign: 'center' }}>
          From ingestion to decision, E2EDocs provides the tools to move documents efficiently.
        </p>
        <div className="feature-grid" style={{ maxWidth: 1200 }}>
          {[
            { icon: GitBranch, title: 'Configurable Rule Engine', description: 'Define conditions and actions that automatically classify, route, and prioritize documents based on your business logic.' },
            { icon: Workflow, title: 'Workflow Automation', description: 'Build multi-step workflows with review gates, approval chains, and automated notifications.' },
            { icon: Route, title: 'Intelligent Routing', description: 'Route documents to the right department, team, or individual based on configurable rules.' },
            { icon: Shield, title: 'Role-Based Access', description: 'Control who can view, edit, approve, and manage documents with granular permissions.' },
            { icon: BarChart3, title: 'Analytics & Reporting', description: 'Track document throughput, workflow performance, and team workload with detailed reports.' },
            { icon: Bell, title: 'Real-Time Notifications', description: 'Stay informed with alerts for assignments, status changes, and items requiring attention.' },
            { icon: Eye, title: 'Complete Audit Trail', description: 'Every action is logged. Know who did what, when, and why across your entire document lifecycle.' },
            { icon: Settings, title: 'Fully Configurable', description: 'No hardcoded document types or workflows. Configure E2EDocs to match your exact business processes.' },
            { icon: Zap, title: 'Priority Management', description: 'Automatically assign priorities based on document content, type, or business rules to ensure critical items are handled first.' },
          ].map((feature) => (
            <div key={feature.title} className="feature-card">
              <div className="feature-icon"><feature.icon size={20} /></div>
              <h3 className="feature-title">{feature.title}</h3>
              <p className="feature-description">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="landing-section section-center">
        <span className="section-eyebrow">How It Works</span>
        <h2 className="section-heading" style={{ textAlign: 'center' }}>Four steps to automate your document workflows</h2>
        <div className="how-steps" style={{ maxWidth: 1000 }}>
          {[
            { num: 1, title: 'Upload', description: 'Documents enter the system through upload, email, API, or integration.' },
            { num: 2, title: 'Evaluate', description: 'The rule engine evaluates each document against your configured conditions.' },
            { num: 3, title: 'Route', description: 'Documents are assigned priority, routed to departments, and workflows are triggered.' },
            { num: 4, title: 'Resolve', description: 'Teams review, approve, or escalate. Every action is tracked in the audit log.' },
          ].map((step) => (
            <div key={step.num} className="how-step">
              <span className="how-step-number">{step.num}</span>
              <h3 className="how-step-title">{step.title}</h3>
              <p className="how-step-description">{step.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Rule Engine */}
      <section id="rule-engine" className="landing-section" style={{ backgroundColor: 'var(--color-gray-50)' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-12)', alignItems: 'center' }}>
            <div>
              <span className="section-eyebrow">Rule Engine</span>
              <h2 className="section-heading">Business logic, not code</h2>
              <p className="section-description" style={{ marginBottom: 'var(--space-6)' }}>
                Define rules with conditions and actions. When a document matches, E2EDocs takes action automatically — routing it to the right team, setting priority, triggering workflows, or sending notifications.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                {['Conditions with AND/OR logic', 'Configurable evaluation order', 'Multiple action types per rule', 'No coding required'].map((item) => (
                  <div key={item} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', fontSize: 'var(--text-body-sm)' }}>
                    <CheckCircle size={16} style={{ color: 'var(--color-success-500)', flexShrink: 0 }} />
                    {item}
                  </div>
                ))}
              </div>
            </div>
            {/* Rule example visualization */}
            <div style={{
              border: '1px solid var(--border-primary)', borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--bg-primary)', overflow: 'hidden',
            }}>
              <div style={{
                padding: 'var(--space-3) var(--space-4)', borderBottom: '1px solid var(--border-secondary)',
                fontSize: 'var(--text-body-sm)', fontWeight: 'var(--weight-medium)',
              }}>
                Example Rule
              </div>
              <div style={{ padding: 'var(--space-4)', fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)' }}>
                <div style={{ color: 'var(--text-tertiary)', marginBottom: 'var(--space-2)' }}>// Financial Document Routing</div>
                <div style={{ marginBottom: 'var(--space-3)' }}>
                  <span style={{ color: 'var(--color-brand-600)' }}>WHEN</span>
                </div>
                <div style={{ paddingLeft: 'var(--space-4)', marginBottom: 'var(--space-1)' }}>
                  document.type <span style={{ color: 'var(--color-warning-600)' }}>contains</span> "Financial"
                </div>
                <div style={{ paddingLeft: 'var(--space-4)', marginBottom: 'var(--space-3)', color: 'var(--text-tertiary)' }}>
                  <span style={{ color: 'var(--color-brand-600)' }}>AND</span> document.tags <span style={{ color: 'var(--color-warning-600)' }}>contains</span> "quarterly"
                </div>
                <div style={{ marginBottom: 'var(--space-3)' }}>
                  <span style={{ color: 'var(--color-success-600)' }}>THEN</span>
                </div>
                <div style={{ paddingLeft: 'var(--space-4)', marginBottom: 'var(--space-1)' }}>
                  Route → <span style={{ color: 'var(--color-brand-600)' }}>Finance</span>
                </div>
                <div style={{ paddingLeft: 'var(--space-4)', marginBottom: 'var(--space-1)' }}>
                  Priority → <span style={{ color: 'var(--color-warning-600)' }}>High</span>
                </div>
                <div style={{ paddingLeft: 'var(--space-4)' }}>
                  Workflow → <span style={{ color: 'var(--color-brand-600)' }}>Quarterly Review</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Security */}
      <section id="security" className="landing-section section-center">
        <span className="section-eyebrow">Enterprise-Grade</span>
        <h2 className="section-heading" style={{ textAlign: 'center' }}>Security and auditability built in</h2>
        <p className="section-description" style={{ textAlign: 'center', marginBottom: 'var(--space-8)' }}>
          E2EDocs provides role-based access control, complete audit trails, and enterprise security features from day one.
        </p>
        <div className="feature-grid" style={{ maxWidth: 900 }}>
          {[
            { icon: Lock, title: 'Role-Based Access Control', description: 'Configurable roles and permissions ensure users only access what they need.' },
            { icon: Eye, title: 'Complete Audit Logging', description: 'Every document action, rule trigger, and user activity is logged with timestamps.' },
            { icon: Users, title: 'Team Management', description: 'Organize users by department and role for efficient document routing and oversight.' },
          ].map((item) => (
            <div key={item.title} className="feature-card">
              <div className="feature-icon"><item.icon size={20} /></div>
              <h3 className="feature-title">{item.title}</h3>
              <p className="feature-description">{item.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="cta-section">
        <h2 className="cta-heading">Ready to automate your document workflows?</h2>
        <p className="cta-description">
          Start defining rules, building workflows, and routing documents in minutes.
        </p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: 'var(--space-3)' }}>
          <Link to="/signup"><Button variant="primary" size="lg">Get Started <ArrowRight size={16} /></Button></Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <div className="landing-footer-inner">
          <div className="footer-brand">
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-2)' }}>
              <img
                src={brandIcon}
                alt="E2EDocs"
                style={{ width: 24, height: 24, objectFit: 'contain' }}
              />
              <span style={{ fontSize: 'var(--text-body-sm)', fontWeight: 'var(--weight-bold)' }}>
                E2E<span style={{ color: 'var(--color-brand-600)' }}>Docs</span>
              </span>
            </div>
            <p>Document workflow automation platform</p>
            <p style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', marginTop: 'var(--space-2)' }}>
              Developed by AravindRamesh
            </p>
          </div>
          <div className="footer-column">
            <h4>Product</h4>
            <ul>
              <li><a href="#features">Features</a></li>
              <li><a href="#rule-engine">Rule Engine</a></li>
              <li><a href="#how-it-works">How It Works</a></li>
              <li><a href="#security">Security</a></li>
            </ul>
          </div>
          <div className="footer-column">
            <h4>Company</h4>
            <ul>
              <li><a href="#">About</a></li>
              <li><a href="#">Careers</a></li>
              <li><a href="#">Contact</a></li>
            </ul>
          </div>
          <div className="footer-column">
            <h4>Legal</h4>
            <ul>
              <li><a href="#">Privacy</a></li>
              <li><a href="#">Terms</a></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          <span>&copy; 2026 E2EDocs. All rights reserved. &bull; Developed by AravindRamesh</span>
          <span>Document Workflow Automation</span>
        </div>
      </footer>
    </div>
  );
}
