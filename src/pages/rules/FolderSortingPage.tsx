// ===================================================================
// E2EDocs — First-Class Feature Page: Folder Sorting
// ===================================================================

import { ArrowUpDown } from 'lucide-react';
import { RuleFeaturePage } from './RuleFeaturePage';

export default function FolderSortingPage() {
  return (
    <RuleFeaturePage
      ruleType="sorting"
      title="Folder Sorting"
      description="Manage rules that determine how documents are prioritized (CRITICAL, HIGH, MEDIUM, LOW) inside folders, categories, queues, or processing views."
      badgeLabel="SORTING RULE"
      createButtonLabel="Add Sorting Rule"
      icon={<ArrowUpDown size={24} style={{ color: '#d97706' }} />}
      accentColor="#d97706"
      accentBg="rgba(217, 119, 6, 0.08)"
    />
  );
}
