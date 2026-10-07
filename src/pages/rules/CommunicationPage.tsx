// ===================================================================
// E2EDocs — Redirect to Decision Automation (Communication is a Decision Action)
// ===================================================================
import { Navigate } from 'react-router-dom';

export default function CommunicationPage() {
  return <Navigate to="/decision-rules" replace />;
}
