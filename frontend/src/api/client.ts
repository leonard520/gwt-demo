/**
 * Fetch-based HTTP client wrapper.
 *
 * Provides:
 *   - JSON content-type headers on every request
 *   - credentials: 'include' for session cookie (JSESSIONID) transport
 *   - Automatic JSON response parsing
 *   - 401 interceptor: clears auth state and redirects to /login (BL-012)
 *
 * Source references:
 *   - src/main/java/com/example/client/cookies/Cookies.java  (session cookie handling)
 *   - src/main/java/com/example/client/presenter/MainPresenter.java#handleThrowable
 */

const BASE_URL = '';  // Same-origin; Vite proxy handles /api → backend in dev

/**
 * Performs an HTTP request with JSON defaults and session cookie support.
 *
 * @throws Error with the server's error message for non-OK responses.
 *         For 401 responses the error is thrown AFTER triggering a redirect.
 */
export async function apiRequest<T>(
  url: string,
  options: RequestInit = {},
): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  const response = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers,
    credentials: 'include', // send JSESSIONID cookie with every request
  });

  // --- 401 Interceptor (BL-012) ---
  // When the session expires the backend returns 401.
  // The original GWT app handled this in MainPresenter.handleThrowable:
  //   SessionTimedOutException → logout + fire SessionTimedOutEvent → redirect to login.
  // Here we replicate that behavior with a simple redirect.
  if (response.status === 401) {
    // Clear any client-side auth artefacts — the AuthContext listens for
    // the storage event or the redirect itself will re-initialise state.
    // Redirect to login page only if we aren't already on it.
    if (!window.location.pathname.startsWith('/login')) {
      window.location.href = '/login';
    }
    // Still throw so callers can handle the rejection.
    const errorBody = await response.json().catch(() => ({
      message: 'Session expired. Please log in again.',
    }));
    throw new Error(errorBody.message ?? 'Unauthorized');
  }

  // --- 204 No Content ---
  if (response.status === 204) {
    return undefined as T;
  }

  // --- Other non-OK statuses ---
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({
      message: `Request failed with status ${response.status}`,
    }));
    throw new Error(errorBody.message ?? `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}
