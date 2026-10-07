// ===================================================================
// E2EDocs — Redirect to Decision Automation (Routing is a Decision Action)
// ===================================================================
import { Navigate } from 'react-router-dom';

export default function RoutingPage() {
  return <Navigate to="/decision-rules" replace />;
}
