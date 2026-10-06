// ===================================================================
// E2EDocs — First-Class Feature Page: Folder Classification
// ===================================================================

import { FolderTree } from 'lucide-react';
import { RuleFeaturePage } from './RuleFeaturePage';

export default function FolderClassificationPage() {
  return (
    <RuleFeaturePage
      ruleType="folder"
      title="Folder Classification"
      description="Manage rules that classify documents and assign them to dynamic virtual folders/categories based on extracted information."
      badgeLabel="FOLDER RULE"
      createButtonLabel="Add Folder Rule"
      icon={<FolderTree size={24} style={{ color: '#9333ea' }} />}
      accentColor="#9333ea"
      accentBg="rgba(147, 51, 234, 0.08)"
    />
  );
}
