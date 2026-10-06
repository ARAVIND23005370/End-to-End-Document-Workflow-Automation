// ===================================================================
// E2EDocs — First-Class Feature Page: Decision Automation
// ===================================================================

import { CheckCircle2 } from 'lucide-react';
import { RuleFeaturePage } from './RuleFeaturePage';

export default function DecisionRulesPage() {
  return (
    <RuleFeaturePage
      ruleType="decision"
      title="Decision Automation"
      description="Configure rules that determine the verification outcome of documents (APPROVE, REJECT, or MANUAL REVIEW)."
      badgeLabel="DECISION RULE"
      createButtonLabel="Add Decision Rule"
      icon={<CheckCircle2 size={24} style={{ color: 'var(--color-brand-600, #2563eb)' }} />}
      accentColor="var(--color-brand-600, #2563eb)"
      accentBg="rgba(37, 99, 235, 0.08)"
    />
  );
}
