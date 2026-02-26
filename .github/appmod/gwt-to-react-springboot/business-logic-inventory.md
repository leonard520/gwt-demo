# Business Logic Inventory

**Source Application**: gwt-demo
**Extraction Date**: 2025-01-30
**Total Business Logic Units**: 12
**Mode**: Rewrite (Full Re-architecture)

---

## Summary by Category

| Category | Count | Complexity | Source Layer |
|----------|-------|------------|-------------|
| Authentication | 3 | Medium-High | Server Service |
| CRUD Operations | 4 | Low-Medium | Server Service + DAO |
| Validation | 2 | Low | Model |
| Session Management | 2 | Medium | Server Service |
| Navigation / Error Routing | 1 | Medium | Client Presenter |

---

## Business Logic Units

### Authentication Logic

#### BL-001: User Login

- **Source**: `com.example.server.service.UserServiceImpl.login()` (lines ~30-55)
- **Source Methods**: `login(User user)`
- **Purpose**: Authenticates a user with username/password against BCrypt-hashed credentials
- **Requirements**: REQ-001, REQ-005, REQ-021

**Inputs**:
| Name | Type | Required | Description |
|---|---|---|---|
| `user` | `User` | Yes | Contains username and password |

**Outputs**:
| Name | Type | Description |
|---|---|---|
| `sessionId` | `String` | The HTTP session ID on success |

**Behavioral Spec**:
- condition: `user == null || !user.isValid()`
  - action: throw `LoginFailureException` with default message
  - branch_type: validation_guard
- condition: `userDao.findByUserName(user.getUsername()) == null`
  - action: throw `LoginFailureException("Sorry, we couldn't locate you in our records.")`
  - branch_type: not_found
- condition: `!BCrypt.checkpw(user.getPassword(), dbUser.getPassword())`
  - action: throw `LoginFailureException` with default message
  - branch_type: auth_failure
- condition: BCrypt password matches
  - action: store `User.clone()` in `sessionMap` keyed by `httpSession.getId()`, return `httpSession.getId()`
  - branch_type: success

**Side Effects**:
- Adds user (without password, via `clone()`) to `sessionMap` keyed by session ID
- HTTP session is created

**Edge Cases**:
- Null user object → exception
- Empty username or password → fails `isValid()` → exception
- Correct username, wrong password → exception
- User not in repository → custom error message

---

#### BL-002: User Logout

- **Source**: `com.example.server.service.UserServiceImpl.logout()` (lines ~56-68)
- **Source Methods**: `logout(String sessionId)`
- **Purpose**: Invalidates a user's session
- **Requirements**: REQ-002, REQ-021

**Inputs**:
| Name | Type | Required | Description |
|---|---|---|---|
| `sessionId` | `String` | Yes | The session to invalidate |

**Behavioral Spec**:
- condition: `isSessionValid(sessionId)` returns false
  - action: throw `LoginFailureException` (invalid session)
  - branch_type: validation_guard
- condition: valid session
  - action: remove from `sessionMap`, call `httpSession.invalidate()`
  - branch_type: success

**Side Effects**:
- Removes user from `sessionMap`
- Invalidates HTTP session

---

#### BL-003: Session Recovery (isLoggedIn)

- **Source**: `com.example.server.service.UserServiceImpl.isLoggedIn()` (lines ~70-82)
- **Source Methods**: `isLoggedIn(String sessionId)`
- **Purpose**: Checks if a session is still valid and returns the associated user
- **Requirements**: REQ-003, REQ-021

**Inputs**:
| Name | Type | Required | Description |
|---|---|---|---|
| `sessionId` | `String` | Yes | Session ID to verify |

**Outputs**:
| Name | Type | Description |
|---|---|---|
| `user` | `User` | User associated with the session (no password) |

**Behavioral Spec**:
- condition: `!isSessionValid(sessionId)`
  - action: throw `LoginFailureException`
  - branch_type: validation_guard
- condition: valid session
  - action: return `sessionMap.get(sessionId)` (already cloned, no password)
  - branch_type: success

---

### Session Management Logic

#### BL-004: Session Validation

- **Source**: `com.example.server.service.AbstractService.isSessionValid()` (lines ~15-25)
- **Source Methods**: `isSessionValid(String sessionId)`
- **Purpose**: Dual-check session validity (sessionMap + XSRF)
- **Requirements**: REQ-004, REQ-029, REQ-030

**Behavioral Spec**:
- condition: `sessionMap.containsKey(sessionId) && httpSession.getId().equals(sessionId)`
  - action: return `true`
  - note: The second check (`httpSession.getId().equals(sessionId)`) is a basic XSRF protection
- condition: either check fails
  - action: return `false`

**Rewrite Note**: Replaced entirely by Spring Security session management. The XSRF check is replaced by Spring Security's CSRF protection or the inherent safety of session-cookie-based auth.

---

#### BL-005: Session Expiry Cleanup

- **Source**: `com.example.server.service.UserServiceImpl.valueUnbound()` (HttpSessionBindingListener)
- **Source Methods**: `valueUnbound(HttpSessionBindingEvent event)`
- **Purpose**: Automatically cleans up expired sessions from sessionMap
- **Requirements**: REQ-029

**Behavioral Spec**:
- condition: HTTP session expires (container-managed timeout)
  - action: `valueUnbound()` fires → removes entry from `sessionMap`
  - note: `setHttpSession()` registers the service as an `HttpSessionBindingListener`

**Rewrite Note**: Spring Security handles session lifecycle automatically. No manual cleanup needed.

---

### Item CRUD Logic

#### BL-006: Find All Items

- **Source**: `com.example.server.service.ItemServiceImpl.findAll()`, `com.example.server.dao.ItemDaoImpl.findAll()`
- **Source Methods**: `findAll(String sessionId)`
- **Purpose**: Returns all items from the repository
- **Requirements**: REQ-006, REQ-022

**Behavioral Spec**:
- action: return `new ArrayList<>(map.values())`
- note: Returns a copy of all items; order is not guaranteed (HashMap iteration order)

**Rewrite Note**: Session validation (previously in `RpcController`) moves to Spring Security filter chain.

---

#### BL-007: Create Item

- **Source**: `com.example.server.dao.ItemDaoImpl.create()`
- **Source Methods**: `create(String sessionId, Item item)` (service), `create(Item item)` (DAO)
- **Purpose**: Creates a new item with auto-assigned ID
- **Requirements**: REQ-007, REQ-018, REQ-022

**Behavioral Spec**:
- condition: `item != null`
  - action: assign `id = sequence.incrementAndGet()`, put in map
  - branch_type: success
- condition: `item == null`
  - action: no-op (null guard in DAO)
  - branch_type: early_return

**Side Effects**:
- ID sequence incremented
- Item stored in `ConcurrentHashMap`

**Service Layer**: Wraps DAO call in try-catch, throws `ItemServiceException` on any exception.

---

#### BL-008: Update Item

- **Source**: `com.example.server.dao.ItemDaoImpl.update()`
- **Source Methods**: `update(String sessionId, Item item)` (service), `update(Item item)` (DAO)
- **Purpose**: Updates an existing item by ID
- **Requirements**: REQ-008, REQ-022

**Behavioral Spec**:
- condition: `item != null && map.containsKey(item.getId())`
  - action: `map.put(item.getId(), item)` — replaces existing item
  - branch_type: success
- condition: `item == null || !map.containsKey(item.getId())`
  - action: no-op (silent ignore)
  - branch_type: early_return

**Note**: Non-existent IDs are silently ignored — NOT an error.

---

#### BL-009: Delete Items (Batch)

- **Source**: `com.example.server.dao.ItemDaoImpl.delete()`
- **Source Methods**: `delete(String sessionId, ArrayList<Item> items)` (service), `delete(ArrayList<Item> items)` (DAO)
- **Purpose**: Batch-deletes items by ID
- **Requirements**: REQ-009, REQ-022

**Behavioral Spec**:
- condition: `items == null || items.isEmpty()`
  - action: no-op (early return)
  - branch_type: early_return
- condition: valid non-empty list
  - action: iterate items, `map.remove(item.getId())` for each
  - branch_type: success

**Note**: Non-existent IDs in the list are silently ignored by `map.remove()`.

---

### Validation Logic

#### BL-010: User Validation

- **Source**: `com.example.client.model.User.isValid()`
- **Source Methods**: `isValid()`
- **Purpose**: Validates that both username and password are present
- **Requirements**: REQ-019, REQ-001

**Behavioral Spec**:
- condition: `username != null && !username.isEmpty() && password != null && !password.isEmpty()`
  - action: return `true`
- condition: any field null or empty
  - action: return `false`

---

#### BL-011: Password Security (Never Expose)

- **Source**: `com.example.client.model.User.clone()`
- **Source Methods**: `clone()`
- **Purpose**: Creates a copy of User without the password field
- **Requirements**: REQ-005, REQ-019

**Behavioral Spec**:
- action: `new User(this.username, null)` — password explicitly set to null
- usage: Called before storing user in `sessionMap` and before returning user in API responses

**Rewrite**: Replaced by `@JsonIgnore` on the password field + DTO pattern.

---

### Navigation / Error Routing Logic

#### BL-012: Session Timeout Error Routing

- **Source**: `com.example.client.presenter.MainPresenter.handleThrowable()`
- **Source Methods**: `handleThrowable(Throwable caught)`
- **Purpose**: Routes errors — session timeouts trigger logout+redirect; other errors display message
- **Requirements**: REQ-004, REQ-017

**Behavioral Spec**:
- condition: `caught instanceof SessionTimedOutException`
  - action: call `logoutClickHandler.onClick(null)` → fires `SessionTimedOutEvent` → AppController redirects to login
  - branch_type: session_expired
- condition: any other exception
  - action: `display.setErrorMsg(caught.getMessage())`
  - branch_type: display_error

**Rewrite**: Frontend HTTP client interceptor checks for 401 responses → clears auth state → redirects to login. Non-401 errors are displayed in the UI.

---

## Cross-Cutting Concerns

### Authentication/Authorization
- **Location**: `RpcController.validateSession()`, `AbstractService.isSessionValid()`, `UserServiceImpl`
- **Pattern**: Front-controller validates session on every non-auth request; session stored in static `ConcurrentHashMap`
- **Rewrite approach**: Spring Security filter chain with session-based authentication

### Error Handling
- **Location**: `LoginFailureException`, `SessionTimedOutException`, `ItemServiceException`
- **Pattern**: Custom exceptions implementing `IsSerializable`, thrown by services, serialized over GWT-RPC
- **Rewrite approach**: Custom exceptions + `@ControllerAdvice` `GlobalExceptionHandler` returning structured JSON

### Transaction Management
- **Location**: N/A — no database transactions in source (in-memory stores)
- **Rewrite approach**: Not needed for in-memory implementation

---

## Rewrite Priority

| Priority | Business Logic Units | Reason |
|----------|---------------------|--------|
| P1 | BL-001 (Login), BL-004 (Session Validation) | Authentication is prerequisite for all protected operations |
| P1 | BL-010 (User Validation), BL-011 (Password Security) | Security-critical |
| P2 | BL-006 (FindAll), BL-007 (Create), BL-008 (Update), BL-009 (Delete) | Core CRUD functionality |
| P2 | BL-002 (Logout), BL-003 (Session Recovery) | Session lifecycle |
| P3 | BL-005 (Session Cleanup), BL-012 (Error Routing) | Supporting behavior, partially handled by framework |
