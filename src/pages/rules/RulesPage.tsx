// ===================================================================
// E2EDocs — Legacy /rules Redirect
// ===================================================================
import { Navigate } from 'react-router-dom';

export default function RulesPage() {
  return <Navigate to="/decision-rules" replace />;
}
