// ===========================
// E2EDocs — Rule Builder Page
// ===========================

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2, GripVertical, Mail } from 'lucide-react';
import {
  Button, Card, CardHeader, CardBody, Input, Textarea, SelectField, Breadcrumb,
} from '../../components/ui';
import { useDocumentTitle, useAsync } from '../../hooks';
import { ruleService, organizationService, userService, workflowService } from '../../services/api';
import {
  CONDITION_OPERATORS,
  CONDITION_FIELDS,
  ACTION_TYPES,
  EMAIL_RECIPIENT_OPTIONS,
  PRIORITY_LABELS,
} from '../../constants';
import { generateId } from '../../utils';
import type {
  ConditionGroup,
  RuleCondition,
  RuleAction,
  ConditionLogic,
  RuleStatus,
  EmailRecipientType,
} from '../../types';

const DEFAULT_CONDITION: () => RuleCondition = () => ({
  id: generateId('c'),
  field: 'sender.email',
  operator: 'equals',
  value: '',
});

const DEFAULT_GROUP: () => ConditionGroup = () => ({
  id: generateId('cg'),
  logic: 'AND',
  conditions: [DEFAULT_CONDITION()],
});

const DEFAULT_ACTION: () => RuleAction = () => ({
  id: generateId('act'),
  type: 'set_priority',
  value: 'high',
  emailConfig: {
    recipientType: 'original_sender',
    subject: 'Update regarding your document {{document.name}}',
    message: 'Your document has been processed according to configured workflow rules.',
  },
});

export default function RuleBuilderPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isNew = !id || id === 'new';

  const { data: existingRule } = useAsync(
    () => (isNew ? Promise.resolve(null) : ruleService.getById(id!)),
    [id]
  );

  // Fetch users and workflows for rule actions
  const { data: users = [] } = useAsync(() => userService.getAll(), []);
  const { data: workflows = [] } = useAsync(() => workflowService.getAll(), []);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<RuleStatus>('draft');
  const [evalOrder, setEvalOrder] = useState('10');
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([DEFAULT_GROUP()]);
  const [actions, setActions] = useState<RuleAction[]>([DEFAULT_ACTION()]);
  const [saveLoading, setSaveLoading] = useState(false);
  const [saveError, setSaveError] = useState('');

  // Sync existing rule when loaded
  useEffect(() => {
    if (existingRule) {
      setName(existingRule.name);
      setDescription(existingRule.description);
      setStatus(existingRule.status);
      setEvalOrder(existingRule.evaluationOrder?.toString() || '10');
      if (existingRule.conditionGroups && existingRule.conditionGroups.length > 0) {
        setConditionGroups(existingRule.conditionGroups);
      }
      if (existingRule.actions && existingRule.actions.length > 0) {
        setActions(existingRule.actions);
      }
    }
  }, [existingRule]);

  useDocumentTitle(isNew ? 'Create Rule' : `Edit Rule — ${name || 'Untitled'}`);

  // --- Condition Group handlers ---
  const addConditionGroup = () => setConditionGroups([...conditionGroups, DEFAULT_GROUP()]);

  const removeConditionGroup = (groupId: string) => {
    if (conditionGroups.length <= 1) return;
    setConditionGroups(conditionGroups.filter((g) => g.id !== groupId));
  };

  const toggleGroupLogic = (groupId: string) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId ? { ...g, logic: g.logic === 'AND' ? 'OR' as ConditionLogic : 'AND' as ConditionLogic } : g
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

  const updateCondition = (groupId: string, condId: string, field: keyof RuleCondition, value: string) => {
    setConditionGroups(conditionGroups.map((g) =>
      g.id === groupId ? {
        ...g,
        conditions: g.conditions.map((c) =>
          c.id === condId ? { ...c, [field]: value } : c
        ),
      } : g
    ));
  };

  // --- Action handlers ---
  const addAction = () => setActions([...actions, DEFAULT_ACTION()]);

  const removeAction = (actId: string) => {
    if (actions.length <= 1) return;
    setActions(actions.filter((a) => a.id !== actId));
  };

  const updateAction = (actId: string, field: keyof RuleAction, value: string) => {
    setActions(actions.map((a) => a.id === actId ? { ...a, [field]: value } : a));
  };

  const updateEmailConfig = (actId: string, updates: Partial<NonNullable<RuleAction['emailConfig']>>) => {
    setActions(actions.map((a) => {
      if (a.id !== actId) return a;
      return {
        ...a,
        emailConfig: {
          recipientType: a.emailConfig?.recipientType || 'original_sender',
          subject: a.emailConfig?.subject || '',
          message: a.emailConfig?.message || '',
          ...updates,
        },
      };
    }));
  };

  const getActionValueOptions = (type: string): { options: { value: string; label: string }[]; placeholder: string } | null => {
    const userList = users || [];
    const workflowList = workflows || [];

    switch (type) {
      case 'set_priority':
        return {
          options: Object.entries(PRIORITY_LABELS).map(([v, l]) => ({ value: v, label: l })),
          placeholder: 'Select priority',
        };
      case 'assign_user':
        return {
          options: userList.map((u) => ({ value: u.name, label: `${u.name} (${u.role})` })),
          placeholder: userList.length > 0 ? 'Select user' : 'No users configured',
        };
      case 'start_workflow':
        return {
          options: workflowList.map((w) => ({ value: w.name, label: w.name })),
          placeholder: workflowList.length > 0 ? 'Select workflow' : 'No workflows configured',
        };
      case 'set_decision':
        return {
          options: [
            { value: 'approve', label: 'Approve Document' },
            { value: 'reject', label: 'Reject Document' },
            { value: 'review', label: 'Send to Review Queue' },
          ],
          placeholder: 'Select decision',
        };
      case 'send_email':
        return null; // Specialized UI rendered separately
      default:
        return null;
    }
  };

  const handleSave = async () => {
    if (!name.trim()) {
      setSaveError('Rule name is required');
      return;
    }

    setSaveLoading(true);
    setSaveError('');

    try {
      const payload = {
        name: name.trim(),
        description: description.trim(),
        status,
        evaluationOrder: parseInt(evalOrder, 10) || 1,
        conditionGroups,
        actions,
      };

      if (isNew) {
        await ruleService.create(payload as any);
      } else {
        await ruleService.update(id!, payload as any);
      }
      navigate('/rules');
    } catch (err: any) {
      setSaveError(err?.message || 'Failed to save rule. Please try again.');
    } finally {
      setSaveLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <Breadcrumb items={[{ label: 'Rules', href: '/rules' }, { label: isNew ? 'Create Rule' : name || 'Edit Rule' }]} />
        <div className="page-header-row" style={{ marginTop: 'var(--space-3)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <Button variant="ghost" icon size="sm" onClick={() => navigate('/rules')} aria-label="Back">
              <ArrowLeft size={16} />
            </Button>
            <h1 className="page-title" style={{ fontSize: 'var(--text-h3)' }}>
              {isNew ? 'Create Rule' : 'Edit Rule'}
            </h1>
          </div>
          <div className="page-actions">
            <Button variant="secondary" onClick={() => navigate('/rules')} disabled={saveLoading}>Cancel</Button>
            <Button variant="primary" onClick={handleSave} disabled={saveLoading}>
              {saveLoading ? 'Saving…' : 'Save Rule'}
            </Button>
          </div>
        </div>
      </div>

      {saveError && (
        <div style={{
          marginBottom: 'var(--space-4)', padding: '12px 16px',
          backgroundColor: 'var(--color-error-50, #fef2f2)', border: '1px solid var(--color-error-200, #fecaca)',
          borderRadius: 'var(--radius-md, 6px)', color: 'var(--color-error-700, #b91c1c)', fontSize: 'var(--text-body-sm)'
        }}>
          {saveError}
        </div>
      )}

      {/* Rule Information */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader><div className="card-title">Rule Information</div></CardHeader>
        <CardBody>
          <div className="form-grid-2">
            <Input
              label="Rule Name"
              placeholder="e.g., External Ingestion Auto-Routing"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <div className="form-grid-2">
              <SelectField
                label="Status"
                options={[
                  { value: 'draft', label: 'Draft' },
                  { value: 'active', label: 'Active' },
                  { value: 'inactive', label: 'Inactive' },
                ]}
                value={status}
                onChange={(e) => setStatus(e.target.value as RuleStatus)}
              />
              <Input
                label="Evaluation Order"
                type="number"
                value={evalOrder}
                onChange={(e) => setEvalOrder(e.target.value)}
                helper="Lower numbers evaluated first"
              />
            </div>
          </div>
          <div style={{ marginTop: 'var(--space-4)' }}>
            <Textarea
              label="Description"
              placeholder="Describe what this rule does…"
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
        </CardBody>
      </Card>

      {/* Conditions */}
      <Card style={{ marginBottom: 'var(--space-4)' }}>
        <CardHeader>
          <div>
            <div className="card-title">Conditions</div>
            <div className="card-subtitle">Define when this rule should trigger across generic document fields and metadata</div>
          </div>
          <Button variant="secondary" size="sm" onClick={addConditionGroup}>
            <Plus size={14} /> Add Group
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
                      padding: '2px 8px', borderRadius: 'var(--radius-sm)',
                      backgroundColor: 'var(--color-brand-50)', color: 'var(--color-brand-700)',
                      fontSize: 'var(--text-label)', fontWeight: 'var(--weight-semibold)',
                      cursor: 'pointer', border: '1px solid var(--color-brand-200)',
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
                <div key={cond.id} className="rule-condition-row">
                  <div className="rule-condition-field">
                    <SelectField
                      options={CONDITION_FIELDS}
                      value={cond.field}
                      onChange={(e) => updateCondition(group.id, cond.id, 'field', e.target.value)}
                      aria-label="Condition Field"
                    />
                  </div>
                  <div className="rule-condition-operator">
                    <SelectField
                      options={CONDITION_OPERATORS}
                      value={cond.operator}
                      onChange={(e) => updateCondition(group.id, cond.id, 'operator', e.target.value)}
                      aria-label="Condition Operator"
                    />
                  </div>
                  <div className="rule-condition-value">
                    <Input
                      placeholder={
                        cond.field === 'sender.email' ? 'e.g. @example.org or partner@domain.com' :
                        cond.field === 'document.description' ? 'e.g. contains urgent' :
                        cond.field === 'metadata.source' ? 'e.g. email, manual_upload, api' : 'Enter value'
                      }
                      value={cond.value}
                      onChange={(e) => updateCondition(group.id, cond.id, 'value', e.target.value)}
                      aria-label="Condition Value"
                    />
                  </div>
                  <div className="rule-condition-delete">
                    <Button variant="ghost" icon size="sm" onClick={() => removeCondition(group.id, cond.id)} disabled={group.conditions.length <= 1} aria-label="Remove condition">
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

      {/* Actions */}
      <Card>
        <CardHeader>
          <div>
            <div className="card-title">Actions</div>
            <div className="card-subtitle">Define configurable actions executed when rule criteria are met</div>
          </div>
          <Button variant="secondary" size="sm" onClick={addAction}>
            <Plus size={14} /> Add Action
          </Button>
        </CardHeader>
        <CardBody>
          {actions.map((action, ai) => {
            const selectConfig = getActionValueOptions(action.type);
            const isEmailAction = action.type === 'send_email';

            return (
              <div
                key={action.id}
                style={{
                  border: isEmailAction ? '1px solid var(--border-primary)' : undefined,
                  borderRadius: isEmailAction ? 'var(--radius-md)' : undefined,
                  padding: isEmailAction ? 'var(--space-3)' : undefined,
                  marginBottom: ai < actions.length - 1 ? 'var(--space-3)' : 0,
                  backgroundColor: isEmailAction ? 'var(--color-gray-50)' : 'transparent',
                }}
              >
                <div className="rule-action-row">
                  <div style={{ width: 28, display: 'flex', alignItems: 'center', justifyContent: 'center', paddingTop: 8, color: 'var(--text-tertiary)' }}>
                    <GripVertical size={14} />
                  </div>
                  <div style={{ flex: '1 1 35%', minWidth: 0 }}>
                    <SelectField
                      options={ACTION_TYPES}
                      value={action.type}
                      onChange={(e) => updateAction(action.id, 'type', e.target.value)}
                      aria-label="Action type"
                    />
                  </div>
                  {!isEmailAction && (
                    <div style={{ flex: '1 1 65%', minWidth: 0 }}>
                      {selectConfig ? (
                        <SelectField
                          options={selectConfig.options}
                          placeholder={selectConfig.placeholder}
                          value={action.value}
                          onChange={(e) => updateAction(action.id, 'value', e.target.value)}
                          aria-label="Action value"
                        />
                      ) : (
                        <Input
                          placeholder="Action value"
                          value={action.value}
                          onChange={(e) => updateAction(action.id, 'value', e.target.value)}
                          aria-label="Action value"
                        />
                      )}
                    </div>
                  )}
                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Button variant="ghost" icon size="sm" onClick={() => removeAction(action.id)} disabled={actions.length <= 1} aria-label="Remove action">
                      <Trash2 size={14} />
                    </Button>
                  </div>
                </div>

                {/* Specialized Email Action Configuration */}
                {isEmailAction && (
                  <div style={{
                    marginTop: 'var(--space-3)',
                    paddingTop: 'var(--space-3)',
                    borderTop: '1px dashed var(--border-secondary)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 'var(--space-3)',
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--text-secondary)', fontSize: 'var(--text-caption)' }}>
                      <Mail size={14} />
                      <span style={{ fontWeight: 600 }}>Email Notification Configuration (Simulated Frontend Action)</span>
                    </div>

                    <div className="form-grid-2">
                      <SelectField
                        label="Recipient Target"
                        options={EMAIL_RECIPIENT_OPTIONS}
                        value={action.emailConfig?.recipientType || 'original_sender'}
                        onChange={(e) => updateEmailConfig(action.id, { recipientType: e.target.value as EmailRecipientType })}
                      />
                      {action.emailConfig?.recipientType === 'custom' ? (
                        <Input
                          label="Custom Email Address"
                          placeholder="e.g. alerts@example.org"
                          value={action.emailConfig?.customRecipient || ''}
                          onChange={(e) => updateEmailConfig(action.id, { customRecipient: e.target.value })}
                        />
                      ) : (
                        <Input
                          label="Target Description"
                          disabled
                          value={
                            action.emailConfig?.recipientType === 'original_sender' ? 'Document Original Sender' :
                            action.emailConfig?.recipientType === 'assigned_user' ? 'Assigned User on Document' :
                            'Configured Department Notification Channel'
                          }
                        />
                      )}
                    </div>

                    <Input
                      label="Subject Template"
                      placeholder="e.g., Receipt confirmation for {{document.name}}"
                      value={action.emailConfig?.subject || ''}
                      onChange={(e) => updateEmailConfig(action.id, { subject: e.target.value })}
                    />

                    <Textarea
                      label="Message Template"
                      placeholder="Enter email message body template. Supports {{document.name}}, {{document.type}}, {{document.status}} variables..."
                      rows={2}
                      value={action.emailConfig?.message || ''}
                      onChange={(e) => updateEmailConfig(action.id, { message: e.target.value })}
                    />
                  </div>
                )}
              </div>
            );
          })}
        </CardBody>
      </Card>
    </div>
  );
}
