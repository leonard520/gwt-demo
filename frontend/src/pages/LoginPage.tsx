/**
 * LoginPage — Full login form with authentication.
 *
 * Rewrite of the GWT LoginView + LoginPresenter MVP pair into a single
 * React component that integrates with AuthContext.
 *
 * Behavioral mapping:
 *   LoginView.ui.xml + LoginView.java  → JSX form structure
 *   LoginPresenter.doLogin()           → doLogin() local function
 *   LoginPresenter.keyDownHandler      → handleKeyDown() on both inputs
 *   LoginPresenter.loginClickHandler   → onClick on Login button
 *   LoginPresenter.callback.onSuccess  → navigate('/main') after auth.login()
 *   LoginPresenter.callback.onFailure  → setError(err.message)
 *
 * Business Logic:
 *   BL-001: Login flow (frontend portion — client-side validation + API call)
 *   BL-010: User.isValid() — both username and password must be non-empty
 *
 * Source references:
 *   - src/main/java/com/example/client/view/LoginView.java
 *   - src/main/java/com/example/client/presenter/LoginPresenter.java#doLogin,callback
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  /**
   * Mirrors LoginPresenter.doLogin():
   *   1. Clear previous errors  (display.setErrorMsg(null))
   *   2. Validate both fields   (user.isValid() → BL-010)
   *   3. Call auth service       (loginService.login(user, callback) → BL-001)
   *   4. On success → navigate   (callback.onSuccess → cookie + LoginEvent)
   *   5. On failure → show error (callback.onFailure → display.setErrorMsg)
   */
  const doLogin = async () => {
    // Clear all previous errors (LoginPresenter.doLogin first line)
    setError('');

    // Client-side validation: BL-010 (User.isValid())
    // Original: (username != null) && !(username.isEmpty()) && (password != null) && !(password.isEmpty())
    if (!username || !password) {
      setError('Please enter a username and password');
      return;
    }

    try {
      // LoginPresenter: loginService.login(user, callback)
      await login(username, password);
      // callback.onSuccess → navigate to /main
      // (replaces: cookie.setCookie("sid",...) + eventBus.fireEvent(new LoginEvent(user)))
      navigate('/main');
    } catch (err) {
      // callback.onFailure → display.setErrorMsg(t.getMessage())
      // Server returns specific messages like:
      //   "Sorry, we couldn't locate you in our records." (user not found)
      //   "The email or password you entered is incorrect." (wrong password)
      const message =
        err instanceof Error ? err.message : 'An unexpected error occurred';
      setError(message);
    }
  };

  /**
   * Mirrors LoginPresenter.keyDownHandler:
   *   - Clears error on any key press (display.setErrorMsg(null))
   *   - If ENTER key → triggers doLogin()
   *
   * Applied to BOTH username and password inputs, matching the original:
   *   for(HasKeyDownHandlers h : display.keyDownHandlers()) {
   *       h.addKeyDownHandler(keyDownHandler);
   *   }
   */
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // Clear error on any key press (matches keyDownHandler first line)
    setError('');
    // ENTER triggers login (matches: if(KeyCodes.KEY_ENTER == evt.getNativeKeyCode()))
    if (e.key === 'Enter') {
      doLogin();
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
      }}
    >
      {/* Mirrors LoginView.loginPanel (DecoratorPanel) — centered container */}
      <div
        style={{
          border: '1px solid #ccc',
          padding: '2rem',
          borderRadius: '8px',
          minWidth: '300px',
        }}
      >
        <h2>Login</h2>

        {/* Username field — maps to LoginView.usernameBox (TextBox) */}
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="username">Username</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              setError(''); // Clear error on input change
            }}
            onKeyDown={handleKeyDown}
            style={{
              display: 'block',
              width: '100%',
              marginTop: '0.25rem',
              padding: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Password field — maps to LoginView.passwordBox (PasswordTextBox) */}
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setError(''); // Clear error on input change
            }}
            onKeyDown={handleKeyDown}
            style={{
              display: 'block',
              width: '100%',
              marginTop: '0.25rem',
              padding: '0.5rem',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Error display — maps to LoginView.errorLabel (Label) */}
        {error && (
          <div role="alert" style={{ color: 'red', marginBottom: '1rem' }}>
            {error}
          </div>
        )}

        {/* Login button — maps to LoginView.loginButton (Button) */}
        <button
          onClick={doLogin}
          style={{ width: '100%', padding: '0.5rem' }}
        >
          Login
        </button>
      </div>
    </div>
  );
};

export default LoginPage;
