// ===========================
// E2EDocs — Document Detail Page
// ===========================

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Download,
  Clock,
  CheckCircle,
  XCircle,
  FileText,
  User,
  Building2,
  Mail,
  Share2,
  Send,
} from 'lucide-react';
import {
  Button,
  Card,
  CardHeader,
  CardBody,
  Breadcrumb,
  StatusBadge,
  PriorityBadge,
  Badge,
  Avatar,
  Tabs,
  LoadingState,
  ErrorState,
  Modal,
  Input,
  Textarea,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { documentService } from '../../services/api';
import { DOCUMENT_SOURCE_LABELS } from '../../constants';
import { formatDate, formatRelativeTime, formatFileSize } from '../../utils';

const EVAL_ICONS: Record<string, React.ElementType> = {
  true: CheckCircle,
  false: XCircle,
};

export default function DocumentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');

  // Email modal state
  const [isEmailModalOpen, setIsEmailModalOpen] = useState(false);
  const [emailRecipient, setEmailRecipient] = useState('');
  const [emailSubject, setEmailSubject] = useState('');
  const [emailMessage, setEmailMessage] = useState('');
  const [emailLoading, setEmailLoading] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [emailSuccess, setEmailSuccess] = useState<string | null>(null);

  const { data: doc, status, error, refetch } = useAsync(
    () => documentService.getById(id || ''),
    [id]
  );

  useDocumentTitle(doc ? doc.name : 'Document');

  const handleDownload = async () => {
    if (!doc) return;
    try {
      await documentService.download(doc.id, doc.name);
    } catch (err: any) {
      alert(err.message || 'Failed to download document');
    }
  };

  const handleOpenEmailModal = () => {
    if (!doc) return;
    setEmailRecipient(doc.originalSender?.email || '');
    setEmailSubject(`Document: ${doc.name}`);
    setEmailMessage(`Please find attached the document "${doc.name}" for your reference.`);
    setEmailError(null);
    setEmailSuccess(null);
    setIsEmailModalOpen(true);
  };

  const handleSendEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!doc) return;
    if (!emailRecipient.trim()) {
      setEmailError('Recipient email is required');
      return;
    }
    if (!emailSubject.trim()) {
      setEmailError('Subject is required');
      return;
    }

    try {
      setEmailLoading(true);
      setEmailError(null);
      await documentService.sendEmail(doc.id, {
        recipient: emailRecipient.trim(),
        subject: emailSubject.trim(),
        message: emailMessage.trim(),
      });
      setEmailSuccess('Document sent successfully via email!');
      setTimeout(() => {
        setIsEmailModalOpen(false);
        setEmailSuccess(null);
        refetch();
      }, 1500);
    } catch (err: any) {
      setEmailError(err.message || 'Failed to send document email.');
    } finally {
      setEmailLoading(false);
    }
  };

  if (status === 'loading') return <LoadingState message="Loading document…" />;
  if (status === 'error' || !doc) return <ErrorState message={error || 'Document not found'} onRetry={refetch} />;

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'rules', label: 'Rule Evaluation' },
    { id: 'history', label: 'Audit History' },
  ];

  const sourceLabel = DOCUMENT_SOURCE_LABELS[doc.source] || doc.source || 'Manual Upload';

  return (
    <div>
      {/* Breadcrumb & actions */}
      <div className="page-header">
        <Breadcrumb items={[
          { label: 'Documents', href: '/documents' },
          { label: doc.name },
        ]} />
        <div className="page-header-row" style={{ marginTop: 'var(--space-3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Button variant="ghost" icon size="sm" onClick={() => navigate('/documents')} aria-label="Back">
              <ArrowLeft size={16} />
            </Button>
            <div>
              <h1 className="page-title" style={{ fontSize: 'var(--text-h3)' }}>{doc.name}</h1>
              <p className="page-description" style={{ fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)' }}>{doc.id}</p>
            </div>
          </div>
          <div className="page-actions" style={{ display: 'flex', gap: 'var(--space-2)' }}>
            {doc.status === 'review' && (
              <>
                <Button
                  variant="primary"
                  size="sm"
                  style={{ backgroundColor: 'var(--color-success-600, #16a34a)', borderColor: 'var(--color-success-600, #16a34a)' }}
                  onClick={async () => {
                    try {
                      await documentService.update(doc.id, { status: 'approved' });
                      refetch();
                    } catch (e: any) {
                      alert(e.message || 'Failed to approve document');
                    }
                  }}
                >
                  <CheckCircle size={14} /> Approve Document
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={async () => {
                    try {
                      await documentService.update(doc.id, { status: 'rejected' });
                      refetch();
                    } catch (e: any) {
                      alert(e.message || 'Failed to reject document');
                    }
                  }}
                >
                  <XCircle size={14} /> Reject Document
                </Button>
              </>
            )}
            <Button variant="secondary" size="sm" onClick={handleDownload}><Download size={14} /> Download</Button>
            <Button variant="secondary" size="sm" onClick={handleOpenEmailModal}><Mail size={14} /> Send via Email</Button>
          </div>
        </div>
      </div>

      {/* Missing Fields / Rejection Alert Banner */}
      {(doc.missingFields || doc.decisionReason) && (
        <div style={{
          backgroundColor: doc.status === 'rejected' ? 'var(--color-error-50, #fef2f2)' : 'var(--color-warning-50, #fffbeb)',
          border: `1px solid ${doc.status === 'rejected' ? 'var(--color-error-200, #fecaca)' : 'var(--color-warning-200, #fde68a)'}`,
          borderRadius: 'var(--radius-md)',
          padding: 'var(--space-3) var(--space-4)',
          marginBottom: 'var(--space-4)',
          fontSize: 'var(--text-body-sm)',
        }}>
          {doc.decisionReason && (
            <div style={{ fontWeight: 600, marginBottom: doc.missingFields ? 'var(--space-1)' : 0 }}>
              {doc.decisionReason}
            </div>
          )}
          {doc.missingFields && (
            <div style={{ color: doc.status === 'rejected' ? 'var(--color-error-700, #b91c1c)' : 'var(--color-warning-800, #92400e)' }}>
              <strong>Missing / Required Fields Detected:</strong> {doc.missingFields}
            </div>
          )}
        </div>
      )}

      {/* Key info bar */}
      <div style={{ display: 'flex', gap: 'var(--space-6)', flexWrap: 'wrap', marginBottom: 'var(--space-6)' }}>
        <InfoPair icon={<Clock size={14} />} label="Updated" value={formatRelativeTime(doc.updatedAt)} />
        <InfoPair label="Status"><StatusBadge status={doc.status} /></InfoPair>
        <InfoPair label="Priority"><PriorityBadge priority={doc.priority} /></InfoPair>
        <InfoPair icon={<Share2 size={14} />} label="Source" value={sourceLabel} />
        <InfoPair icon={<Building2 size={14} />} label="Department" value={doc.department || 'Not assigned'} />
        <InfoPair icon={<User size={14} />} label="Assigned To" value={doc.assignedTo || 'Unassigned'} />
        <InfoPair icon={<FileText size={14} />} label="Folder / Category" value={doc.folder || doc.type} />
        <InfoPair label="Size" value={formatFileSize(doc.size)} />
      </div>

      {/* Tabs */}
      <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />

      <div style={{ marginTop: 'var(--space-4)' }}>
        {activeTab === 'overview' && (
          <div className="form-grid-2">
            {/* Document Information (Generic Metadata Section) */}
            <Card style={{ gridColumn: '1 / -1' }}>
              <CardHeader>
                <div>
                  <div className="card-title">DOCUMENT INFORMATION</div>
                  <div className="card-subtitle">Comprehensive document metadata and ingestion details</div>
                </div>
              </CardHeader>
              <CardBody>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                  gap: 'var(--space-4)',
                }}>
                  <MetaField label="Document Name" value={doc.name} isPrimary />
                  
                  <MetaField
                    label="Document Description"
                    value={doc.description}
                    fallback="Not provided"
                    fullWidth
                  />

                  <MetaField
                    label="Original Sender Name"
                    value={doc.originalSender?.name}
                    fallback="Not available"
                    icon={<User size={14} />}
                  />

                  <MetaField
                    label="Original Sender Email"
                    value={doc.originalSender?.email}
                    fallback="Not available"
                    icon={<Mail size={14} />}
                    isMono
                  />

                  <MetaField label="Document Type" value={doc.type} />

                  <MetaField label="Source" value={sourceLabel} />

                  <MetaField label="Created" value={formatDate(doc.createdAt)} />

                  <MetaField
                    label="Updated"
                    value={`${formatDate(doc.updatedAt)} (${formatRelativeTime(doc.updatedAt)})`}
                  />

                  <MetaField
                    label="Status"
                    customRender={<StatusBadge status={doc.status} />}
                  />

                  <MetaField
                    label="Priority"
                    customRender={<PriorityBadge priority={doc.priority} />}
                  />

                  <MetaField
                    label="Department"
                    value={doc.department}
                    fallback="Not assigned"
                  />

                  <MetaField
                    label="Assigned To"
                    value={doc.assignedTo}
                    fallback="Unassigned"
                  />

                  <MetaField label="File Size" value={formatFileSize(doc.size)} />
                </div>

                {doc.tags && doc.tags.length > 0 && (
                  <div style={{ marginTop: 'var(--space-4)', paddingTop: 'var(--space-4)', borderTop: '1px solid var(--border-secondary)' }}>
                    <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>Tags</div>
                    <div style={{ display: 'flex', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
                      {doc.tags.map((tag) => (
                        <Badge key={tag} variant="brand">{tag}</Badge>
                      ))}
                    </div>
                  </div>
                )}
              </CardBody>
            </Card>

            {/* Content Preview if available */}
            {doc.contentPreview && (
              <Card>
                <CardHeader><div className="card-title">Content Preview</div></CardHeader>
                <CardBody>
                  <p style={{ fontSize: 'var(--text-body-sm)', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                    {doc.contentPreview}
                  </p>
                </CardBody>
              </Card>
            )}

            {/* Detected Information */}
            {doc.detectedInfo && doc.detectedInfo.length > 0 && (
              <Card>
                <CardHeader><div className="card-title">Detected Information</div></CardHeader>
                <CardBody>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                    {doc.detectedInfo.map((info) => (
                      <div key={info.label} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--text-body-sm)' }}>
                        <span style={{ color: 'var(--text-secondary)' }}>{info.label}</span>
                        <span style={{ fontWeight: 500 }}>{info.value}</span>
                      </div>
                    ))}
                  </div>
                </CardBody>
              </Card>
            )}

            {/* Assignment Card */}
            {doc.assignedTo && (
              <Card style={{ gridColumn: doc.contentPreview && doc.detectedInfo?.length ? '1 / -1' : undefined }}>
                <CardHeader><div className="card-title">Assignment</div></CardHeader>
                <CardBody>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                    <Avatar name={doc.assignedTo} size="lg" />
                    <div>
                      <div style={{ fontWeight: 500, fontSize: 'var(--text-body-sm)' }}>{doc.assignedTo}</div>
                      <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)' }}>{doc.department || 'General'}</div>
                    </div>
                  </div>
                </CardBody>
              </Card>
            )}
          </div>
        )}

        {activeTab === 'rules' && (
          <Card>
            <CardHeader>
              <div>
                <div className="card-title">Rule Evaluation Results</div>
                <div className="card-subtitle">{doc.ruleEvaluations.length} rules evaluated against this document</div>
              </div>
            </CardHeader>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              {doc.ruleEvaluations.map((evaluation, i) => {
                const Icon = EVAL_ICONS[String(evaluation.matched)];
                return (
                  <div key={evaluation.ruleId} style={{
                    display: 'flex', alignItems: 'flex-start', gap: 'var(--space-3)',
                    padding: 'var(--space-4) var(--space-5)',
                    borderBottom: i < doc.ruleEvaluations.length - 1 ? '1px solid var(--border-secondary)' : 'none',
                  }}>
                    <div style={{
                      width: 28, height: 28, borderRadius: 'var(--radius-full)',
                      backgroundColor: evaluation.matched ? 'var(--color-success-50)' : 'var(--color-gray-100)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                    }}>
                      <Icon size={14} style={{ color: evaluation.matched ? 'var(--color-success-600)' : 'var(--color-gray-400)' }} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                        <span style={{ fontSize: 'var(--text-body-sm)', fontWeight: 500 }}>
                          {evaluation.ruleName}
                        </span>
                        <Badge variant={evaluation.matched ? 'success' : 'neutral'}>
                          {evaluation.matched ? 'Matched' : 'No Match'}
                        </Badge>
                      </div>
                      <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', marginBottom: 'var(--space-2)' }}>
                        {evaluation.conditionsChecked} conditions evaluated · {formatRelativeTime(evaluation.evaluatedAt)}
                      </div>
                      {evaluation.matched && evaluation.actionsTriggered.length > 0 && (
                        <div style={{ display: 'flex', gap: 'var(--space-1)', flexWrap: 'wrap' }}>
                          {evaluation.actionsTriggered.map((action) => (
                            <Badge key={action} variant="brand">{action}</Badge>
                          ))}
                        </div>
                      )}
                    </div>
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: 'var(--text-code)', color: 'var(--text-tertiary)' }}>
                      {evaluation.ruleId}
                    </span>
                  </div>
                );
              })}
            </div>
          </Card>
        )}

        {activeTab === 'history' && (
          <Card>
            <CardHeader>
              <div className="card-title">Audit History</div>
            </CardHeader>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              {doc.auditHistory.map((entry, i) => (
                <div key={entry.id} style={{
                  display: 'flex', alignItems: 'flex-start', gap: 'var(--space-3)',
                  padding: 'var(--space-3) var(--space-5)',
                  borderBottom: i < doc.auditHistory.length - 1 ? '1px solid var(--border-secondary)' : 'none',
                }}>
                  <Avatar name={entry.userName} size="sm" />
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 'var(--text-body-sm)' }}>
                      <strong style={{ fontWeight: 500 }}>{entry.userName}</strong> {entry.action} this document
                    </div>
                    <div style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', marginTop: 2 }}>{entry.details}</div>
                  </div>
                  <span style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', whiteSpace: 'nowrap' }}>
                    {formatRelativeTime(entry.timestamp)}
                  </span>
                </div>
              ))}
            </div>
          </Card>
        )}
      </div>

      {/* Send Document via Email Modal */}
      <Modal
        isOpen={isEmailModalOpen}
        onClose={() => !emailLoading && setIsEmailModalOpen(false)}
        title="Send Document via Email"
        footer={
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)' }}>
            <Button
              variant="secondary"
              onClick={() => setIsEmailModalOpen(false)}
              disabled={emailLoading}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleSendEmail}
              disabled={emailLoading}
            >
              <Mail size={14} /> {emailLoading ? 'Sending…' : 'Send Document'}
            </Button>
          </div>
        }
      >
        <form onSubmit={handleSendEmail} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {emailError && (
            <div style={{
              padding: 'var(--space-3)',
              backgroundColor: 'var(--color-danger-50)',
              color: 'var(--color-danger-700)',
              borderRadius: 'var(--radius-md)',
              fontSize: 'var(--text-body-sm)',
              border: '1px solid var(--color-danger-200)',
            }}>
              {emailError}
            </div>
          )}

          {emailSuccess && (
            <div style={{
              padding: 'var(--space-3)',
              backgroundColor: 'var(--color-success-50)',
              color: 'var(--color-success-700)',
              borderRadius: 'var(--radius-md)',
              fontSize: 'var(--text-body-sm)',
              border: '1px solid var(--color-success-200)',
            }}>
              {emailSuccess}
            </div>
          )}

          <div style={{
            padding: 'var(--space-3)',
            backgroundColor: 'var(--color-gray-50)',
            borderRadius: 'var(--radius-md)',
            fontSize: 'var(--text-caption)',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--space-2)',
          }}>
            <FileText size={16} />
            <span>Attachment: <strong>{doc.name}</strong> ({formatFileSize(doc.size)})</span>
          </div>

          <Input
            label="Recipient Email"
            type="email"
            placeholder="colleague@example.com"
            value={emailRecipient}
            onChange={(e) => setEmailRecipient(e.target.value)}
            required
            autoFocus
          />

          <Input
            label="Subject"
            placeholder="e.g. Contract Review"
            value={emailSubject}
            onChange={(e) => setEmailSubject(e.target.value)}
            required
          />

          <Textarea
            label="Message (Optional)"
            placeholder="Add an optional message for the recipient..."
            value={emailMessage}
            onChange={(e) => setEmailMessage(e.target.value)}
            rows={4}
          />
        </form>
      </Modal>
    </div>
  );
}

// --- Helper Components ---
function InfoPair({ icon, label, value, children }: { icon?: React.ReactNode; label: string; value?: string; children?: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', fontSize: 'var(--text-body-sm)' }}>
      {icon && <span style={{ color: 'var(--text-tertiary)' }}>{icon}</span>}
      <span style={{ color: 'var(--text-tertiary)' }}>{label}:</span>
      {children || <span style={{ fontWeight: 500 }}>{value}</span>}
    </div>
  );
}

interface MetaFieldProps {
  label: string;
  value?: string | null;
  fallback?: string;
  customRender?: React.ReactNode;
  icon?: React.ReactNode;
  isPrimary?: boolean;
  isMono?: boolean;
  fullWidth?: boolean;
}

function MetaField({
  label,
  value,
  fallback = 'Not available',
  customRender,
  icon,
  isPrimary,
  isMono,
  fullWidth,
}: MetaFieldProps) {
  const hasValue = value !== undefined && value !== null && value.trim() !== '';

  return (
    <div style={{
      gridColumn: fullWidth ? '1 / -1' : undefined,
      display: 'flex',
      flexDirection: 'column',
      gap: 'var(--space-1)',
    }}>
      <span style={{
        fontSize: 'var(--text-caption)',
        color: 'var(--text-tertiary)',
        textTransform: 'uppercase',
        letterSpacing: '0.05em',
      }}>
        {label}
      </span>
      {customRender ? (
        <div>{customRender}</div>
      ) : hasValue ? (
        <span style={{
          fontSize: 'var(--text-body-sm)',
          fontWeight: isPrimary ? 600 : 500,
          color: 'var(--text-primary)',
          fontFamily: isMono ? 'var(--font-mono)' : undefined,
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--space-1)',
        }}>
          {icon && <span style={{ color: 'var(--text-tertiary)', display: 'inline-flex' }}>{icon}</span>}
          {value}
        </span>
      ) : (
        <span style={{
          fontSize: 'var(--text-body-sm)',
          color: 'var(--text-tertiary)',
          fontStyle: 'italic',
        }}>
          {fallback}
        </span>
      )}
    </div>
  );
}
