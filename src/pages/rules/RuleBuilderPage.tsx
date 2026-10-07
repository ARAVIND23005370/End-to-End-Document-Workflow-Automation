// ===========================
// E2EDocs — Unified Rule Builder Page (Decision, Folder, Sorting)
// ===========================

import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
  ArrowLeft, Plus, Trash2, CheckCircle2, FolderTree, ArrowUpDown,
  Sparkles, Play, AlertCircle, SendHorizontal, Mail, Users, Check,
  Send, Layers, HelpCircle
} from 'lucide-react';
import {
  Button, Card, CardHeader, CardBody, Input, Textarea, SelectField, Breadcrumb, Badge,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService, userService, workflowService, organizationService } from '../../services/api';
import {
  CONDITION_OPERATORS,
  CONDITION_FIELDS,
  DECISION_OUTCOME_OPTIONS,
  SORTING_PRIORITY_OPTIONS,
  ROUTING_DESTINATION_TYPES,
  EMAIL_RECIPIENT_OPTIONS,
  TEMPLATE_VARIABLE_CHIPS,
} from '../../constants';
import { generateId } from '../../utils';
import type {
  ConditionGroup,
  RuleCondition,
  RuleAction,
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
    default: return '/decision-rules';
  }
};

const getFeatureTitle = (type: RuleType) => {
  switch (type) {
    case 'decision': return 'Decision Automation';
    case 'folder': return 'Folder Classification';
    case 'sorting': return 'Folder Sorting';
    default: return 'Decision Automation';
  }
};

export default function RuleBuilderPage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const isNew = !id || id === 'new';

  const initialTypeFromQuery = (searchParams.get('type') as RuleType) || 'decision';
  // Standardize legacy routing/communication links to decision
  const initialType: RuleType = (initialTypeFromQuery === 'routing' || initialTypeFromQuery === 'communication') 
    ? 'decision' 
    : initialTypeFromQuery;

  const { data: existingRule } = useAsync(
    () => (isNew ? Promise.resolve(null) : ruleService.getById(id!)),
    [id]
  );

  const { data: users = [] } = useAsync(() => userService.getAll(), []);
  const { data: workflows = [] } = useAsync(() => workflowService.getAll(), []);
  const { data: departments = [] } = useAsync(() => organizationService.getDepartments(), []);

  const [ruleType, setRuleType] = useState<RuleType>(initialType);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<RuleStatus>('active');
  const [evalOrder, setEvalOrder] = useState('1');
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([DEFAULT_GROUP()]);
  
  // Decision outcome state
  const [decisionOutcome, setDecisionOutcome] = useState<'approved' | 'rejected' | 'review'>('approved');
  
  // Optional Routing Action attached to Decision
  const [enableRouting, setEnableRouting] = useState(false);
  const [routingType, setRoutingType] = useState('assign_user');
  const [routingValue, setRoutingValue] = useState('');

  // Optional Communication Action attached to Decision
  const [enableCommunication, setEnableCommunication] = useState(false);
  const [emailRecipientType, setEmailRecipientType] = useState<EmailRecipientType>('original_sender');
  const [customRecipient, setCustomRecipient] = useState('');
  const [emailSubject, setEmailSubject] = useState('Update regarding your document: {{document_name}}');
  const [emailMessage, setEmailMessage] = useState(
    'Hello,\n\nYour document (ID: {{document_id}}) has been processed.\nStatus: {{decision}}\n\nDetails: {{review_reason}}\n\nBest regards,\nE2EDocs Platform'
  );

  // Folder classification state
  const [folderName, setFolderName] = useState('');

  // Folder sorting state
  const [sortingPriority, setSortingPriority] = useState('high');

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
      const effectiveType = (existingRule.ruleType === 'routing' || existingRule.ruleType === 'communication')
        ? 'decision'
        : (existingRule.ruleType || 'decision');
      setRuleType(effectiveType);
      setEvalOrder(existingRule.evaluationOrder?.toString() || '1');
      if (existingRule.conditionGroups && existingRule.conditionGroups.length > 0) {
        setConditionGroups(existingRule.conditionGroups);
      }

      // Map actions back into states
      if (existingRule.actions && existingRule.actions.length > 0) {
        for (const act of existingRule.actions) {
          if (act.type === 'set_decision') {
            setDecisionOutcome((act.value as any) || 'approved');
          } else if (act.type === 'assign_folder') {
            setFolderName(act.value || '');
          } else if (act.type === 'set_priority') {
            setSortingPriority(act.value || 'high');
          } else if (act.type === 'assign_user' || act.type === 'assign_department' || act.type === 'assign_team' || act.type === 'assign_queue' || act.type === 'start_workflow') {
            setEnableRouting(true);
            setRoutingType(act.type);
            setRoutingValue(act.value || '');
          } else if (act.type === 'send_email') {
            setEnableCommunication(true);
            if (act.emailConfig) {
              setEmailRecipientType(act.emailConfig.recipientType || 'original_sender');
              setCustomRecipient(act.emailConfig.customRecipient || '');
              setEmailSubject(act.emailConfig.subject || '');
              setEmailMessage(act.emailConfig.message || '');
            }
          }
        }
      }
    }
  }, [existingRule]);

  useDocumentTitle(isNew ? `Create ${getFeatureTitle(ruleType)} Rule` : `Edit Rule — ${name || 'Untitled'}`);

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

  // Compile actions array
  const buildActionsForCurrentSection = (): RuleAction[] => {
    if (ruleType === 'decision') {
      const actions: RuleAction[] = [
        {
          id: generateId('act_dec'),
          type: 'set_decision',
          value: decisionOutcome,
        }
      ];

      // Attached optional routing action
      if (enableRouting && routingValue.trim()) {
        actions.push({
          id: generateId('act_route'),
          type: routingType as ActionType,
          value: routingValue.trim(),
        });
      }

      // Attached optional communication action
      if (enableCommunication && emailSubject.trim()) {
        actions.push({
          id: generateId('act_comm'),
          type: 'send_email',
          value: emailRecipientType,
          emailConfig: {
            recipientType: emailRecipientType,
            customRecipient: customRecipient.trim(),
            subject: emailSubject.trim(),
            message: emailMessage.trim(),
          },
        });
      }

      return actions;
    }

    if (ruleType === 'folder') {
      return [{
        id: generateId('act_folder'),
        type: 'assign_folder',
        value: folderName.trim(),
      }];
    }

    if (ruleType === 'sorting') {
      return [{
        id: generateId('act_sort'),
        type: 'set_priority',
        value: sortingPriority,
      }];
    }

    return [{
      id: generateId('act_default'),
      type: 'set_decision',
      value: 'approved',
    }];
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

    if (ruleType === 'decision' && enableRouting && !routingValue.trim()) {
      setSaveError('Destination value is required when routing action is enabled.');
      return;
    }

    if (ruleType === 'decision' && enableCommunication && !emailSubject.trim()) {
      setSaveError('Email Subject is required when automated communication is enabled.');
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
          'document.name': 'Sample_Invoice_Document.pdf',
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

  const featurePath = getFeaturePath(ruleType);
  const featureTitle = getFeatureTitle(ruleType);

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', paddingBottom: 'var(--space-12)' }}>
      {/* Header */}
      <div className="page-header">
        <Breadcrumb items={[
          { label: featureTitle, href: featurePath },
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
                  {isNew ? `Create ${featureTitle} Rule` : `Edit Rule — ${name || 'Untitled'}`}
                </h1>
                <Badge variant="brand">{ruleType.toUpperCase()} RULE</Badge>
              </div>
              <p className="page-description">
                {ruleType === 'decision' && 'Configure conditions to determine document verification outcome (APPROVE, REJECT, or MANUAL REVIEW) with optional routing and email actions.'}
                {ruleType === 'folder' && 'Configure conditions to classify documents into dynamic virtual folders or categories.'}
                {ruleType === 'sorting' && 'Configure conditions to set document sorting priority (CRITICAL, HIGH, MEDIUM, LOW).'}
              </p>
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

      {/* 1. Rule Information Card */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">1. Rule Information</div>
            <div className="card-subtitle">General metadata, evaluation status, and rule priority</div>
          </div>
        </CardHeader>
        <CardBody>
          <div className="form-grid-2">
            <Input
              label="Rule Name"
              required
              placeholder={ruleType === 'decision' ? 'e.g. High Value Invoice Approval' : ruleType === 'folder' ? 'e.g. Classify Tax Returns' : 'e.g. Critical Urgent Dispatch'}
              value={name}
              onChange={(e) => setName(e.target.value)}
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
          </div>

          <div style={{ marginTop: 'var(--space-3)' }}>
            <Textarea
              label="Description (Optional)"
              placeholder="Explain the business purpose and verification requirements of this rule…"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={2}
            />
          </div>

          <div style={{ marginTop: 'var(--space-3)', maxWidth: 280 }}>
            <Input
              label="Rule Evaluation Priority / Order"
              type="number"
              min="1"
              value={evalOrder}
              onChange={(e) => setEvalOrder(e.target.value)}
              helper="Lower numbers evaluate first (e.g. 1 = highest evaluation priority). Distinct from document sorting priority."
            />
          </div>
        </CardBody>
      </Card>
      {/* 2. Conditions Card */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">2. Evaluation Conditions (IF)</div>
            <div className="card-subtitle">
              Configure conditions on extracted OCR text, document metadata, sender email, or file properties
            </div>
          </div>
          <Button variant="secondary" size="sm" onClick={addConditionGroup}>
            <Plus size={14} /> Add Condition Group (OR)
          </Button>
        </CardHeader>
        <CardBody>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            {conditionGroups.map((group, groupIndex) => (
              <div
                key={group.id}
                style={{
                  border: '1px solid var(--border-primary)',
                  borderRadius: 'var(--radius-md)',
                  padding: 'var(--space-4)',
                  backgroundColor: 'var(--bg-secondary, #f8fafc)',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-3)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <span style={{ fontSize: 'var(--text-caption)', fontWeight: 700, color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>
                      Group {groupIndex + 1}
                    </span>
                    <button
                      type="button"
                      onClick={() => toggleGroupLogic(group.id)}
                      style={{
                        padding: '2px 8px',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--color-brand-300)',
                        backgroundColor: 'var(--color-brand-50)',
                        color: 'var(--color-brand-700)',
                        fontSize: '11px',
                        fontWeight: 700,
                        cursor: 'pointer',
                      }}
                      title="Click to toggle logic operator between conditions"
                    >
                      {group.logic} (Match {group.logic === 'AND' ? 'ALL' : 'ANY'})
                    </button>
                  </div>

                  {conditionGroups.length > 1 && (
                    <Button variant="ghost" size="sm" onClick={() => removeConditionGroup(group.id)} style={{ color: 'var(--color-error-600)' }}>
                      <Trash2 size={14} /> Remove Group
                    </Button>
                  )}
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  {group.conditions.map((cond, condIndex) => (
                    <div
                      key={cond.id}
                      style={{
                        display: 'grid',
                        gridTemplateColumns: 'minmax(180px, 1.5fr) minmax(140px, 1fr) minmax(180px, 2fr) 36px',
                        gap: 'var(--space-2)',
                        alignItems: 'center',
                      }}
                    >
                      <SelectField
                        value={cond.field}
                        onChange={(e) => updateCondition(group.id, cond.id, 'field', e.target.value)}
                        options={CONDITION_FIELDS}
                      />

                      <SelectField
                        value={cond.operator}
                        onChange={(e) => updateCondition(group.id, cond.id, 'operator', e.target.value)}
                        options={CONDITION_OPERATORS}
                      />

                      <Input
                        placeholder="Expected value / keyword…"
                        value={cond.value}
                        onChange={(e) => updateCondition(group.id, cond.id, 'value', e.target.value)}
                      />

                      {group.conditions.length > 1 ? (
                        <Button
                          variant="ghost"
                          icon
                          size="sm"
                          onClick={() => removeCondition(group.id, cond.id)}
                          aria-label="Remove condition"
                          style={{ color: 'var(--color-error-600)' }}
                        >
                          <Trash2 size={14} />
                        </Button>
                      ) : <div />}
                    </div>
                  ))}
                </div>

                <div style={{ marginTop: 'var(--space-3)' }}>
                  <Button variant="ghost" size="sm" onClick={() => addCondition(group.id)}>
                    <Plus size={12} /> Add Condition in Group
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardBody>
      </Card>

      {/* 3. Decision Outcome & Actions (FOR DECISION RULES) */}
      {ruleType === 'decision' && (
        <>
          {/* Decision Outcome Card */}
          <Card style={{ marginBottom: 'var(--space-4)' }}>
            <CardHeader>
              <div>
                <div className="card-title">3. Decision Outcome (THEN)</div>
                <div className="card-subtitle">Select the verification outcome when conditions are satisfied</div>
              </div>
            </CardHeader>
            <CardBody>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                {DECISION_OUTCOME_OPTIONS.map((opt) => (
                  <label
                    key={opt.value}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 'var(--space-3)',
                      padding: '12px 16px',
                      borderRadius: 'var(--radius-md)',
                      border: `1px solid ${decisionOutcome === opt.value ? 'var(--color-brand-600, #2563eb)' : 'var(--border-primary, #e2e8f0)'}`,
                      backgroundColor: decisionOutcome === opt.value ? 'var(--color-brand-50, #eff6ff)' : 'var(--bg-primary, #ffffff)',
                      cursor: 'pointer',
                      transition: 'all 0.15s ease'
                    }}
                  >
                    <input
                      type="radio"
                      name="decisionOutcome"
                      value={opt.value}
                      checked={decisionOutcome === opt.value}
                      onChange={() => setDecisionOutcome(opt.value as any)}
                      style={{ accentColor: 'var(--color-brand-600)' }}
                    />
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: 'var(--text-body)', color: 'var(--text-primary)' }}>
                        {opt.value === 'approved' && <span style={{ marginRight: 8 }}><Badge variant="success">APPROVE</Badge></span>}
                        {opt.value === 'rejected' && <span style={{ marginRight: 8 }}><Badge variant="error">REJECT</Badge></span>}
                        {opt.value === 'review' && <span style={{ marginRight: 8 }}><Badge variant="warning">MANUAL REVIEW</Badge></span>}
                        <span>{opt.label}</span>
                      </div>
                    </div>
                  </label>
                ))}
              </div>
            </CardBody>
          </Card>

          {/* Optional Routing / Assignment Action */}
          <Card style={{ marginBottom: 'var(--space-4)' }}>
            <CardHeader>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <SendHorizontal size={18} style={{ color: '#0284c7' }} />
                <div>
                  <div className="card-title">4. Optional Routing / Assignment Action</div>
                  <div className="card-subtitle">Automatically route or assign document upon this decision outcome</div>
                </div>
              </div>
              <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '13px', fontWeight: 600 }}>
                <input
                  type="checkbox"
                  checked={enableRouting}
                  onChange={(e) => setEnableRouting(e.target.checked)}
                  style={{ accentColor: 'var(--color-brand-600)' }}
                />
                <span>Enable Routing Action</span>
              </label>
            </CardHeader>
            {enableRouting && (
              <CardBody>
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
                  ) : routingType === 'assign_department' ? (
                    <SelectField
                      label="Select Department"
                      options={[
                        { value: '', label: 'Select a department…' },
                        ...(departments || []).map((d) => ({ value: d.name, label: d.name })),
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
                        routingType === 'assign_team' ? 'e.g. Intake Team, Review Squad' : 'e.g. Urgent Processing Queue'
                      }
                      value={routingValue}
                      onChange={(e) => setRoutingValue(e.target.value)}
                    />
                  )}
                </div>
              </CardBody>
            )}
          </Card>

          {/* Optional Communication / Notification Action */}
          <Card style={{ marginBottom: 'var(--space-4)' }}>
            <CardHeader>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                <Mail size={18} style={{ color: '#059669' }} />
                <div>
                  <div className="card-title">5. Optional Communication / Email Action</div>
                  <div className="card-subtitle">Send automated email or notification triggered by this decision outcome</div>
                </div>
              </div>
              <label style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', fontSize: '13px', fontWeight: 600 }}>
                <input
                  type="checkbox"
                  checked={enableCommunication}
                  onChange={(e) => setEnableCommunication(e.target.checked)}
                  style={{ accentColor: 'var(--color-brand-600)' }}
                />
                <span>Enable Automated Email</span>
              </label>
            </CardHeader>
            {enableCommunication && (
              <CardBody>
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
                    rows={5}
                    value={emailMessage}
                    onChange={(e) => setEmailMessage(e.target.value)}
                  />

                  {/* Dynamic Variables Helper Chips */}
                  <div style={{
                    backgroundColor: 'var(--color-gray-50, #f8fafc)',
                    padding: 'var(--space-3) var(--space-4)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-secondary)',
                  }}>
                    <div style={{ fontSize: 'var(--text-caption)', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
                      Dynamic Template Variables (Click to insert):
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                      {TEMPLATE_VARIABLE_CHIPS.map((chip) => (
                        <button
                          key={chip}
                          type="button"
                          onClick={() => setEmailMessage((prev) => `${prev} ${chip}`)}
                          style={{
                            padding: '2px 8px',
                            backgroundColor: 'var(--bg-elevated, #ffffff)',
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
              </CardBody>
            )}
          </Card>
        </>
      )}

      {/* 3. Folder Classification Outcome (FOR FOLDER RULES) */}
      {ruleType === 'folder' && (
        <Card style={{ marginBottom: 'var(--space-4)' }}>
          <CardHeader>
            <div>
              <div className="card-title">3. Dynamic Folder / Category Assignment</div>
              <div className="card-subtitle">Set the target folder / category when conditions match</div>
            </div>
          </CardHeader>
          <CardBody>
            <div className="form-grid-2">
              <Input
                label="Target Folder / Category Name"
                required
                placeholder="e.g. Invoices, Contracts, Receipts, Tax Filings"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                helper="Documents matching this rule will be classified into this folder/category."
              />
            </div>
          </CardBody>
        </Card>
      )}

      {/* 3. Folder Sorting Outcome (FOR SORTING RULES) */}
      {ruleType === 'sorting' && (
        <Card style={{ marginBottom: 'var(--space-4)' }}>
          <CardHeader>
            <div>
              <div className="card-title">3. Document Sorting Priority</div>
              <div className="card-subtitle">Set queue ordering priority inside folders and views</div>
            </div>
          </CardHeader>
          <CardBody>
            <div className="form-grid-2">
              <SelectField
                label="Document Sorting Priority"
                options={SORTING_PRIORITY_OPTIONS}
                value={sortingPriority}
                onChange={(e) => setSortingPriority(e.target.value)}
                helper="Controls document position in queue views (CRITICAL, HIGH, MEDIUM, LOW). Completely separate from rule evaluation priority."
              />
            </div>
          </CardBody>
        </Card>
      )}

      {/* 6. Rule Simulation / Test Card */}
      <Card>
        <CardHeader>
          <div>
            <div className="card-title">Simulate & Test Rule</div>
            <div className="card-subtitle">Test this rule against sample text or OCR document content without side effects</div>
          </div>
          <Button variant="secondary" size="sm" onClick={handleTestRule} disabled={testLoading}>
            <Play size={14} /> {testLoading ? 'Evaluating…' : 'Run Test'}
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
