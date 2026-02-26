/**
 * Application root component.
 *
 * Configures React Router routes and wraps everything in AuthProvider.
 *
 * Route mapping (mirrors GWT History tokens from AppController.onValueChange):
 *   - /login → LoginPage
 *   - /main  → ProtectedRoute(MainPage)
 *   - /      → Redirect to /login
 *
 * Source references:
 *   - src/main/java/com/example/client/AppController.java#onValueChange
 *   - src/main/java/com/example/client/Application.java
 */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import MainPage from './pages/MainPage';

const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/main"
            element={
              <ProtectedRoute>
                <MainPage />
              </ProtectedRoute>
            }
          />
          {/* Default: redirect to login (mirrors AppController.go — empty history → login) */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
