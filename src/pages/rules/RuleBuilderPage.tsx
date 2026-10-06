// ===========================
// E2EDocs — Section-Isolated Rule Builder Page
// ===========================

import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
  ArrowLeft, Plus, Trash2, CheckCircle2, FolderTree, ArrowUpDown,
  SendHorizontal, Mail, Info, Sparkles, Play, AlertCircle
} from 'lucide-react';
import {
  Button, Card, CardHeader, CardBody, Input, Textarea, SelectField, Breadcrumb, Badge,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService, userService, workflowService } from '../../services/api';
import {
  CONDITION_OPERATORS,
  CONDITION_FIELDS,
  RULE_SECTIONS,
  DECISION_OUTCOME_OPTIONS,
  SORTING_PRIORITY_OPTIONS,
  ROUTING_DESTINATION_TYPES,
  EMAIL_RECIPIENT_OPTIONS,
} from '../../constants';
import { generateId } from '../../utils';
import type {
  ConditionGroup,
  RuleCondition,
  RuleAction,
  ConditionLogic,
  RuleStatus,
  RuleType,
  ActionType,
  EmailRecipientType,
} from '../../types';

const DEFAULT_CONDITION: () => RuleCondition = () => ({
  id: generateId('c'),
  field: 'extracted.text',
  operator: 'contains',
  value: '',
});

const DEFAULT_GROUP: () => ConditionGroup = () => ({
  id: generateId('cg'),
  logic: 'AND',
  conditions: [DEFAULT_CONDITION()],
});

const getFeaturePath = (type: RuleType) => {
  switch (type) {
    case 'decision': return '/decision-rules';
    case 'folder': return '/folder-classification';
    case 'sorting': return '/folder-sorting';
    case 'routing': return '/routing';
    case 'communication': return '/communication';
    default: return '/decision-rules';
  }
};

export default function RuleBuilderPage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const isNew = !id || id === 'new';

  const initialTypeFromQuery = (searchParams.get('type') as RuleType) || 'decision';

  const { data: existingRule } = useAsync(
    () => (isNew ? Promise.resolve(null) : ruleService.getById(id!)),
    [id]
  );

  const { data: users = [] } = useAsync(() => userService.getAll(), []);
  const { data: workflows = [] } = useAsync(() => workflowService.getAll(), []);

  const [ruleType, setRuleType] = useState<RuleType>(initialTypeFromQuery);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<RuleStatus>('active');
  const [evalOrder, setEvalOrder] = useState('1');
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([DEFAULT_GROUP()]);
  
  // Section-specific action states
  const [decisionOutcome, setDecisionOutcome] = useState('approved');
  const [folderName, setFolderName] = useState('');
  const [sortingPriority, setSortingPriority] = useState('high');
  const [routingType, setRoutingType] = useState('assign_user');
  const [routingValue, setRoutingValue] = useState('');
  const [emailRecipientType, setEmailRecipientType] = useState<EmailRecipientType>('original_sender');
  const [customRecipient, setCustomRecipient] = useState('');
  const [emailSubject, setEmailSubject] = useState('Update regarding your document: {{document_name}}');
  const [emailMessage, setEmailMessage] = useState(
    'Hello,\n\nYour document (ID: {{document_id}}) has been processed.\nStatus: {{decision}}\n\nDetails: {{review_reason}}\n\nBest regards,\nE2EDocs Platform'
  );

  const [saveLoading, setSaveLoading] = useState(false);
  const [saveError, setSaveError] = useState('');
  const [testDocumentText, setTestDocumentText] = useState('');
  const [testResult, setTestResult] = useState<any>(null);
  const [testLoading, setTestLoading] = useState(false);

  // Sync existing rule when loaded
  useEffect(() => {
    if (existingRule) {
      setName(existingRule.name);
      setDescription(existingRule.description || '');
      setStatus(existingRule.status);
      setRuleType(existingRule.ruleType || 'decision');
      setEvalOrder(existingRule.evaluationOrder?.toString() || '1');
      if (existingRule.conditionGroups && existingRule.conditionGroups.length > 0) {
        setConditionGroups(existingRule.conditionGroups);
      }

      // Map actions back into section states
      if (existingRule.actions && existingRule.actions.length > 0) {
        const primaryAction = existingRule.actions[0];
        if (existingRule.ruleType === 'decision' || primaryAction.type === 'set_decision') {
          setDecisionOutcome(primaryAction.value || 'approved');
        } else if (existingRule.ruleType === 'folder' || primaryAction.type === 'assign_folder') {
          setFolderName(primaryAction.value || '');
        } else if (existingRule.ruleType === 'sorting' || primaryAction.type === 'set_priority') {
          setSortingPriority(primaryAction.value || 'high');
        } else if (existingRule.ruleType === 'routing') {
          setRoutingType(primaryAction.type);
          setRoutingValue(primaryAction.value || '');
        } else if (existingRule.ruleType === 'communication' || primaryAction.type === 'send_email') {
          if (primaryAction.emailConfig) {
            setEmailRecipientType(primaryAction.emailConfig.recipientType || 'original_sender');
            setCustomRecipient(primaryAction.emailConfig.customRecipient || '');
            setEmailSubject(primaryAction.emailConfig.subject || '');
            setEmailMessage(primaryAction.emailConfig.message || '');
          }
        }
      }
    }
  }, [existingRule]);

  useDocumentTitle(isNew ? `Create ${ruleType.toUpperCase()} Rule` : `Edit Rule — ${name || 'Untitled'}`);

  // Condition handlers
  const addConditionGroup = () => setConditionGroups([...conditionGroups, DEFAULT_GROUP()]);

  const removeConditionGroup = (groupId: string) => {
    if (conditionGroups.length <= 1) return;
    setConditionGroups(conditionGroups.filter((g) => g.id !== groupId));
  };

  const toggleGroupLogic = (groupId: string) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId ? { ...g, logic: g.logic === 'AND' ? 'OR' : 'AND' } : g
    ));
  };

  const addCondition = (groupId: string) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId ? { ...g, conditions: [...g.conditions, DEFAULT_CONDITION()] } : g
    ));
  };

  const removeCondition = (groupId: string, condId: string) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId ? { ...g, conditions: g.conditions.filter((c) => c.id !== condId) } : g
    ));
  };

  const updateCondition = (groupId: string, condId: string, field: keyof RuleCondition, value: any) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId
        ? {
            ...g,
            conditions: g.conditions.map((c) => (c.id === condId ? { ...c, [field]: value } : c)),
          }
        : g
    ));
  };

  // Compile section-specific actions array
  const buildActionsForCurrentSection = (): RuleAction[] => {
    switch (ruleType) {
      case 'decision':
        return [{
          id: generateId('act'),
          type: 'set_decision',
          value: decisionOutcome,
        }];
      case 'folder':
        return [{
          id: generateId('act'),
          type: 'assign_folder',
          value: folderName.trim(),
        }];
      case 'sorting':
        return [{
          id: generateId('act'),
          type: 'set_priority',
          value: sortingPriority,
        }];
      case 'routing':
        return [{
          id: generateId('act'),
          type: routingType as ActionType,
          value: routingValue.trim(),
        }];
      case 'communication':
        return [{
          id: generateId('act'),
          type: 'send_email',
          value: emailRecipientType,
          emailConfig: {
            recipientType: emailRecipientType,
            customRecipient: customRecipient.trim(),
            subject: emailSubject.trim(),
            message: emailMessage.trim(),
          },
        }];
      default:
        return [{
          id: generateId('act'),
          type: 'set_decision',
          value: 'approved',
        }];
    }
  };

  const handleSave = async () => {
    if (!name.trim()) {
      setSaveError('Rule name is required.');
      return;
    }

    if (ruleType === 'folder' && !folderName.trim()) {
      setSaveError('Folder / Category name is required for Folder Rules.');
      return;
    }

    if (ruleType === 'routing' && !routingValue.trim()) {
      setSaveError('Destination value is required for Routing Rules.');
      return;
    }

    if (ruleType === 'communication' && !emailSubject.trim()) {
      setSaveError('Email Subject is required for Communication Rules.');
      return;
    }

    try {
      setSaveLoading(true);
      setSaveError('');

      const payload: Partial<any> = {
        name: name.trim(),
        description: description.trim(),
        status,
        ruleType,
        evaluationOrder: parseInt(evalOrder, 10) || 1,
        conditionGroups,
        actions: buildActionsForCurrentSection(),
      };

      if (isNew) {
        await ruleService.create(payload);
      } else {
        await ruleService.update(id!, payload);
      }

      navigate(getFeaturePath(ruleType));
    } catch (err: any) {
      setSaveError(err.message || 'Failed to save rule.');
    } finally {
      setSaveLoading(false);
    }
  };

  const handleTestRule = async () => {
    try {
      setTestLoading(true);
      const testPayload = {
        rule: {
          name,
          ruleType,
          conditionGroups,
          actions: buildActionsForCurrentSection(),
        },
        testContext: {
          'extracted.text': testDocumentText,
          'document.name': 'Sample_Document.pdf',
          'sender.email': 'customer@example.org',
          'file.extension': '.pdf',
          'document.status': 'processing',
        },
      };

      const result = await ruleService.testRule(testPayload);
      setTestResult(result);
    } catch (err: any) {
      setTestResult({ matched: false, logs: ['Simulation error: ' + err.message] });
    } finally {
      setTestLoading(false);
    }
  };

  const currentSectionMeta = RULE_SECTIONS.find((s) => s.id === ruleType) || RULE_SECTIONS[0];
  const featurePath = getFeaturePath(ruleType);

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <Breadcrumb items={[
          { label: `${currentSectionMeta.label}`, href: featurePath },
          { label: isNew ? 'New Rule' : (name || 'Edit Rule') },
        ]} />
        <div className="page-header-row" style={{ marginTop: 'var(--space-3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Button variant="ghost" icon size="sm" onClick={() => navigate(featurePath)} aria-label="Back">
              <ArrowLeft size={16} />
            </Button>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <h1 className="page-title" style={{ fontSize: 'var(--text-h3)' }}>
                  {isNew ? `Create ${currentSectionMeta.label.replace(' Rules', '')} Rule` : `Edit Rule — ${name || 'Untitled'}`}
                </h1>
                <Badge variant="brand">{ruleType.toUpperCase()} RULE</Badge>
              </div>
              <p className="page-description">{currentSectionMeta.description}</p>
            </div>
          </div>
          <div className="page-actions">
            <Button variant="secondary" onClick={() => navigate(featurePath)}>Cancel</Button>
            <Button variant="primary" onClick={handleSave} disabled={saveLoading}>
              {saveLoading ? 'Saving…' : isNew ? 'Create Rule' : 'Save Changes'}
            </Button>
          </div>
        </div>
      </div>

      {saveError && (
        <div style={{
          backgroundColor: 'var(--color-error-50, #fef2f2)',
          color: 'var(--color-error-700, #b91c1c)',
          padding: 'var(--space-3) var(--space-4)',
          borderRadius: 'var(--radius-md)',
          marginBottom: 'var(--space-4)',
          fontSize: 'var(--text-body-sm)',
          border: '1px solid var(--color-error-200, #fecaca)',
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--space-2)'
        }}>
          <AlertCircle size={16} />
          <span>{saveError}</span>
        </div>
      )}

      {/* 1. Rule Section & Basic Details Card */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">Rule Section & Metadata</div>
            <div className="card-subtitle">Select the rule responsibility section and configure metadata</div>
          </div>
        </CardHeader>
        <CardBody>
          <div className="form-grid-2">
            <SelectField
              label="Rule Type / Section"
              options={RULE_SECTIONS.map((s) => ({ value: s.id, label: s.label }))}
              value={ruleType}
              onChange={(e) => setRuleType(e.target.value as RuleType)}
              helper="Each section has strictly isolated responsibility."
            />

            <SelectField
              label="Rule Status"
              options={[
                { value: 'active', label: 'Active (Evaluates during ingestion)' },
                { value: 'draft', label: 'Draft (Disabled)' },
                { value: 'inactive', label: 'Inactive (Archived)' },
              ]}
              value={status}
              onChange={(e) => setStatus(e.target.value as RuleStatus)}
            />

            <Input
              label="Rule Name"
              required
              placeholder={`e.g. ${ruleType === 'decision' ? 'Verify Document Title & Tax ID' : ruleType === 'folder' ? 'Classify Financial Invoices' : ruleType === 'sorting' ? 'High Urgency Escalation' : ruleType === 'routing' ? 'Assign To Specialist' : 'Customer Approval Notice'}`}
              value={name}
              onChange={(e) => setName(e.target.value)}
            />

            <Input
              label="Evaluation Order"
              type="number"
              min="1"
              max="999"
              value={evalOrder}
              onChange={(e) => setEvalOrder(e.target.value)}
              helper="Determines sequential evaluation order among rules in THIS section."
            />

            <div style={{ gridColumn: '1 / -1' }}>
              <Textarea
                label="Description"
                placeholder="Explain the purpose of this rule…"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={2}
              />
            </div>
          </div>
        </CardBody>
      </Card>

      {/* 2. Generic Conditions Card */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">Rule Conditions</div>
            <div className="card-subtitle">Define criteria evaluated on document content, OCR data, title, or metadata</div>
          </div>
          <Button variant="secondary" size="sm" onClick={addConditionGroup}>
            <Plus size={14} /> Add Condition Group
          </Button>
        </CardHeader>
        <CardBody>
          {conditionGroups.map((group, gi) => (
            <div key={group.id} style={{
              border: '1px solid var(--border-primary)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-4)',
              marginBottom: gi < conditionGroups.length - 1 ? 'var(--space-3)' : 0,
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--space-3)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                  <span style={{ fontSize: 'var(--text-caption)', color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: 'var(--tracking-widest)' }}>
                    {gi === 0 ? 'WHEN' : 'OR WHEN'}
                  </span>
                  <button
                    type="button"
                    onClick={() => toggleGroupLogic(group.id)}
                    style={{
                      padding: '2px 8px',
                      borderRadius: 'var(--radius-sm)',
                      backgroundColor: 'var(--color-brand-50)',
                      color: 'var(--color-brand-700)',
                      fontSize: 'var(--text-label)',
                      fontWeight: 600,
                      cursor: 'pointer',
                      border: '1px solid var(--color-brand-200)',
                    }}
                  >
                    {group.logic}
                  </button>
                </div>
                {conditionGroups.length > 1 && (
                  <Button variant="ghost" icon size="sm" onClick={() => removeConditionGroup(group.id)} aria-label="Remove group">
                    <Trash2 size={14} />
                  </Button>
                )}
              </div>

              {group.conditions.map((cond) => (
                <div key={cond.id} className="rule-condition-row" style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-2)' }}>
                  <div style={{ flex: '1 1 30%' }}>
                    <SelectField
                      options={CONDITION_FIELDS}
                      value={cond.field}
                      onChange={(e) => updateCondition(group.id, cond.id, 'field', e.target.value)}
                    />
                  </div>
                  <div style={{ flex: '1 1 25%' }}>
                    <SelectField
                      options={CONDITION_OPERATORS}
                      value={cond.operator}
                      onChange={(e) => updateCondition(group.id, cond.id, 'operator', e.target.value)}
                    />
                  </div>
                  <div style={{ flex: '1 1 40%' }}>
                    <Input
                      placeholder={
                        cond.field === 'extracted.text' ? 'e.g. Account Number, Loan Agreement, Tax ID' :
                        cond.field === 'document.name' ? 'e.g. Master_Agreement.pdf' :
                        cond.field === 'sender.email' ? 'e.g. @partner.org' : 'Enter expected value'
                      }
                      value={cond.value}
                      onChange={(e) => updateCondition(group.id, cond.id, 'value', e.target.value)}
                    />
                  </div>
                  <div style={{ width: 36, display: 'flex', alignItems: 'center' }}>
                    <Button
                      variant="ghost"
                      icon
                      size="sm"
                      onClick={() => removeCondition(group.id, cond.id)}
                      disabled={group.conditions.length <= 1}
                      aria-label="Remove condition"
                    >
                      <Trash2 size={14} />
                    </Button>
                  </div>
                </div>
              ))}

              <Button variant="ghost" size="sm" onClick={() => addCondition(group.id)} style={{ marginTop: 'var(--space-2)' }}>
                <Plus size={14} /> Add Condition
              </Button>
            </div>
          ))}
        </CardBody>
      </Card>

      {/* 3. Section-Specific Isolated Outcome Card */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">
              {ruleType === 'decision' && 'Decision Outcome'}
              {ruleType === 'folder' && 'Folder & Classification Assignment'}
              {ruleType === 'sorting' && 'Document Sorting Priority'}
              {ruleType === 'routing' && 'Routing Destination'}
              {ruleType === 'communication' && 'Automated Email & Notification Configuration'}
            </div>
            <div className="card-subtitle">
              {ruleType === 'decision' && 'Configure the document approval state applied when conditions pass'}
              {ruleType === 'folder' && 'Define the virtual folder / category where matching documents are grouped'}
              {ruleType === 'sorting' && 'Set the priority used to sort documents inside folders and queues'}
              {ruleType === 'routing' && 'Select the destination user, team, department, or queue'}
              {ruleType === 'communication' && 'Set up dynamic email templates sent on trigger events'}
            </div>
          </div>
        </CardHeader>
        <CardBody>
          {/* DECISION SECTION */}
          {ruleType === 'decision' && (
            <div style={{ maxWidth: 540 }}>
              <SelectField
                label="Target Decision Outcome"
                options={DECISION_OUTCOME_OPTIONS}
                value={decisionOutcome}
                onChange={(e) => setDecisionOutcome(e.target.value)}
                helper="Decision rules ONLY set Approve, Reject, or Manual Review."
              />
              <div style={{ marginTop: 'var(--space-3)', padding: 'var(--space-3)', backgroundColor: 'var(--color-gray-50)', borderRadius: 'var(--radius-md)', fontSize: 'var(--text-body-sm)' }}>
                {decisionOutcome === 'approved' && <span className="text-success-700 font-medium">✓ Document will be marked APPROVED and audit logged.</span>}
                {decisionOutcome === 'rejected' && <span className="text-error-700 font-medium">✗ Document will be marked REJECTED with failed conditions recorded.</span>}
                {decisionOutcome === 'review' && <span className="text-warning-700 font-medium">⚠ Document will be flagged for MANUAL REVIEW with reason details.</span>}
              </div>
            </div>
          )}

          {/* FOLDER SECTION */}
          {ruleType === 'folder' && (
            <div style={{ maxWidth: 540 }}>
              <Input
                label="Virtual Folder / Category Name"
                required
                placeholder="e.g. Invoices, Service Agreements, Complaints, KYC Verification"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                helper="Matching documents will be automatically segregated into this virtual category/folder."
              />
            </div>
          )}

          {/* SORTING SECTION */}
          {ruleType === 'sorting' && (
            <div style={{ maxWidth: 540 }}>
              <SelectField
                label="Document Sorting Priority"
                options={SORTING_PRIORITY_OPTIONS}
                value={sortingPriority}
                onChange={(e) => setSortingPriority(e.target.value)}
                helper="Controls document priority in queue views. Completely separate from rule evaluation order."
              />
            </div>
          )}

          {/* ROUTING SECTION */}
          {ruleType === 'routing' && (
            <div className="form-grid-2">
              <SelectField
                label="Destination Type"
                options={ROUTING_DESTINATION_TYPES}
                value={routingType}
                onChange={(e) => {
                  setRoutingType(e.target.value);
                  setRoutingValue('');
                }}
              />

              {routingType === 'assign_user' ? (
                <SelectField
                  label="Select User"
                  options={[
                    { value: '', label: 'Select a user…' },
                    ...(users || []).map((u) => ({ value: u.id, label: `${u.name} (${u.email})` })),
                  ]}
                  value={routingValue}
                  onChange={(e) => setRoutingValue(e.target.value)}
                />
              ) : routingType === 'start_workflow' ? (
                <SelectField
                  label="Select Workflow"
                  options={[
                    { value: '', label: 'Select a workflow…' },
                    ...(workflows || []).map((w) => ({ value: w.id, label: w.name })),
                  ]}
                  value={routingValue}
                  onChange={(e) => setRoutingValue(e.target.value)}
                />
              ) : (
                <Input
                  label="Destination Identifier / Name"
                  required
                  placeholder={
                    routingType === 'assign_department' ? 'e.g. Operations, Legal, Accounts' :
                    routingType === 'assign_team' ? 'e.g. Intake Team, Review Squad' : 'e.g. Urgent Processing Queue'
                  }
                  value={routingValue}
                  onChange={(e) => setRoutingValue(e.target.value)}
                />
              )}
            </div>
          )}

          {/* COMMUNICATION SECTION */}
          {ruleType === 'communication' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
              <div className="form-grid-2">
                <SelectField
                  label="Recipient Target"
                  options={EMAIL_RECIPIENT_OPTIONS}
                  value={emailRecipientType}
                  onChange={(e) => setEmailRecipientType(e.target.value as EmailRecipientType)}
                />

                {emailRecipientType === 'custom' && (
                  <Input
                    label="Specific Email Address"
                    required
                    placeholder="e.g. alerts@company.org"
                    value={customRecipient}
                    onChange={(e) => setCustomRecipient(e.target.value)}
                  />
                )}
              </div>

              <Input
                label="Email Subject Template"
                required
                value={emailSubject}
                onChange={(e) => setEmailSubject(e.target.value)}
              />

              <Textarea
                label="Email Message Body Template"
                required
                rows={6}
                value={emailMessage}
                onChange={(e) => setEmailMessage(e.target.value)}
              />

              {/* Dynamic Variables Helper Chips */}
              <div style={{
                backgroundColor: 'var(--color-gray-50)',
                padding: 'var(--space-3) var(--space-4)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-secondary)',
              }}>
                <div style={{ fontSize: 'var(--text-caption)', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
                  Available Dynamic Template Variables (Click to insert):
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {[
                    '{{document_name}}',
                    '{{document_id}}',
                    '{{decision}}',
                    '{{missing_fields}}',
                    '{{failed_conditions}}',
                    '{{review_reason}}',
                    '{{priority}}',
                    '{{category}}',
                    '{{folder}}',
                    '{{assigned_user}}',
                    '{{department}}',
                  ].map((chip) => (
                    <button
                      key={chip}
                      type="button"
                      onClick={() => setEmailMessage((prev) => `${prev} ${chip}`)}
                      style={{
                        padding: '2px 8px',
                        backgroundColor: 'var(--color-white, #fff)',
                        border: '1px solid var(--border-primary)',
                        borderRadius: 'var(--radius-sm)',
                        fontFamily: 'var(--font-mono)',
                        fontSize: '11px',
                        cursor: 'pointer',
                        color: 'var(--color-brand-700)',
                      }}
                    >
                      {chip}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}
        </CardBody>
      </Card>

      {/* 4. Rule Simulation / Test Card */}
      <Card>
        <CardHeader>
          <div>
            <div className="card-title">Simulate & Test Rule</div>
            <div className="card-subtitle">Test this rule against sample text or document content without side effects</div>
          </div>
          <Button variant="secondary" size="sm" onClick={handleTestRule} disabled={testLoading}>
            <Play size={14} /> {testLoading ? 'Evaluating…' : 'Run Simulation'}
          </Button>
        </CardHeader>
        <CardBody>
          <Textarea
            label="Sample Document Extracted Text"
            placeholder="Paste sample OCR / extracted document text here to test condition matching…"
            value={testDocumentText}
            onChange={(e) => setTestDocumentText(e.target.value)}
            rows={3}
          />

          {testResult && (
            <div style={{
              marginTop: 'var(--space-3)',
              padding: 'var(--space-3) var(--space-4)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-primary)',
              backgroundColor: testResult.matched ? 'var(--color-success-50, #f0fdf4)' : 'var(--color-error-50, #fef2f2)',
            }}>
              <div style={{ fontWeight: 600, marginBottom: 'var(--space-1)', display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                {testResult.matched ? (
                  <>
                    <CheckCircle2 size={16} className="text-success-600" />
                    <span>Rule Matched Successfully</span>
                  </>
                ) : (
                  <>
                    <AlertCircle size={16} className="text-error-600" />
                    <span>Rule Did Not Match</span>
                  </>
                )}
              </div>
              {testResult.logs && testResult.logs.length > 0 && (
                <ul style={{ margin: 0, paddingLeft: 'var(--space-4)', fontSize: 'var(--text-caption)' }}>
                  {testResult.logs.map((log: string, idx: number) => (
                    <li key={idx}>{log}</li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
