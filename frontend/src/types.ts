/**
 * TypeScript interfaces for the GWT Demo frontend.
 *
 * Mapped from:
 *   - src/main/java/com/example/client/model/Item.java
 *   - src/main/java/com/example/client/model/User.java
 *   - API contract: contracts/api-contract.yaml
 */

/** Corresponds to the Item domain model. */
export interface Item {
  id: number;
  name: string;
  description: string;
  /** ISO-8601 date string (YYYY-MM-DD) */
  date: string;
}

/** Corresponds to the User domain model (password NEVER included). */
export interface User {
  username: string;
}

/** Request body for POST /api/auth/login. */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Response body from POST /api/auth/login. */
export interface LoginResponse {
  token: string;
  user: User;
}

/** Structured error returned by the backend GlobalExceptionHandler. */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

/** Request body for DELETE /api/items. */
export interface DeleteRequest {
  ids: number[];
}
