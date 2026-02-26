/**
 * ProtectedRoute component.
 *
 * Wraps child routes that require authentication.
 * - If the user is authenticated, renders children.
 * - If auth is still loading (session recovery in progress), shows a loading indicator.
 * - If not authenticated, redirects to /login.
 *
 * Replaces the GWT pattern:
 *   AppController.navigateToMain → checks session → on failure redirects to login.
 *
 * Source reference:
 *   - src/main/java/com/example/client/AppController.java#navigateToMain,callback
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <p>Loading...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
