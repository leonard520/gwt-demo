/**
 * Tests for LoginPage component.
 *
 * Mirrors the test patterns from the original GWT LoginPresenterTest:
 *   - testLoginPresenter                    → renders form elements
 *   - testLoginClickHandlerUserInvalid      → client-side validation (empty fields)
 *   - testLoginClickHandlerUserValid        → successful login navigates to /main
 *   - testCallBackFailure                   → failed login shows error
 *   - testKeyDownHandlerUserValid           → ENTER key triggers login
 *   - testKeyDownHandlerUserInvalid         → ENTER key with empty fields shows error
 *   + error clears on new input             → React-specific (onChange clears error)
 *
 * Source reference:
 *   - src/test/java/com/example/client/presenter/LoginPresenterTest.java
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import LoginPage from '../pages/LoginPage';

// --- Mocks ---

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const mockLogin = jest.fn();
jest.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    login: mockLogin,
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    logout: jest.fn(),
    checkSession: jest.fn(),
    clearAuth: jest.fn(),
  }),
}));

// --- Helpers ---

function renderLoginPage() {
  return render(<LoginPage />);
}

// --- Test Suite ---

beforeEach(() => {
  jest.clearAllMocks();
});

describe('LoginPage', () => {
  /**
   * Mirrors: testLoginPresenter + testGo
   * Verifies the form renders with all required elements.
   */
  test('renders form elements: username input, password input (masked), and login button', () => {
    renderLoginPage();

    const usernameInput = screen.getByLabelText(/username/i);
    expect(usernameInput).toBeInTheDocument();
    expect(usernameInput).toHaveAttribute('type', 'text');

    const passwordInput = screen.getByLabelText(/password/i);
    expect(passwordInput).toBeInTheDocument();
    expect(passwordInput).toHaveAttribute('type', 'password');

    expect(
      screen.getByRole('button', { name: /login/i }),
    ).toBeInTheDocument();
  });

  /**
   * Mirrors: testLoginClickHandlerUserInvalid
   * BL-010: User.isValid() → both fields empty → error
   */
  test('client-side validation: clicking Login with both fields empty shows error', () => {
    renderLoginPage();

    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Please enter a username and password',
    );
    expect(mockLogin).not.toHaveBeenCalled();
  });

  /**
   * BL-010: Only username provided → password empty → error
   */
  test('client-side validation: only username provided shows error', () => {
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'testuser' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Please enter a username and password',
    );
    expect(mockLogin).not.toHaveBeenCalled();
  });

  /**
   * BL-010: Only password provided → username empty → error
   */
  test('client-side validation: only password provided shows error', () => {
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'secret' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Please enter a username and password',
    );
    expect(mockLogin).not.toHaveBeenCalled();
  });

  /**
   * Mirrors: testLoginClickHandlerUserValid + testCallbackSuccess
   * BL-001: Valid credentials → auth.login() → navigate to /main
   */
  test('successful login navigates to /main', async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'admin' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('admin', 'password');
      expect(mockNavigate).toHaveBeenCalledWith('/main');
    });
  });

  /**
   * Mirrors: testCallBackFailure (user not found)
   * Server returns: "Sorry, we couldn't locate you in our records."
   */
  test('failed login shows server error message (user not found)', async () => {
    mockLogin.mockRejectedValueOnce(
      new Error("Sorry, we couldn't locate you in our records."),
    );
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'unknown' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'wrongpass' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        "Sorry, we couldn't locate you in our records.",
      );
    });
  });

  /**
   * Mirrors: testCallBackFailure (wrong password)
   * Server returns: "The email or password you entered is incorrect."
   */
  test('failed login shows server error message (wrong password)', async () => {
    mockLogin.mockRejectedValueOnce(
      new Error('The email or password you entered is incorrect.'),
    );
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'admin' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'wrongpassword' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'The email or password you entered is incorrect.',
      );
    });
  });

  /**
   * Mirrors: testKeyDownHandlerUserValid (on username input)
   * ENTER key on username field triggers login.
   */
  test('ENTER key on username input triggers login', async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'admin' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password' },
    });
    fireEvent.keyDown(screen.getByLabelText(/username/i), {
      key: 'Enter',
      code: 'Enter',
    });

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('admin', 'password');
      expect(mockNavigate).toHaveBeenCalledWith('/main');
    });
  });

  /**
   * Mirrors: testKeyDownHandlerUserValid (on password input)
   * ENTER key on password field triggers login.
   */
  test('ENTER key on password input triggers login', async () => {
    mockLogin.mockResolvedValueOnce(undefined);
    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'admin' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password' },
    });
    fireEvent.keyDown(screen.getByLabelText(/password/i), {
      key: 'Enter',
      code: 'Enter',
    });

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('admin', 'password');
      expect(mockNavigate).toHaveBeenCalledWith('/main');
    });
  });

  /**
   * Mirrors: testKeyDownHandlerUserInvalid
   * ENTER key with empty fields → validation error, no API call.
   */
  test('ENTER key with empty fields shows validation error', () => {
    renderLoginPage();

    fireEvent.keyDown(screen.getByLabelText(/username/i), {
      key: 'Enter',
      code: 'Enter',
    });

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Please enter a username and password',
    );
    expect(mockLogin).not.toHaveBeenCalled();
  });

  /**
   * React-specific: error clears when user types (onChange).
   * Matches original keyDownHandler behavior: display.setErrorMsg(null) on any key.
   */
  test('error clears when user types in username field', () => {
    renderLoginPage();

    // Trigger validation error
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    expect(screen.getByRole('alert')).toBeInTheDocument();

    // Type in username — error should clear via onChange
    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'a' },
    });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  /**
   * Error also clears when typing in password field.
   */
  test('error clears when user types in password field', () => {
    renderLoginPage();

    // Trigger validation error
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    expect(screen.getByRole('alert')).toBeInTheDocument();

    // Type in password — error should clear via onChange
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'x' },
    });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});
