# REST API Contract (Target)

> Maps GWT-RPC service interfaces to target REST API endpoints.
> This serves as the bridge between the React frontend and Spring Boot backend.

---

## 1. Authentication API

**Source:** `UserService.java` (GWT-RPC at `login.rpc`)

### POST /api/auth/login
**Source method:** `String login(User user) throws LoginFailureException`

| | Request | Response (200) | Response (401) |
|---|---|---|---|
| **Body** | `{ "username": "string", "password": "string" }` | `{ "token": "string", "user": { "username": "string" } }` | `{ "error": "string", "message": "The email or password you entered is incorrect." }` |

**Behavioral Notes:**
- Validates user is non-null, `isValid()` returns true
- Looks up user by username via UserRepository
- Verifies password with BCryptPasswordEncoder
- Returns token (JWT or session ID) — NEVER returns password
- Original uses `User.clone()` to strip password — target uses `@JsonIgnore` or DTO

### POST /api/auth/logout
**Source method:** `void logout(String sessionId)`

| | Request | Response (200) |
|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `204 No Content` |

**Behavioral Notes:**
- Invalidates the session/token
- Original removed from `sessionMap` and called `httpSession.invalidate()`

### GET /api/auth/me
**Source method:** `User isLoggedIn(String sessionId) throws LoginFailureException`

| | Request | Response (200) | Response (401) |
|---|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `{ "username": "string" }` | `{ "error": "string" }` |

**Behavioral Notes:**
- Validates the token/session is still active
- Returns the user associated with the session (without password)
- Used by frontend on app startup to recover session from stored token

---

## 2. Items API

**Source:** `ItemService.java` (GWT-RPC at `item.rpc`)
**Authentication:** All endpoints require valid token (replaces `sessionId` parameter + `RpcController.validateSession()`)

### GET /api/items
**Source method:** `ArrayList<Item> findAll(String sessionId)`

| | Request | Response (200) |
|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `[{ "id": number, "name": "string", "description": "string", "date": "ISO-8601" }, ...]` |

### POST /api/items
**Source method:** `void create(String sessionId, Item item)`

| | Request | Response (201) |
|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `{ "id": number, "name": "string", "description": "string", "date": "ISO-8601" }` |
| **Body** | `{ "name": "string", "description": "string", "date": "ISO-8601" }` | |

**Behavioral Notes:**
- Server assigns ID (auto-increment)
- Original returned `void`; target returns created item with ID for better REST semantics

### PUT /api/items/{id}
**Source method:** `void update(String sessionId, Item item)`

| | Request | Response (200) |
|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `{ "id": number, "name": "string", "description": "string", "date": "ISO-8601" }` |
| **Body** | `{ "name": "string", "description": "string", "date": "ISO-8601" }` | |

**Behavioral Notes:**
- Original identified item by `item.getId()` in body; target uses URL path parameter
- Original returned `void`; target returns updated item

### DELETE /api/items
**Source method:** `void delete(String sessionId, ArrayList<Item> items)`

| | Request | Response (204) |
|---|---|---|
| **Header** | `Authorization: Bearer <token>` | `204 No Content` |
| **Body** | `{ "ids": [number, ...] }` | |

**Behavioral Notes:**
- Original accepted `ArrayList<Item>` with full objects; target accepts array of IDs only
- Supports batch delete (matching original behavior where multiple checkboxes could be selected)

---

## 3. Error Response Format

**Source:** GWT-RPC serialized exceptions

All error responses follow a consistent JSON structure:

```json
{
  "timestamp": "ISO-8601",
  "status": 401,
  "error": "Unauthorized",
  "message": "The session has timed out. You will need to login again.",
  "path": "/api/items"
}
```

### Error Mapping

| Source Exception | HTTP Status | Error Code |
|---|---|---|
| `LoginFailureException` | 401 Unauthorized | `AUTH_FAILED` |
| `SessionTimedOutException` | 401 Unauthorized | `SESSION_EXPIRED` |
| `ItemServiceException` | 500 Internal Server Error | `ITEM_SERVICE_ERROR` |

---

## 4. CORS Configuration

Since frontend and backend are separate deployables:

```yaml
# application.properties (or SecurityConfig.java)
cors:
  allowed-origins: http://localhost:3000  # React dev server
  allowed-methods: GET, POST, PUT, DELETE, OPTIONS
  allowed-headers: Authorization, Content-Type
  allow-credentials: true
```
