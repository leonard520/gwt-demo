/**
 * Authentication API functions.
 *
 * Maps the GWT-RPC UserService/UserServiceAsync interface to REST calls:
 *   - UserService.login(User)          → POST /api/auth/login
 *   - UserService.logout(sessionId)    → POST /api/auth/logout
 *   - UserService.isLoggedIn(sessionId)→ GET  /api/auth/me
 *
 * Source references:
 *   - src/main/java/com/example/client/service/UserService.java
 *   - src/main/java/com/example/client/service/UserServiceAsync.java
 */

import { apiRequest } from './client';
import type { LoginRequest, LoginResponse, User } from '../types';

/**
 * Authenticates a user.
 * On success the backend sets the JSESSIONID cookie automatically.
 */
export function login(req: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

/**
 * Logs the current user out by invalidating the server session.
 */
export function logout(): Promise<void> {
  return apiRequest<void>('/api/auth/logout', {
    method: 'POST',
  });
}

/**
 * Returns the currently authenticated user.
 * Used for session recovery on page load (BL-003).
 */
export function getMe(): Promise<User> {
  return apiRequest<User>('/api/auth/me', {
    method: 'GET',
  });
}
