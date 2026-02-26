/**
 * Authentication context and provider.
 *
 * Provides global auth state and actions to the entire React tree.
 * Replaces the GWT EventBus-driven auth flow:
 *   - LoginEvent / SessionTimedOutEvent → React context state changes
 *   - AppController.navigateToMain / callback → AuthProvider.checkSession on mount
 *
 * Source references:
 *   - src/main/java/com/example/client/AppController.java#onValueChange,navigateToMain
 *   - src/main/java/com/example/client/event/LoginEvent.java
 *   - src/main/java/com/example/client/event/SessionTimedOutEvent.java
 *   - BL-003: Session recovery via isLoggedIn / getMe
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { User } from '../types';
import * as authApi from '../api/authApi';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
}

interface AuthContextValue extends AuthState {
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkSession: () => Promise<void>;
  /** Called by the 401 interceptor or internally to clear state without API call. */
  clearAuth: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>({
    user: null,
    token: null,
    isAuthenticated: false,
    loading: true, // start loading — will call getMe() on mount
  });

  /**
   * Clear all auth state (used on logout and 401 interception).
   */
  const clearAuth = useCallback(() => {
    setState({
      user: null,
      token: null,
      isAuthenticated: false,
      loading: false,
    });
  }, []);

  /**
   * Session recovery on mount.
   * Mirrors AppController.navigateToMain → userService.isLoggedIn(sid, callback).
   * If the server returns a valid user, the session is recovered without re-login.
   * If 401, the interceptor in client.ts handles redirect.
   */
  const checkSession = useCallback(async () => {
    try {
      setState((prev) => ({ ...prev, loading: true }));
      const user = await authApi.getMe();
      setState({
        user,
        token: null, // server-managed session; no explicit token needed client-side
        isAuthenticated: true,
        loading: false,
      });
    } catch {
      // Session invalid or expired — clear state, don't redirect here
      // (the 401 interceptor in client.ts handles redirect for API calls;
      //  ProtectedRoute handles redirect for page access)
      clearAuth();
    }
  }, [clearAuth]);

  /**
   * Login action.
   * Mirrors LoginPresenter.doLogin → userService.login(user, callback).
   */
  const login = useCallback(async (username: string, password: string) => {
    const response = await authApi.login({ username, password });
    setState({
      user: response.user,
      token: response.token,
      isAuthenticated: true,
      loading: false,
    });
  }, []);

  /**
   * Logout action.
   * Mirrors MainPresenter.logoutClickHandler → userService.logout(sid, callback).
   */
  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Even if the logout API call fails, clear local state
    } finally {
      clearAuth();
    }
  }, [clearAuth]);

  // On mount, attempt session recovery (BL-003)
  useEffect(() => {
    checkSession();
  }, [checkSession]);

  const value: AuthContextValue = {
    ...state,
    login,
    logout,
    checkSession,
    clearAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Hook to access the auth context.
 * Must be used within an AuthProvider.
 */
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
