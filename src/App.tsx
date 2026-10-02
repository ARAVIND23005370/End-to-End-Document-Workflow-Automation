// ===========================
// E2EDocs — App Entry
// ===========================

import { RouterProvider } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { router } from './routes';
import './styles/global.css';
import './styles/components.css';
import './styles/layout.css';

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
