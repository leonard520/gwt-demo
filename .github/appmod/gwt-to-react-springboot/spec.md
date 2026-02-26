# Feature Specification: GWT-to-React+SpringBoot Rewrite

> **Branch Short Name:** `gwt-to-react-springboot`
> **Mode:** Rewrite (Full Re-architecture)
> **Created:** Auto-generated during Design Phase
> **Constitution Reference:** `.github/appmod/constitution.md`
> **Knowledge Graph:** `.github/appmod/knowledge-graph/`

---

## 1. Overview

### 1.1 Problem Statement

The existing application is built on GWT 2.0 (Java 1.6), Spring 2.5.6, and a WAR-based deployment model targeting JBoss 4.2.x / Jetty 6.1.10. These technologies are end-of-life, unsupported, and present security risks (e.g., Log4j 1.2, custom BCrypt implementation, servlet-era session management). The monolithic client-server architecture couples the UI tightly to the Java backend via GWT-RPC, preventing independent development, testing, and deployment of frontend and backend.

### 1.2 Proposed Solution

Rewrite the application as two fully decoupled deployable units:

1. **Backend:** Spring Boot 3.x REST API (Java 17+) with Spring Security, providing JSON-over-HTTP endpoints
2. **Frontend:** React 18+ SPA (TypeScript) communicating exclusively via the REST API

This rewrite preserves all existing business logic and user-facing behavior (Constitution **P1: Functional Equivalence**) while modernizing the technology stack, improving security posture, and enabling independent frontend/backend development lifecycles (Constitution **P2: Clean Separation of Concerns**).

### 1.3 Scope

| In Scope | Out of Scope |
|---|---|
| All existing GWT client functionality | Persistent database (current system uses in-memory stores) |
| All existing server-side business logic | New features not present in source application |
| Authentication (login/logout/session recovery) | OAuth2/OIDC integration |
| Item CRUD operations | Real-time features (WebSocket) |
| Navigation and routing | Internationalization (not in source) |
| Session management and timeout handling | Mobile-specific UI |
| Error handling and user feedback | Deployment infrastructure (CI/CD, Docker) |
| Seed data (mock users and items) | Production-grade database migration |

### 1.4 Source System Summary

| Attribute | Value |
|---|---|
| Language | Java 1.6 |
| UI Framework | GWT 2.0 (UiBinder + programmatic UI) |
| Backend Framework | Spring 2.5.6 (XML config) |
| Communication | GWT-RPC (`RemoteService`, `AsyncCallback`) |
| Architecture | MVP (Model-View-Presenter) + EventBus |
| Security | Custom BCrypt, servlet HTTP sessions, cookie-based `sid` |
| Data Storage | In-memory (`HashMap`, `ConcurrentHashMap`) |
| Packaging | WAR deployed to JBoss/Jetty |
| Source Files | 27 main Java + 14 test Java + 7 XML configs |

---

## 2. Target Architecture

### 2.1 High-Level Architecture

```
┌──────────────────────────────┐     HTTP/JSON      ┌──────────────────────────────┐
│       REACT FRONTEND         │◄──────────────────►│     SPRING BOOT BACKEND      │
│                              │                     │                              │
│  TypeScript + React 18+      │  REST API           │  Java 17+ + Spring Boot 3.x  │
│  Vite build                  │  (see §4)           │  Maven build                 │
│  React Router                │                     │  Embedded Tomcat             │
│  Context API (auth state)    │                     │  Spring Security             │
│  Fetch/Axios HTTP client     │                     │  BCryptPasswordEncoder       │
│  Jest + RTL tests            │                     │  JUnit 5 + Mockito tests     │
│                              │                     │                              │
│  Build: npm / Vite           │                     │  Build: Maven                │
│  Output: Static files        │                     │  Output: Executable JAR      │
└──────────────────────────────┘                     └──────────────────────────────┘
```

*Constitution references: **P2** (Clean Separation), **P5** (API-First Design), **AC1–AC3** (Constraints)*

### 2.2 Backend Architecture (Spring Boot)

```
com.example
├── GwtDemoApplication.java          // @SpringBootApplication
├── config/
│   ├── SecurityConfig.java          // Spring Security, BCrypt, CORS
│   └── WebConfig.java               // Additional web configuration
├── controller/
│   ├── AuthController.java          // POST /api/auth/login, logout, GET /api/auth/me
│   └── ItemController.java          // GET/POST/PUT/DELETE /api/items
├── service/
│   ├── UserService.java             // Authentication business logic
│   └── ItemService.java             // Item CRUD business logic
├── repository/
│   ├── UserRepository.java          // User data access (in-memory)
│   └── ItemRepository.java          // Item data access (in-memory)
├── model/
│   ├── User.java                    // Domain model
│   └── Item.java                    // Domain model
├── dto/
│   ├── LoginRequest.java            // Login request DTO
│   ├── LoginResponse.java           // Login response DTO
│   └── ErrorResponse.java           // Structured error response
└── exception/
    ├── LoginFailureException.java
    ├── SessionExpiredException.java
    ├── ItemServiceException.java
    └── GlobalExceptionHandler.java  // @ControllerAdvice
```

### 2.3 Frontend Architecture (React + TypeScript)

```
frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── src/
│   ├── App.tsx                      // Root component + Router config
│   ├── main.tsx                     // React DOM entry point
│   ├── types.ts                     // TypeScript interfaces (Item, User, etc.)
│   ├── api/
│   │   ├── client.ts                // HTTP client (Fetch/Axios wrapper)
│   │   ├── authApi.ts               // Auth API calls
│   │   └── itemApi.ts               // Item API calls
│   ├── context/
│   │   └── AuthContext.tsx           // Auth state + provider + useAuth hook
│   ├── pages/
│   │   ├── LoginPage.tsx            // Login form page
│   │   └── MainPage.tsx             // Item management page
│   ├── components/
│   │   ├── ItemDialog.tsx           // Create/edit item modal
│   │   ├── ItemTable.tsx            // Item list table
│   │   └── ProtectedRoute.tsx       // Auth-guarded route wrapper
│   └── __tests__/                   // Jest + RTL tests
└── public/
    └── index.html
```

---

## 3. Functional Requirements

### 3.1 Authentication

#### REQ-001: User Login

**Source:** `LoginPresenter.java`, `UserServiceImpl.login()`, `LoginView.java`
**Constitution:** P1 (Functional Equivalence), P3 (Modern Security), BC1 (Authentication Flow)

The system SHALL allow a user to authenticate by providing a username and password.

**Behavior (derived from source code):**
1. User enters username and password in a login form
2. Client-side validation: both fields must be non-null and non-empty (`User.isValid()`)
3. If validation fails, display: "Please enter a username and password"
4. Credentials are sent to the backend via `POST /api/auth/login`
5. Backend looks up user by username in the user repository
6. If user not found, return 401 with message: "Sorry, we couldn't locate you in our records."
7. If user found, verify password using BCrypt (`BCryptPasswordEncoder.matches()`)
8. If password incorrect, return 401 with default message: "The email or password you entered is incorrect."
9. If password correct, create an authenticated session/token and return it with the user's username (never the password)
10. Frontend stores the authentication token and navigates to the main page

**Acceptance Criteria:**
- [ ] Login form renders with username field, password field, and login button
- [ ] Pressing ENTER in either input field triggers login (source: `KeyDownHandler` in `LoginPresenter`)
- [ ] Client-side validation prevents empty submissions
- [ ] Successful login returns token + username (no password)
- [ ] Failed login displays server-provided error message
- [ ] After successful login, user is navigated to the main (items) page
- [ ] BCrypt password verification is compatible with existing hashed passwords (`$2a` format, 10 rounds default)

---

#### REQ-002: User Logout

**Source:** `MainPresenter.logoutClickHandler`, `UserServiceImpl.logout()`
**Constitution:** P1 (Functional Equivalence), P3 (Modern Security)

The system SHALL allow an authenticated user to log out.

**Behavior (derived from source code):**
1. User clicks the logout link in the main view
2. Frontend calls `POST /api/auth/logout` with the current token
3. Backend invalidates the session/token
4. Frontend removes stored authentication token
5. User is NOT automatically redirected to login (source: `logoutClickHandler` only removes cookie; `SessionTimedOutEvent` handles redirect)

**Acceptance Criteria:**
- [ ] Logout link is visible on the main page
- [ ] Clicking logout calls the backend to invalidate the session
- [ ] Authentication token is removed from client storage
- [ ] Backend session is fully invalidated

---

#### REQ-003: Session Recovery on Page Load

**Source:** `AppController.navigateToMain()`, `AppController.callback`, `UserServiceImpl.isLoggedIn()`
**Constitution:** P1 (Functional Equivalence), BC1 (Authentication Flow)

The system SHALL attempt to recover an existing session when the application loads.

**Behavior (derived from source code):**
1. On application startup, check if a stored authentication token exists
2. If token exists, call `GET /api/auth/me` to verify the session is still valid
3. If valid, the backend returns the user (without password) and the frontend navigates to the main page, firing the equivalent of a login event (fetching items, showing welcome text)
4. If invalid (401), redirect to the login page

**Acceptance Criteria:**
- [ ] Page refresh with valid token recovers session without re-login
- [ ] Page refresh with expired/invalid token redirects to login
- [ ] Recovered session triggers item list fetch and welcome text display
- [ ] First load with no stored token shows the login page

---

#### REQ-004: Session Timeout Handling

**Source:** `MainPresenter.handleThrowable()`, `SessionTimedOutEvent`, `AppController`, `RpcController.validateSession()`
**Constitution:** P1 (Functional Equivalence), P3 (Modern Security), BC2 (Session Validation), BC4 (Session Timeout)

The system SHALL detect session expiration and redirect the user to the login page.

**Behavior (derived from source code):**
1. Any API call that returns 401 (session expired) is intercepted by the HTTP client
2. The frontend performs a logout (removes token)
3. The user is redirected to the login page
4. The original error flow: `SessionTimedOutException` → `MainPresenter.handleThrowable()` → calls `logoutClickHandler.onClick(null)` → fires `SessionTimedOutEvent` → `AppController.doLogin()`

**Acceptance Criteria:**
- [ ] Expired session on any API call triggers automatic redirect to login
- [ ] Client-side token is cleared on session timeout
- [ ] Backend logout is attempted before redirect (matching source behavior)
- [ ] Session timeout (20 minutes, from `web.xml`) is configurable in backend properties

---

#### REQ-005: Password Security

**Source:** `BCrypt.java`, `UserDaoImpl`, `UserServiceImpl.login()`, `User.clone()`
**Constitution:** P3 (Modern Security), P4 (Preserve Domain Model), R1 (BCrypt Risk)

The system SHALL securely handle passwords using BCrypt hashing.

**Behavior (derived from source code):**
1. User passwords are stored as BCrypt hashes (format: `$2a$10$...`)
2. Password verification uses `BCryptPasswordEncoder.matches()` (replacing custom `BCrypt.checkpw()`)
3. Passwords are NEVER returned in API responses (source: `User.clone()` omits password; target: `@JsonIgnore` or DTO)
4. `User.toString()` masks password as `*******`

**Acceptance Criteria:**
- [ ] Passwords stored as BCrypt hashes compatible with `$2a` format
- [ ] Spring Security `BCryptPasswordEncoder` is used (not custom BCrypt)
- [ ] No API response ever contains a password field
- [ ] Seed users (`davis/davis`, `homer/homer`) have BCrypt-hashed passwords
- [ ] Login succeeds with correct plaintext password against BCrypt hash

---

### 3.2 Item Management

#### REQ-006: List All Items

**Source:** `MainPresenter.loginHandler`, `MainPresenter.refreshClickHandler`, `ItemServiceImpl.findAll()`, `ItemDaoImpl.findAll()`, `MainView.setItems()`
**Constitution:** P1 (Functional Equivalence), P5 (API-First Design), BC3 (Item CRUD)

The system SHALL display all items in a table after login and on refresh.

**Behavior (derived from source code):**
1. After successful login (or session recovery), fetch all items via `GET /api/items`
2. Display items in a table with columns: Name (128px), Description (192px), Date (256px), Checkbox (25px)
3. Items are rendered from the `ArrayList<Item>` returned by the server
4. Each row includes a checkbox for multi-selection (used by delete)

**Acceptance Criteria:**
- [ ] Items are automatically fetched after login
- [ ] Table displays: name, description, date, and selection checkbox per row
- [ ] All items from the repository are returned
- [ ] Empty state is handled gracefully (no items)
- [ ] Date format is human-readable (source used `Date.toString()`)

---

#### REQ-007: Create Item

**Source:** `MainPresenter.newClickHandler`, `ItemPresenter.saveHandler`, `ItemCreateEvent`, `MainPresenter.createItemHandler`, `ItemServiceImpl.create()`, `ItemDaoImpl.create()`, `ItemView`
**Constitution:** P1 (Functional Equivalence), P4 (Preserve Domain Model), BC3 (Item CRUD)

The system SHALL allow creating a new item via a modal dialog.

**Behavior (derived from source code):**
1. User clicks the "New" button
2. A modal dialog appears with fields: Name (text), Description (text), Date (date picker)
3. Modal title: "Create/Edit an Item" (source: `ItemView.showPopUp()`)
4. For new items, `item.getId()` is 0 (or unset)
5. User fills in fields and clicks "Save"
6. If `item.getId() == 0`, the item is sent to `POST /api/items`
7. Backend assigns an auto-incrementing ID (`AtomicLong.incrementAndGet()`)
8. The item list is refreshed after successful creation
9. User can click "Cancel" to dismiss the dialog without saving

**Acceptance Criteria:**
- [ ] "New" button opens a modal dialog
- [ ] Dialog has Name, Description, and Date fields
- [ ] Save sends a POST request with the item data (no ID)
- [ ] Server assigns a unique auto-incrementing ID
- [ ] Item list refreshes automatically after creation
- [ ] Cancel dismisses dialog without side effects
- [ ] Modal has glass overlay (source: `dialogBox.setGlassEnabled(true)`)

---

#### REQ-008: Update Item

**Source:** `MainPresenter.onSelectedItem()`, `ItemPresenter.saveHandler`, `ItemUpdateEvent`, `MainPresenter.updateItemHandler`, `ItemServiceImpl.update()`, `ItemDaoImpl.update()`
**Constitution:** P1 (Functional Equivalence), P4 (Preserve Domain Model), BC3 (Item CRUD)

The system SHALL allow editing an existing item via the same modal dialog.

**Behavior (derived from source code):**
1. User clicks on a row in the item table (not the checkbox column — source: `cell.getCellIndex() != 3`)
2. The clicked row is visually highlighted (`selectionStyle.selectedRow()`)
3. The modal dialog opens with the item's current values pre-populated
4. For existing items, `item.getId() > 0`
5. User modifies fields and clicks "Save"
6. If `item.getId() > 0`, the item is sent to `PUT /api/items/{id}`
7. Backend updates the item in the repository (only if ID exists: `map.containsKey(item.getId())`)
8. The item list is refreshed after successful update

**Acceptance Criteria:**
- [ ] Clicking a table row (not checkbox) opens the edit dialog
- [ ] Clicked row is visually highlighted
- [ ] Dialog is pre-populated with the item's current name, description, and date
- [ ] Save sends a PUT request with the updated item data
- [ ] Update is a no-op if the item ID doesn't exist (matching `ItemDaoImpl.update()`)
- [ ] Item list refreshes automatically after update

---

#### REQ-009: Delete Items (Batch)

**Source:** `MainPresenter.deleteClickHandler`, `ItemServiceImpl.delete()`, `ItemDaoImpl.delete()`, `MainView.getSelectedItems()`
**Constitution:** P1 (Functional Equivalence), BC3 (Item CRUD)

The system SHALL allow batch deletion of selected items.

**Behavior (derived from source code):**
1. Each row in the item table has a checkbox
2. User selects one or more items via checkboxes
3. User clicks the "Delete" button
4. Only items with checked checkboxes are included (source: `getSelectedItems()` iterates rows, checks `CheckBox.getValue()`)
5. If no items are selected (`list.size() == 0`), the delete is silently skipped (no API call)
6. Selected items are sent to `DELETE /api/items` with their IDs
7. Backend removes each item from the repository by ID
8. The item list is refreshed after successful deletion

**Acceptance Criteria:**
- [ ] Checkboxes are rendered per row
- [ ] Delete button only fires when at least one item is selected
- [ ] Batch delete sends all selected item IDs in a single request
- [ ] Items are removed from the repository
- [ ] Item list refreshes automatically after deletion
- [ ] Null/empty item lists are handled gracefully (source: `ItemDaoImpl.delete()` checks for null/empty)

---

#### REQ-010: Refresh Items

**Source:** `MainPresenter.refreshClickHandler`
**Constitution:** P1 (Functional Equivalence)

The system SHALL allow manually refreshing the item list.

**Behavior (derived from source code):**
1. User clicks the "Refresh" button
2. A `GET /api/items` call is made
3. The table is re-populated with the latest items from the server

**Acceptance Criteria:**
- [ ] Refresh button triggers a fresh fetch of all items
- [ ] Table is updated with latest server data
- [ ] Any previous selection state is cleared on refresh

---

### 3.3 Navigation and Routing

#### REQ-011: Client-Side Routing

**Source:** `AppController`, `History`, `ValueChangeHandler`
**Constitution:** P1 (Functional Equivalence), BC5 (Navigation)

The system SHALL provide client-side routing between login and main views.

**Behavior (derived from source code):**
1. Two primary routes: `/login` and `/main` (source: `History.newItem("login")`, `History.newItem("main")`)
2. Default route (empty path) redirects to `/login`
3. Login success navigates to `/main`
4. Session timeout navigates to `/login`
5. Direct URL access to `/main` triggers session validation before showing content

**Acceptance Criteria:**
- [ ] `/login` route renders the login page
- [ ] `/main` route renders the main (items) page (protected)
- [ ] Root path `/` redirects to `/login`
- [ ] Navigating directly to `/main` validates session first
- [ ] Browser back/forward works correctly with routes

---

#### REQ-012: Protected Routes

**Source:** `AppController.navigateToMain()`, `AppController.callback`
**Constitution:** P1 (Functional Equivalence), P3 (Modern Security), BC5 (Navigation)

The system SHALL prevent unauthenticated access to protected pages.

**Behavior (derived from source code):**
1. The main view (item management) requires an authenticated session
2. If a user navigates to `/main` without a valid session, they are redirected to `/login`
3. Source implementation: `AppController.navigateToMain()` checks cookie `sid`, calls `isLoggedIn()`, and on failure redirects to login

**Acceptance Criteria:**
- [ ] Unauthenticated users cannot access the main page
- [ ] Attempting to access `/main` without auth redirects to `/login`
- [ ] After login, user is taken to the main page

---

### 3.4 UI Components

#### REQ-013: Login Page UI

**Source:** `LoginView.java`, `LoginView.ui.xml`
**Constitution:** P1 (Functional Equivalence)

The system SHALL render a login form with specific layout and components.

**Behavior (derived from source code):**
1. A centered panel containing: username text input, password text input, login button, error label
2. Username input is a standard text box
3. Password input is a password-masked text box
4. Error label displays validation and server error messages
5. Error label is cleared when a new login attempt begins (source: `display.setErrorMsg(null)` at start of `doLogin()`)
6. Error label is cleared on any key-down event (source: `keyDownHandler` sets error to null)

**Acceptance Criteria:**
- [ ] Login form has username, password, and submit button
- [ ] Password field masks input
- [ ] Error messages are displayed below the form
- [ ] Error messages clear on new input or submission attempt

---

#### REQ-014: Main Page UI

**Source:** `MainView.java`, `MainView.ui.xml`
**Constitution:** P1 (Functional Equivalence)

The system SHALL render the main page with a header, item table, and action buttons.

**Behavior (derived from source code):**
1. **Header area:** Welcome text showing username (`setNameText()`), logout hyperlink
2. **Center area:** Item data table with header row (Name, Description, Date, checkbox) and data rows
3. **Footer/action area:** Refresh button, Delete button, New button, error label
4. Row click (on non-checkbox cells) selects the row and highlights it
5. Selected row is styled differently (`SelectionStyle.selectedRow()`)

**Acceptance Criteria:**
- [ ] Welcome text displays the logged-in username
- [ ] Logout link is in the header area
- [ ] Item table has column headers: Name, Description, Date, and a checkbox column
- [ ] Row click highlights the row and opens item edit dialog
- [ ] Checkbox click does NOT trigger row selection (source: `cell.getCellIndex() != 3`)
- [ ] Action buttons (Refresh, Delete, New) are present
- [ ] Error messages display in the footer area

---

#### REQ-015: Item Dialog UI (Create/Edit Modal)

**Source:** `ItemView.java`
**Constitution:** P1 (Functional Equivalence)

The system SHALL render a modal dialog for creating and editing items.

**Behavior (derived from source code):**
1. Modal dialog box with title "Create/Edit an Item"
2. Fields: Name (text input), Description (text input), Date (date picker)
3. Buttons: Save, Cancel
4. Glass overlay enabled (dims background — source: `dialogBox.setGlassEnabled(true)`)
5. Animation enabled (source: `dialogBox.setAnimationEnabled(true)`)
6. Dialog is centered on screen (source: `dialogBox.center()`)
7. For edit mode: fields pre-populated from the selected item
8. For create mode: fields are empty (new `Item()` with default date)

**Acceptance Criteria:**
- [ ] Modal appears centered with a backdrop/overlay
- [ ] Name, Description, and Date fields are present
- [ ] Save and Cancel buttons are present
- [ ] Edit mode pre-populates all fields
- [ ] Create mode shows empty/default fields
- [ ] Cancel closes the modal without saving

---

### 3.5 Error Handling

#### REQ-016: Structured Error Responses

**Source:** `LoginFailureException`, `SessionTimedOutException`, `ItemServiceException`, `RpcController`
**Constitution:** P1 (Functional Equivalence), AC3 (Integration Constraints)

The system SHALL return structured JSON error responses for all error conditions.

**Error Mapping (derived from source exceptions):**

| Source Exception | HTTP Status | Error Code | Default Message |
|---|---|---|---|
| `LoginFailureException` | 401 | `AUTH_FAILED` | "The email or password you entered is incorrect." |
| `LoginFailureException` (custom msg) | 401 | `AUTH_FAILED` | "Sorry, we couldn't locate you in our records." |
| `SessionTimedOutException` | 401 | `SESSION_EXPIRED` | "The session has timed out. You will need to login again." |
| `ItemServiceException` | 500 | `ITEM_SERVICE_ERROR` | "Oops, we're having problems with the server. Try again later." |

**Response Format:**
```json
{
  "timestamp": "ISO-8601",
  "status": 401,
  "error": "Unauthorized",
  "message": "The email or password you entered is incorrect.",
  "path": "/api/auth/login"
}
```

**Acceptance Criteria:**
- [ ] All errors return structured JSON with timestamp, status, error, message, and path
- [ ] `@ControllerAdvice` / `GlobalExceptionHandler` handles all exception types
- [ ] Error messages match the source application's default messages
- [ ] 401 errors trigger client-side session cleanup and redirect (for `SESSION_EXPIRED`)
- [ ] 500 errors display the error message to the user (source: `display.setErrorMsg(throwable.getMessage())`)

---

#### REQ-017: Client-Side Error Display

**Source:** `LoginPresenter.callback.onFailure()`, `MainPresenter.handleThrowable()`, `MainPresenter.findCallback.onFailure()`
**Constitution:** P1 (Functional Equivalence)

The system SHALL display error messages to the user in the appropriate UI location.

**Behavior (derived from source code):**
1. Login errors → displayed in the login form error label
2. Item operation errors (non-session) → displayed in the main page error label
3. Session timeout errors → trigger redirect to login (not displayed as text)

**Acceptance Criteria:**
- [ ] Login API errors display in the login page error area
- [ ] Item API errors (500) display in the main page error area
- [ ] Session expired errors (401) redirect to login instead of showing text

---

### 3.6 Data Model

#### REQ-018: Item Domain Model

**Source:** `Item.java`
**Constitution:** P4 (Preserve Domain Model)

The system SHALL maintain the Item domain model with the following structure:

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `long` | Auto-assigned by server | `AtomicLong.incrementAndGet()` on create |
| `name` | `String` | — | |
| `description` | `String` | — | |
| `date` | `Date` / `ISO-8601` | Default: current date | `new Date()` as default in source |

**Behavioral Contracts:**
- `equals()` and `hashCode()` include all fields (id, name, description, date)
- Server assigns ID on create (client sends item without ID)
- Update is keyed on ID; items not found are silently ignored

**Acceptance Criteria:**
- [ ] Backend `Item` entity has id (long), name (String), description (String), date (temporal type)
- [ ] Frontend `Item` TypeScript interface matches the JSON schema
- [ ] ID is server-assigned on creation
- [ ] JSON serialization uses ISO-8601 for dates

---

#### REQ-019: User Domain Model

**Source:** `User.java`
**Constitution:** P4 (Preserve Domain Model), P3 (Modern Security)

The system SHALL maintain the User domain model with the following structure:

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `username` | `String` | Required (non-null, non-empty) | Used as unique identifier |
| `password` | `String` | Required for login; never serialized in responses | `@JsonIgnore` on output |

**Behavioral Contracts:**
- `isValid()`: returns `true` only when both username and password are non-null and non-empty
- Password MUST never appear in API responses (source: `User.clone()` explicitly omits password)
- `toString()` masks password as `*******`
- `equals()` and `hashCode()` include both username and password

**Acceptance Criteria:**
- [ ] Backend `User` entity has username and password fields
- [ ] Password is excluded from all API responses (`@JsonIgnore` or DTO)
- [ ] `isValid()` logic is preserved in input validation
- [ ] Frontend `User` TypeScript interface has only `username` (no password)

---

#### REQ-020: Seed Data

**Source:** `UserDaoImpl`, `ItemDaoImpl`
**Constitution:** P1 (Functional Equivalence), R3 (In-Memory DAO Risk)

The system SHALL initialize with the same seed data as the source application.

**Users:**
| Username | Password (plaintext) | Password (stored) |
|---|---|---|
| `davis` | `davis` | BCrypt hash |
| `homer` | `homer` | BCrypt hash |

**Items:** 9 seed items with auto-generated IDs (1–9):
- Item 1, Item 2, Foo, Bar, Baz, Widget, FooBar, BarFoo, FooBaz
- Each with a description and random date

**Acceptance Criteria:**
- [ ] Application starts with 2 users (`davis`, `homer`) with BCrypt-hashed passwords
- [ ] Application starts with 9 items matching the source seed data names
- [ ] `davis` can log in with password `davis`
- [ ] `homer` can log in with password `homer`

---

### 3.7 API Contract

#### REQ-021: Authentication REST API

**Source:** `UserService.java`, knowledge graph `api-contract.md`
**Constitution:** P5 (API-First Design), AC3 (Integration Constraints)

The system SHALL expose the following authentication endpoints:

**POST /api/auth/login**
- Request: `{ "username": "string", "password": "string" }`
- Response (200): `{ "token": "string", "user": { "username": "string" } }`
- Response (401): Structured error (see REQ-016)
- Source mapping: `UserService.login(User) → String sessionId`

**POST /api/auth/logout**
- Request: `Authorization: Bearer <token>` header
- Response: `204 No Content`
- Source mapping: `UserService.logout(String sessionId)`

**GET /api/auth/me**
- Request: `Authorization: Bearer <token>` header
- Response (200): `{ "username": "string" }`
- Response (401): Structured error
- Source mapping: `UserService.isLoggedIn(String sessionId) → User`

**Acceptance Criteria:**
- [ ] All three endpoints are implemented and return the specified response formats
- [ ] Login returns a token that can be used for subsequent requests
- [ ] Logout invalidates the token
- [ ] `/api/auth/me` validates the token and returns the associated user

---

#### REQ-022: Items REST API

**Source:** `ItemService.java`, knowledge graph `api-contract.md`
**Constitution:** P5 (API-First Design), AC3 (Integration Constraints)

The system SHALL expose the following item management endpoints:

**GET /api/items**
- Request: `Authorization: Bearer <token>` header
- Response (200): `[{ "id": number, "name": "string", "description": "string", "date": "ISO-8601" }, ...]`
- Source mapping: `ItemService.findAll(String sessionId) → ArrayList<Item>`

**POST /api/items**
- Request: `Authorization: Bearer <token>`, Body: `{ "name": "string", "description": "string", "date": "ISO-8601" }`
- Response (201): Created item with server-assigned ID
- Source mapping: `ItemService.create(String sessionId, Item item)`
- Note: Source returned `void`; target returns the created item with ID for REST semantics

**PUT /api/items/{id}**
- Request: `Authorization: Bearer <token>`, Body: `{ "name": "string", "description": "string", "date": "ISO-8601" }`
- Response (200): Updated item
- Source mapping: `ItemService.update(String sessionId, Item item)`
- Note: Source passed ID in body; target uses URL path parameter

**DELETE /api/items**
- Request: `Authorization: Bearer <token>`, Body: `{ "ids": [number, ...] }`
- Response: `204 No Content`
- Source mapping: `ItemService.delete(String sessionId, ArrayList<Item> items)`
- Note: Source passed full item objects; target passes only IDs

**All item endpoints require authentication** (source: `RpcController.validateSession()` for all non-UserService calls)

**Acceptance Criteria:**
- [ ] All four CRUD endpoints are implemented
- [ ] All item endpoints require valid authentication
- [ ] Unauthenticated requests return 401
- [ ] POST returns 201 with the created item including server-assigned ID
- [ ] PUT updates only existing items (no-op for non-existent IDs)
- [ ] DELETE accepts an array of IDs for batch deletion

---

#### REQ-023: CORS Configuration

**Source:** Not in source (frontend/backend were same deployable); required by new architecture
**Constitution:** AC3 (Integration Constraints), P2 (Clean Separation)

The system SHALL configure CORS to allow the React frontend to communicate with the Spring Boot backend.

**Acceptance Criteria:**
- [ ] CORS allows the React dev server origin (e.g., `http://localhost:3000` or `http://localhost:5173`)
- [ ] Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- [ ] Allowed headers: Authorization, Content-Type
- [ ] Credentials are allowed (`allow-credentials: true`)
- [ ] CORS configuration is externalizable via `application.properties`

---

## 4. Non-Functional Requirements

#### REQ-024: Technology Stack Compliance

**Constitution:** AC1 (Backend Constraints), AC2 (Frontend Constraints)

The system SHALL use exclusively the target technology stack defined in the constitution.

**Backend:**
- Java 17+, Spring Boot 3.x, Maven, Spring Security with BCrypt
- SLF4J + Logback (replacing Log4j 1.2)
- JUnit 5 + Mockito (replacing JUnit 4.4 + EasyMock)
- JAR packaging with embedded server (replacing WAR + JBoss/Jetty)

**Frontend:**
- TypeScript, React 18+, Vite
- React Router (replacing GWT History/ValueChangeHandler)
- Jest + React Testing Library

**Acceptance Criteria:**
- [ ] No GWT dependencies remain in any build file
- [ ] No Spring 2.5.6 dependencies remain
- [ ] No Log4j 1.2 dependencies remain
- [ ] No JUnit 4 or EasyMock dependencies remain
- [ ] No `web.xml`, `applicationContext.xml`, or `dispatcher-servlet.xml` configuration
- [ ] Backend builds as an executable JAR
- [ ] Frontend builds as static files

---

#### REQ-025: Complete Technology Removal

**Constitution:** Section 4 (Technology Removal Checklist)

The system SHALL contain ZERO references to the following removed technologies:

| Category | Removed Items |
|---|---|
| GWT SDK | `gwt-user`, `gwt-servlet`, `IsSerializable`, `RemoteService`, `RemoteServiceRelativePath` |
| GWT UI | `Composite`, `UiBinder`, `Widget`, `FlexTable`, `DialogBox`, `RootLayoutPanel` |
| GWT RPC | `AsyncCallback`, `RPC`, `RPCRequest`, `RemoteServiceServlet` |
| GWT Events | `GwtEvent`, `HandlerManager`, `EventHandler`, `ValueChangeHandler` |
| GWT Navigation | `History`, `History.newItem()`, `History.getToken()` |
| GWT Cookies | `com.google.gwt.user.client.Cookies` |
| Spring 2.5.6 | XML bean definitions, `ContextLoaderListener`, `DispatcherServlet` (XML-configured) |
| Servlet API (direct) | `HttpSession` injection, manual session management |
| App Server Config | `jboss-web.xml`, `urlrewrite.xml`, WAR plugin, JBoss/Jetty deployment descriptors |
| Build Plugins | `gwt-maven-plugin`, Cargo Maven plugin |

**Acceptance Criteria:**
- [ ] `grep -r` for any GWT import returns zero results
- [ ] No XML Spring configuration files exist
- [ ] No `web.xml` exists
- [ ] pom.xml contains no GWT, EasyMock, or Log4j 1.x dependencies

---

#### REQ-026: Test Coverage

**Constitution:** P6 (Incremental Testability)

The system SHALL have test coverage equivalent to or exceeding the source application.

**Source Test Coverage Map:**
| Source Test | Equivalent Target Test |
|---|---|
| `UserTest.java` | Backend: `UserTest.java` (JUnit 5) |
| `ItemTest.java` | Backend: `ItemTest.java` (JUnit 5) |
| `UserServiceImplTest.java` | Backend: `UserServiceTest.java` (JUnit 5 + Mockito) |
| `ItemServiceImplTest.java` | Backend: `ItemServiceTest.java` (JUnit 5 + Mockito) |
| `AbstractServiceTest.java` | Backend: Spring Security integration tests |
| `TestBCrypt.java` | Backend: Covered by Spring Security tests |
| `RpcControllerTest.java` | Backend: `AuthControllerTest.java` + `ItemControllerTest.java` (`@WebMvcTest`) |
| `LoginPresenterTest.java` | Frontend: `LoginPage.test.tsx` (Jest + RTL) |
| `MainPresenterTest.java` | Frontend: `MainPage.test.tsx` (Jest + RTL) |
| `ItemPresenterTest.java` | Frontend: `ItemDialog.test.tsx` (Jest + RTL) |

**Acceptance Criteria:**
- [ ] Every backend service has unit tests with mocked dependencies
- [ ] Every backend controller has `@WebMvcTest` integration tests
- [ ] Domain model tests exist for validation logic
- [ ] Frontend pages/components have React Testing Library tests
- [ ] All tests pass in CI-compatible mode (no browser required for backend)

---

#### REQ-027: Independent Build and Deployment

**Constitution:** P2 (Clean Separation of Concerns), AC1–AC2 (Constraints)

The system SHALL support independent building and running of frontend and backend.

**Acceptance Criteria:**
- [ ] `cd backend && mvn clean package` builds the backend independently
- [ ] `cd frontend && npm install && npm run build` builds the frontend independently
- [ ] Backend runs standalone: `java -jar backend/target/*.jar`
- [ ] Frontend runs standalone: `npm run dev` (dev server) or served as static files
- [ ] No circular or cross-project build dependencies

---

#### REQ-028: Logging

**Source:** `log4j.properties`, `Logger.getLogger()` calls throughout server code
**Constitution:** AC1 (Backend Constraints)

The system SHALL use SLF4J + Logback for all logging, replacing Log4j 1.2.

**Acceptance Criteria:**
- [ ] All logging uses SLF4J API (`LoggerFactory.getLogger()`)
- [ ] Debug level for `com.example` package (matching source `log4j.properties`)
- [ ] Info level for root logger
- [ ] Console appender configured
- [ ] Logging configuration in `application.properties` or `logback.xml`

---

#### REQ-029: Session Configuration

**Source:** `web.xml` (`<session-timeout>20</session-timeout>`)
**Constitution:** P1 (Functional Equivalence)

The system SHALL configure session timeout matching the source application.

**Acceptance Criteria:**
- [ ] Session/token timeout is 20 minutes (configurable via `application.properties`)
- [ ] Expired sessions return 401 on subsequent API calls
- [ ] Session cleanup occurs automatically (matching `HttpSessionBindingListener.valueUnbound()` behavior)

---

#### REQ-030: XSRF/CSRF Protection

**Source:** `AbstractService.isSessionValid()` — XSRF check comparing `httpSession.getId()` with passed `sessionId`
**Constitution:** P3 (Modern Security)

The system SHALL provide CSRF protection equivalent to or better than the source application.

**Behavior (derived from source code):**
1. Source: `isSessionValid()` performs a dual check — sessionMap contains sessionId AND `httpSession.getId().equals(sessionId)`
2. This is a basic XSRF protection ensuring the session parameter matches the actual HTTP session
3. Target: Spring Security CSRF protection or stateless token-based auth (JWT) which is inherently CSRF-safe

**Acceptance Criteria:**
- [ ] If using session-based auth: Spring Security CSRF protection is enabled
- [ ] If using JWT: Tokens are validated on every request, making CSRF protection implicit
- [ ] No endpoint is accessible without proper authentication (except login)

---

## 5. Traceability Matrix

### 5.1 Source File → Requirement Mapping

| Source File | Requirements |
|---|---|
| `client/Application.java` | REQ-011, REQ-024 |
| `client/AppController.java` | REQ-003, REQ-004, REQ-011, REQ-012 |
| `client/presenter/LoginPresenter.java` | REQ-001, REQ-013, REQ-017 |
| `client/presenter/MainPresenter.java` | REQ-002, REQ-004, REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-014, REQ-017 |
| `client/presenter/ItemPresenter.java` | REQ-007, REQ-008, REQ-015 |
| `client/view/LoginView.java` | REQ-001, REQ-013 |
| `client/view/MainView.java` | REQ-006, REQ-008, REQ-009, REQ-014 |
| `client/view/ItemView.java` | REQ-007, REQ-008, REQ-015 |
| `client/model/Item.java` | REQ-018 |
| `client/model/User.java` | REQ-019, REQ-005 |
| `client/service/UserService.java` | REQ-021 |
| `client/service/ItemService.java` | REQ-022 |
| `client/event/LoginEvent.java` | REQ-001, REQ-003 |
| `client/event/SessionTimedOutEvent.java` | REQ-004 |
| `client/event/ItemCreateEvent.java` | REQ-007 |
| `client/event/ItemUpdateEvent.java` | REQ-008 |
| `client/exception/LoginFailureException.java` | REQ-016 |
| `client/exception/SessionTimedOutException.java` | REQ-004, REQ-016 |
| `client/exception/ItemServiceException.java` | REQ-016 |
| `client/cookies/Cookies.java` | REQ-001, REQ-003 |
| `server/controller/RpcController.java` | REQ-004, REQ-021, REQ-022, REQ-030 |
| `server/service/UserServiceImpl.java` | REQ-001, REQ-002, REQ-003, REQ-005 |
| `server/service/ItemServiceImpl.java` | REQ-006, REQ-007, REQ-008, REQ-009 |
| `server/service/AbstractService.java` | REQ-004, REQ-029, REQ-030 |
| `server/dao/UserDaoImpl.java` | REQ-005, REQ-020 |
| `server/dao/ItemDaoImpl.java` | REQ-006, REQ-007, REQ-008, REQ-009, REQ-018, REQ-020 |
| `server/security/BCrypt.java` | REQ-005 |
| `conf/applicationContext.xml` | REQ-024, REQ-025 |
| `war/WEB-INF/web.xml` | REQ-024, REQ-025, REQ-029 |
| `war/WEB-INF/dispatcher-servlet.xml` | REQ-024, REQ-025 |
| `conf/log4j.properties` | REQ-028 |

### 5.2 Constitution Principle → Requirement Mapping

| Principle | Requirements |
|---|---|
| **P1:** Functional Equivalence | REQ-001 through REQ-020, REQ-029 |
| **P2:** Clean Separation | REQ-023, REQ-027 |
| **P3:** Modern Security | REQ-001, REQ-002, REQ-004, REQ-005, REQ-012, REQ-030 |
| **P4:** Preserve Domain Model | REQ-007, REQ-008, REQ-018, REQ-019 |
| **P5:** API-First Design | REQ-021, REQ-022 |
| **P6:** Incremental Testability | REQ-026 |
| **AC1:** Backend Constraints | REQ-024, REQ-028 |
| **AC2:** Frontend Constraints | REQ-024 |
| **AC3:** Integration Constraints | REQ-016, REQ-021, REQ-022, REQ-023 |

### 5.3 Behavioral Contract → Requirement Mapping

| Contract | Requirements |
|---|---|
| **BC1:** Authentication Flow | REQ-001, REQ-003, REQ-005, REQ-021 |
| **BC2:** Session Validation | REQ-004, REQ-030 |
| **BC3:** Item CRUD Operations | REQ-006, REQ-007, REQ-008, REQ-009, REQ-022 |
| **BC4:** Session Timeout Handling | REQ-004, REQ-017 |
| **BC5:** Navigation | REQ-011, REQ-012 |

---

## 6. Risk Considerations

| ID | Risk | Related Reqs | Mitigation |
|---|---|---|---|
| R1 | BCrypt salt round incompatibility | REQ-005, REQ-020 | Test login with seed users' existing BCrypt hashes; verify `$2a` format and 10-round default match |
| R2 | In-memory session not production-grade | REQ-004, REQ-029 | Document as known; Spring Security session management is an improvement |
| R3 | Data loss on restart (in-memory DAO) | REQ-020 | Preserve source behavior; document for future DB migration |
| R4 | Date serialization differences | REQ-018 | Source uses `Date.toString()` display; target uses ISO-8601; verify frontend formatting |
| R5 | EventBus decoupling loss | REQ-011, REQ-017 | Map each event to React Context state change or callback prop |
| R6 | Cookie-based `sid` to token migration | REQ-001, REQ-003 | New token storage mechanism (localStorage or httpOnly cookie) must support session recovery |

---

## 7. Glossary

| Term | Definition |
|---|---|
| **GWT** | Google Web Toolkit — the source UI framework |
| **GWT-RPC** | GWT Remote Procedure Call — the source communication protocol |
| **MVP** | Model-View-Presenter — the source client-side architecture pattern |
| **EventBus** | Observer pattern implementation for decoupled communication in GWT |
| **SPA** | Single Page Application — the target frontend architecture |
| **JWT** | JSON Web Token — potential authentication token format |
| **DTO** | Data Transfer Object — used for API request/response bodies |
| **BCrypt** | Password hashing algorithm used in both source and target |
| **CSRF/XSRF** | Cross-Site Request Forgery — attack vector mitigated by session validation |
