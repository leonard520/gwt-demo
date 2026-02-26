# Project Constitution: GWT-to-React+SpringBoot Rewrite

> **Generated:** Auto-generated during Foundation Phase
> **Mode:** Rewrite
> **Source:** GWT 2.0 / Java 1.6 / Spring 2.5.6
> **Target:** React (TypeScript) + Spring Boot 3.x (Java 17+)

---

## 1. Project Identity

| Attribute | Value |
|---|---|
| **Project Name** | gwt-demo |
| **Migration Mode** | Rewrite (full re-architecture) |
| **Source Stack** | Java 1.6, GWT 2.0, Spring 2.5.6, Log4j 1.2, JUnit 4.4 |
| **Target Stack** | Java 17+, Spring Boot 3.x, React 18+ (TypeScript), REST API (JSON), SLF4J/Logback, JUnit 5 |
| **Package Base** | `com.example` |

---

## 2. Guiding Principles

### P1: Functional Equivalence
All existing business logic and user-facing behavior MUST be preserved in the rewritten application. Every GWT-RPC service operation must have an equivalent REST API endpoint. Every client-side interaction flow must be replicated in the React frontend.

**Applies to:** All components
**Verification:** Side-by-side functional testing of login, CRUD operations, session management, and error handling flows.

### P2: Clean Separation of Concerns
The rewritten system MUST be fully decoupled into two independent deployable units:
- **Backend:** Spring Boot REST API (no UI rendering, no GWT dependencies)
- **Frontend:** React SPA (no Java dependencies, communicates solely via REST API)

**Applies to:** Architecture, build system, deployment
**Verification:** Backend builds and runs independently; frontend builds and runs independently; they communicate only via documented REST endpoints.

### P3: Modern Security Practices
- BCrypt password hashing MUST be retained (migrate to Spring Security's `BCryptPasswordEncoder`)
- Session management MUST migrate from servlet HTTP session + cookie-based `sid` to stateless JWT or Spring Security session management
- XSRF protection currently in `RpcController.validateSession()` MUST be replaced with Spring Security CSRF protection or stateless token-based auth
- Passwords MUST never be returned in API responses (currently enforced via `User.clone()`)

**Applies to:** Authentication, authorization, API security
**Verification:** Security review of authentication flow, token handling, and password storage.

### P4: Preserve Domain Model Semantics
The core domain models (`Item`, `User`) and their validation rules MUST be preserved:
- `User.isValid()` — username and password non-null/non-empty
- `Item` fields: id (long), name (String), description (String), date (Date)
- `User.clone()` behavior — never expose password in responses

**Applies to:** Domain model, DTOs, API contracts
**Verification:** Unit tests for model validation; API contract tests.

### P5: API-First Design
All communication between frontend and backend MUST use well-defined REST APIs with JSON payloads. The API contract replaces the GWT-RPC interface definitions (`UserService`, `ItemService`, their Async variants).

**Applies to:** API layer, service interfaces
**Verification:** OpenAPI/Swagger documentation; integration tests for all endpoints.

### P6: Incremental Testability
Every component MUST be independently testable. The existing test patterns (unit tests with mocks for presenters, service tests, DAO tests, BCrypt tests) inform the minimum test coverage expected in the rewrite.

**Applies to:** All components
**Verification:** Test coverage must meet or exceed original coverage for equivalent logic.

---

## 3. Architectural Constraints

### AC1: Backend Constraints
| Constraint | Requirement |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Build Tool | Maven (preserving existing Maven-based build) |
| API Protocol | REST (JSON) replacing GWT-RPC |
| Security | Spring Security with BCrypt |
| Logging | SLF4J + Logback (replacing Log4j 1.2) |
| Testing | JUnit 5 + Mockito (replacing JUnit 4.4 + EasyMock) |
| Packaging | JAR with embedded server (replacing WAR + JBoss/Jetty) |

### AC2: Frontend Constraints
| Constraint | Requirement |
|---|---|
| Language | TypeScript |
| Framework | React 18+ |
| State Management | React Context or equivalent (replacing GWT EventBus) |
| Routing | React Router (replacing GWT History/ValueChangeHandler) |
| HTTP Client | Fetch API or Axios (replacing GWT-RPC AsyncCallback) |
| Build Tool | Vite or Create React App |
| Testing | Jest + React Testing Library |

### AC3: Integration Constraints
| Constraint | Requirement |
|---|---|
| API Format | RESTful JSON endpoints |
| Authentication | Token-based (JWT) or session-based via Spring Security |
| CORS | Must be configured since frontend and backend are separate |
| Error Handling | Structured JSON error responses (replacing GWT serialized exceptions) |

---

## 4. Technology Removal Checklist

The following technologies/patterns from the source codebase MUST be completely eliminated:

| Remove | Replacement |
|---|---|
| GWT SDK (`gwt-user`, `gwt-servlet`) | React + REST API |
| GWT-RPC (`RemoteService`, `RemoteServiceRelativePath`, `AsyncCallback`) | REST controllers + Fetch/Axios |
| GWT UI (`Composite`, `UiBinder`, `Widget`, `FlexTable`, `DialogBox`) | React components (JSX/TSX) |
| GWT Event system (`GwtEvent`, `HandlerManager`, `EventHandler`) | React state management + events |
| GWT History (`History`, `ValueChangeHandler`) | React Router |
| GWT Cookies (`com.google.gwt.user.client.Cookies`) | Browser cookies / HTTP-only auth tokens |
| `IsSerializable` marker interface | Standard Java serialization or Jackson JSON |
| `RpcController` (GWT-RPC dispatcher) | Spring `@RestController` endpoints |
| Spring 2.5.6 XML config (`applicationContext.xml`, `dispatcher-servlet.xml`) | Spring Boot auto-configuration + annotations |
| `web.xml` servlet config | Spring Boot embedded server |
| Log4j 1.2 | SLF4J + Logback |
| JUnit 4.4 + EasyMock | JUnit 5 + Mockito |
| WAR packaging | JAR packaging (backend) + static build (frontend) |
| JBoss/Jetty deployment descriptors (`jboss-web.xml`) | Spring Boot embedded Tomcat |
| URL rewrite filter (`urlrewrite.xml`) | Spring Boot request mapping + React Router |
| GWT Maven plugin (`gwt-maven-plugin`) | Standard `spring-boot-maven-plugin` |

---

## 5. Behavioral Contracts to Preserve

### BC1: Authentication Flow
```
Current: LoginView → LoginPresenter → UserServiceAsync.login() → [GWT-RPC] → UserServiceImpl.login() → UserDao → BCrypt verify → return sessionId → set cookie "sid"
Target:  LoginPage → POST /api/auth/login (JSON) → AuthController → UserService → UserRepository → BCrypt verify → return JWT/session → store token
```

### BC2: Session Validation
```
Current: RpcController.validateSession() checks sessionId param against AbstractService.sessionMap + HttpSession XSRF check
Target:  Spring Security filter chain validates JWT/session token on every protected request
```

### BC3: Item CRUD Operations
```
Current: MainPresenter → ItemServiceAsync.[findAll|create|update|delete]() → [GWT-RPC] → ItemServiceImpl → ItemDao
Target:  React components → GET/POST/PUT/DELETE /api/items → ItemController → ItemService → ItemRepository
```

### BC4: Session Timeout Handling
```
Current: SessionTimedOutException → client catches → fires SessionTimedOutEvent → AppController navigates to login
Target:  401 Unauthorized response → React interceptor → redirect to login page
```

### BC5: Navigation
```
Current: AppController listens to History changes + LoginEvent/SessionTimedOutEvent → switches presenters
Target:  React Router with protected routes + auth context → conditional rendering
```

---

## 6. Risk Register

| ID | Risk | Severity | Mitigation |
|---|---|---|---|
| R1 | BCrypt salt rounds may differ between custom BCrypt.java and Spring Security BCryptPasswordEncoder | High | Verify default salt rounds match (both use $2a, 10 rounds default). Test with existing hashed passwords. |
| R2 | In-memory session map (`AbstractService.sessionMap`) is not production-grade | Medium | Replace with Spring Security session store or JWT (stateless). This is an improvement, not a regression. |
| R3 | In-memory DAO implementations lose data on restart | Low | Document as known behavior from source. Plan for real database in future phase. |
| R4 | GWT's `IsSerializable` models had specific serialization constraints | Medium | Ensure Jackson JSON serialization covers all fields. Add `@JsonIgnore` for password field on User. |
| R5 | EventBus decoupling pattern may be lost in React rewrite | Medium | Map each GWT event to equivalent React state management pattern (Context, callbacks, or state library). |

---

## 7. Definition of Done

A component is considered "done" when:

- [ ] All business logic from the source is implemented with equivalent behavior
- [ ] Unit tests exist covering the same scenarios as the original test suite
- [ ] No GWT, Spring 2.5.6, or Java 1.6 specific code remains
- [ ] API endpoints are documented
- [ ] Frontend components render equivalent UI to original GWT views
- [ ] Security requirements (BCrypt, session validation, XSRF protection) are met
- [ ] The component builds and passes all tests independently
- [ ] Code review confirms no regressions in behavioral contracts (Section 5)

---

## 8. File Mapping Guide

| Source (GWT) | Target (React + Spring Boot) |
|---|---|
| `client/Application.java` (EntryPoint) | `frontend/src/App.tsx` (React root) |
| `client/AppController.java` | `frontend/src/App.tsx` + React Router config |
| `client/presenter/LoginPresenter.java` | `frontend/src/pages/LoginPage.tsx` + hooks |
| `client/presenter/MainPresenter.java` | `frontend/src/pages/MainPage.tsx` + hooks |
| `client/presenter/ItemPresenter.java` | `frontend/src/components/ItemDialog.tsx` |
| `client/view/LoginView.java` + `LoginView.ui.xml` | `frontend/src/pages/LoginPage.tsx` |
| `client/view/MainView.java` + `MainView.ui.xml` | `frontend/src/pages/MainPage.tsx` |
| `client/view/ItemView.java` | `frontend/src/components/ItemDialog.tsx` |
| `client/model/User.java` | `backend: User.java` entity + `frontend: types.ts` |
| `client/model/Item.java` | `backend: Item.java` entity + `frontend: types.ts` |
| `client/service/UserService.java` | `backend: AuthController.java` (@RestController) |
| `client/service/ItemService.java` | `backend: ItemController.java` (@RestController) |
| `client/event/*` | `frontend/src/context/AuthContext.tsx` + React state |
| `client/cookies/*` | Browser localStorage/cookies via auth utilities |
| `client/exception/*` | `backend: exception/` package + `@ControllerAdvice` |
| `server/controller/RpcController.java` | Eliminated — replaced by `@RestController` classes |
| `server/service/UserServiceImpl.java` | `backend: service/UserService.java` |
| `server/service/ItemServiceImpl.java` | `backend: service/ItemService.java` |
| `server/service/AbstractService.java` | Spring Security filter chain |
| `server/dao/UserDao.java` + `UserDaoImpl.java` | `backend: repository/UserRepository.java` |
| `server/dao/ItemDao.java` + `ItemDaoImpl.java` | `backend: repository/ItemRepository.java` |
| `server/security/BCrypt.java` | Spring Security `BCryptPasswordEncoder` |
| `conf/applicationContext.xml` | Spring Boot `@Configuration` auto-config |
| `war/WEB-INF/dispatcher-servlet.xml` | Spring Boot `@RestController` auto-discovery |
| `war/WEB-INF/web.xml` | `application.properties` / `application.yml` |
| `war/WEB-INF/urlrewrite.xml` | React Router + Spring Boot request mappings |
| `Application.gwt.xml` | Eliminated — no GWT module descriptor needed |
| `pom.xml` (GWT build) | `backend/pom.xml` (Spring Boot) + `frontend/package.json` |
