// ===================================================================
// E2EDocs — First-Class Feature Page: Communication
// ===================================================================

import { Mail } from 'lucide-react';
import { RuleFeaturePage } from './RuleFeaturePage';

export default function CommunicationPage() {
  return (
    <RuleFeaturePage
      ruleType="communication"
      title="Communication"
      description="Manage automated email and notification rules triggered by document processing events."
      badgeLabel="COMMUNICATION RULE"
      createButtonLabel="Add Communication Rule"
      icon={<Mail size={24} style={{ color: '#059669' }} />}
      accentColor="#059669"
      accentBg="rgba(5, 150, 105, 0.08)"
    />
  );
}
