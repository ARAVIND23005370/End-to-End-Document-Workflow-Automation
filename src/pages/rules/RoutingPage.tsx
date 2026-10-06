// ===================================================================
// E2EDocs — First-Class Feature Page: Routing & Assignment
// ===================================================================

import { SendHorizontal } from 'lucide-react';
import { RuleFeaturePage } from './RuleFeaturePage';

export default function RoutingPage() {
  return (
    <RuleFeaturePage
      ruleType="routing"
      title="Routing & Assignment"
      description="Manage rules that determine where documents should be assigned or routed (User, Team, Department, Queue, or Workflow)."
      badgeLabel="ROUTING RULE"
      createButtonLabel="Add Routing Rule"
      icon={<SendHorizontal size={24} style={{ color: '#0284c7' }} />}
      accentColor="#0284c7"
      accentBg="rgba(2, 132, 199, 0.08)"
    />
  );
}
