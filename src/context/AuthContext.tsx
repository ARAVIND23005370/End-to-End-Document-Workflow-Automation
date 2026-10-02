// ===========================
// E2EDocs — Auth Context
// ===========================

import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { User, AuthState } from '../types';
import { authService } from '../services/api';

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  register: (data: { name: string; email: string; password: string; organizationName: string }) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    try {
      const storedUser = localStorage.getItem('e2edocs_user');
      const storedToken = localStorage.getItem('e2edocs_token');
      if (storedUser && storedToken) {
        return {
          user: JSON.parse(storedUser),
          isAuthenticated: true,
          isLoading: false,
        };
      }
    } catch {}
    return {
      user: null,
      isAuthenticated: false,
      isLoading: false,
    };
  });

  // Verify and sync active profile on mount if token exists
  useEffect(() => {
    const token = localStorage.getItem('e2edocs_token');
    if (token) {
      authService.getCurrentUser()
        .then((user) => {
          localStorage.setItem('e2edocs_user', JSON.stringify(user));
          setState({ user, isAuthenticated: true, isLoading: false });
        })
        .catch(() => {
          // If token is invalid or expired
          localStorage.removeItem('e2edocs_token');
          localStorage.removeItem('e2edocs_user');
          setState({ user: null, isAuthenticated: false, isLoading: false });
        });
    }
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setState((s) => ({ ...s, isLoading: true }));
    try {
      const { user, token } = await authService.login(email, password);
      localStorage.setItem('e2edocs_token', token);
      localStorage.setItem('e2edocs_user', JSON.stringify(user));
      setState({ user, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      setState({ user: null, isAuthenticated: false, isLoading: false });
      throw err;
    }
  }, []);

  const register = useCallback(async (data: { name: string; email: string; password: string; organizationName: string }) => {
    setState((s) => ({ ...s, isLoading: true }));
    try {
      const { user, token } = await authService.register(data);
      localStorage.setItem('e2edocs_token', token);
      localStorage.setItem('e2edocs_user', JSON.stringify(user));
      setState({ user, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      setState({ user: null, isAuthenticated: false, isLoading: false });
      throw err;
    }
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    localStorage.removeItem('e2edocs_token');
    localStorage.removeItem('e2edocs_user');
    setState({ user: null, isAuthenticated: false, isLoading: false });
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
