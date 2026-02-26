# Implementation Plan: GWT-to-React+SpringBoot Rewrite

**Branch**: `gwt-to-react-springboot` | **Date**: 2025-01-30 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `.github/appmod/gwt-to-react-springboot/spec.md`
**Mode**: Rewrite (Full Re-architecture)
**Constitution**: `.github/appmod/constitution.md`

---

## Summary

Rewrite the GWT 2.0 / Spring 2.5.6 / Java 1.6 monolithic web application as two fully decoupled deployable units: a **Spring Boot 3.x REST API backend** (Java 17+) and a **React 18+ SPA frontend** (TypeScript). The rewrite preserves all existing business logic and user-facing behavior (30 requirements, 12 business logic units) while eliminating all legacy technologies (GWT-RPC, XML configuration, Log4j 1.2, custom BCrypt, WAR packaging).

The plan follows a bottom-up, dependency-ordered approach aligned with the architecture patterns identified in the knowledge graph: Foundation → Data Access → Service → API → Frontend Foundation → Frontend Views → Integration.

---

## Technical Context

**Backend Language/Version**: Java 17+ (LTS)
**Backend Framework**: Spring Boot 3.x with Spring Security
**Backend Build Tool**: Maven 3.9+
**Backend Packaging**: Executable JAR with embedded Tomcat
**Backend Testing**: JUnit 5 + Mockito (`spring-boot-starter-test`)
**Backend Logging**: SLF4J + Logback (Spring Boot default)

**Frontend Language/Version**: TypeScript 5.x
**Frontend Framework**: React 18+
**Frontend Build Tool**: Vite
**Frontend Routing**: React Router v6
**Frontend State Management**: React Context API + useReducer
**Frontend HTTP Client**: Fetch API with thin wrapper
**Frontend Testing**: Jest + React Testing Library

**Authentication**: Session-based via Spring Security (HTTP session + JSESSIONID cookie)
**API Protocol**: REST (JSON over HTTP)
**CORS**: Spring Security `CorsConfigurationSource` with externalized origins
**Data Storage**: In-memory (ConcurrentHashMap) — matching source behavior
**Password Hashing**: Spring Security BCryptPasswordEncoder ($2a format, 10 rounds)
**Date Format**: ISO-8601 for API transport; LocalDate on backend

**Project Type**: Web application (separate frontend + backend)
**Performance Goals**: N/A (demo application, in-memory storage)
**Constraints**: Functional equivalence with source application (Constitution P1)
**Scale/Scope**: 2 users, 9 seed items, 7 API endpoints, 2 frontend pages + 1 dialog

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Evidence |
|---|---|---|
| **P1: Functional Equivalence** | ✅ PASS | All 12 business logic units mapped; all 30 REQs covered in plan phases |
| **P2: Clean Separation** | ✅ PASS | Backend (Spring Boot JAR) and Frontend (Vite static build) are fully independent |
| **P3: Modern Security** | ✅ PASS | Spring Security BCryptPasswordEncoder, session-based auth, CSRF protection, @JsonIgnore on passwords |
| **P4: Preserve Domain Model** | ✅ PASS | Item and User models preserve all fields, validation logic, equality contracts |
| **P5: API-First Design** | ✅ PASS | OpenAPI 3.0 contract defined in `contracts/api-contract.yaml`; all 7 GWT-RPC operations mapped to REST endpoints |
| **P6: Incremental Testability** | ✅ PASS | Every layer has independent tests: model unit tests, service+mockito tests, @WebMvcTest controller tests, Jest+RTL frontend tests |

| Architectural Constraint | Status | Evidence |
|---|---|---|
| **AC1: Backend** | ✅ PASS | Java 17+, Spring Boot 3.x, Maven, Spring Security, SLF4J+Logback, JUnit 5+Mockito, JAR packaging |
| **AC2: Frontend** | ✅ PASS | TypeScript, React 18+, Vite, React Router, Jest+RTL |
| **AC3: Integration** | ✅ PASS | RESTful JSON, Session-based auth, CORS configured, Structured JSON errors |

| Behavioral Contract | Status | Plan Phase |
|---|---|---|
| **BC1: Authentication Flow** | ✅ Covered | Phase 3 (Services) + Phase 4 (Controllers) + Phase 6 (Login Page) |
| **BC2: Session Validation** | ✅ Covered | Phase 3 (Security Config) |
| **BC3: Item CRUD** | ✅ Covered | Phase 3 (ItemService) + Phase 4 (ItemController) + Phase 7 (Main Page) |
| **BC4: Session Timeout** | ✅ Covered | Phase 3 (Security Config) + Phase 5 (HTTP Client interceptor) |
| **BC5: Navigation** | ✅ Covered | Phase 5 (React Router + AuthContext) + Phase 6 (Protected Routes) |

**Post-Design Re-check**: All gates still pass after Phase 1 design artifacts generated.

---

## Project Structure

### Documentation (this feature)

```text
.github/appmod/gwt-to-react-springboot/
├── spec.md                  # Feature specification (30 requirements)
├── plan.md                  # This file
├── research.md              # Phase 0 research decisions
├── data-model.md            # Phase 1 data model design
├── business-logic-inventory.md  # Phase 0 business logic extraction (rewrite mode)
├── quickstart.md            # Integration quick start guide
├── contracts/
│   └── api-contract.yaml    # OpenAPI 3.0 API contract
├── checkpoints/
│   └── spec-to-plan.yaml    # Traceability checkpoint
└── checklists/
    └── requirements.md      # Spec quality checklist
```

### Source Code (new project — rewrite)

```text
backend/
├── pom.xml                              # Spring Boot 3.x parent POM
├── src/main/java/com/example/
│   ├── GwtDemoApplication.java          # @SpringBootApplication entry point
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security + BCrypt + CORS + session
│   │   └── DataInitializer.java         # Seed data (users + items)
│   ├── controller/
│   │   ├── AuthController.java          # POST /api/auth/login, logout, GET /api/auth/me
│   │   └── ItemController.java          # GET/POST/PUT/DELETE /api/items
│   ├── service/
│   │   ├── UserService.java             # Authentication business logic
│   │   └── ItemService.java             # Item CRUD business logic
│   ├── repository/
│   │   ├── UserRepository.java          # In-memory user store
│   │   └── ItemRepository.java          # In-memory item store (ConcurrentHashMap + AtomicLong)
│   ├── model/
│   │   ├── User.java                    # Domain model (@JsonIgnore password)
│   │   └── Item.java                    # Domain model (LocalDate)
│   ├── dto/
│   │   ├── LoginRequest.java            # Login request payload
│   │   ├── LoginResponse.java           # Login response (token + user)
│   │   ├── DeleteRequest.java           # Batch delete payload (ids array)
│   │   └── ErrorResponse.java           # Structured error response
│   └── exception/
│       ├── LoginFailureException.java   # Authentication failure
│       ├── SessionExpiredException.java # Session timeout
│       ├── ItemServiceException.java    # Item operation failure
│       └── GlobalExceptionHandler.java  # @ControllerAdvice
├── src/main/resources/
│   └── application.properties           # Server config, logging, session, CORS
└── src/test/java/com/example/
    ├── model/
    │   ├── UserTest.java
    │   └── ItemTest.java
    ├── service/
    │   ├── UserServiceTest.java
    │   └── ItemServiceTest.java
    ├── controller/
    │   ├── AuthControllerTest.java      # @WebMvcTest
    │   └── ItemControllerTest.java      # @WebMvcTest
    └── repository/
        ├── UserRepositoryTest.java
        └── ItemRepositoryTest.java

frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── src/
│   ├── App.tsx                          # Root component + React Router config
│   ├── main.tsx                         # ReactDOM entry point
│   ├── types.ts                         # TypeScript interfaces (Item, User, DTOs)
│   ├── api/
│   │   ├── client.ts                    # Fetch wrapper (auth headers, 401 interceptor)
│   │   ├── authApi.ts                   # login(), logout(), getMe()
│   │   └── itemApi.ts                   # findAll(), create(), update(), deleteItems()
│   ├── context/
│   │   └── AuthContext.tsx              # Auth state provider + useAuth() hook
│   ├── pages/
│   │   ├── LoginPage.tsx               # Login form + validation + error display
│   │   └── MainPage.tsx                # Item table + CRUD + welcome header
│   ├── components/
│   │   ├── ItemDialog.tsx              # Create/edit item modal dialog
│   │   ├── ItemTable.tsx               # Item list table with checkboxes
│   │   └── ProtectedRoute.tsx          # Auth-guarded route wrapper
│   └── __tests__/
│       ├── LoginPage.test.tsx
│       ├── MainPage.test.tsx
│       └── ItemDialog.test.tsx
└── public/
    └── index.html
```

**Structure Decision**: Web application with separate `backend/` and `frontend/` directories at the repository root. Each is independently buildable and deployable (Constitution P2).

---

## Implementation Phases

### Phase 1: Backend Project Scaffolding
**Goal**: Create the Spring Boot project skeleton with build configuration
**Requirements**: REQ-024, REQ-025, REQ-027, REQ-028
**Guideline**: spring-boot-scaffolding

| # | Task | Requirements | Output |
|---|---|---|---|
| 1.1 | Create `backend/pom.xml` with Spring Boot 3.x parent, Java 17, dependencies (web, security, test, validation) | REQ-024, REQ-025 | `backend/pom.xml` |
| 1.2 | Create `GwtDemoApplication.java` with `@SpringBootApplication` | REQ-024, REQ-027 | Main class |
| 1.3 | Create `application.properties` with server port, session timeout (20min), logging (SLF4J DEBUG for com.example), CORS origins | REQ-028, REQ-029 | `application.properties` |
| 1.4 | Verify build: `mvn clean compile` succeeds | REQ-027 | Green build |

**Completion Criteria**: `mvn clean compile` passes; no GWT, Spring 2.5.6, Log4j 1.2, or JUnit 4 dependencies present.

---

### Phase 2: Domain Models, DTOs, and Exceptions
**Goal**: Implement all domain objects and data transfer types
**Requirements**: REQ-005, REQ-016, REQ-018, REQ-019
**Business Logic**: BL-010 (User Validation), BL-011 (Password Security)

| # | Task | Requirements | Business Logic | Output |
|---|---|---|---|---|
| 2.1 | Implement `Item.java` — id (long), name (String), description (String), date (LocalDate); equals/hashCode on all fields; no-arg + 3-arg constructors | REQ-018 | — | `model/Item.java` |
| 2.2 | Implement `User.java` — username (String), password (String + `@JsonIgnore`); `isValid()` method; equals/hashCode; toString masks password | REQ-005, REQ-019 | BL-010, BL-011 | `model/User.java` |
| 2.3 | Implement DTOs: `LoginRequest`, `LoginResponse`, `DeleteRequest`, `ErrorResponse` | REQ-016, REQ-021, REQ-022 | — | `dto/*.java` |
| 2.4 | Implement exceptions: `LoginFailureException`, `SessionExpiredException`, `ItemServiceException` | REQ-016 | — | `exception/*.java` |
| 2.5 | Implement `GlobalExceptionHandler` (@ControllerAdvice) mapping exceptions to structured JSON ErrorResponse | REQ-016 | — | `exception/GlobalExceptionHandler.java` |
| 2.6 | Write unit tests: `ItemTest.java`, `UserTest.java` (validation, equals, hashCode, toString, @JsonIgnore) | REQ-026 | BL-010 | `test/model/*.java` |

**Completion Criteria**: All model tests pass; `@JsonIgnore` verified; `User.isValid()` matches source behavior.

---

### Phase 3: Repositories, Services, and Security Configuration
**Goal**: Implement data access, business logic, and security
**Requirements**: REQ-001, REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-007, REQ-008, REQ-009, REQ-020, REQ-029, REQ-030
**Business Logic**: BL-001 through BL-009

| # | Task | Requirements | Business Logic | Output |
|---|---|---|---|---|
| 3.1 | Implement `UserRepository` — in-memory `HashMap<String, User>`, `findByUsername()` method | REQ-005, REQ-020 | — | `repository/UserRepository.java` |
| 3.2 | Implement `ItemRepository` — in-memory `ConcurrentHashMap<Long, Item>` + `AtomicLong` sequence; `findAll()`, `save()`, `update()`, `deleteAll()` | REQ-006, REQ-007, REQ-008, REQ-009, REQ-018, REQ-020 | — | `repository/ItemRepository.java` |
| 3.3 | Implement `DataInitializer` (@Component) — seed 2 users (BCrypt-hashed) + 9 items on startup | REQ-020 | — | `config/DataInitializer.java` |
| 3.4 | Implement `SecurityConfig` — Spring Security filter chain: session-based auth, BCryptPasswordEncoder bean, CORS config, CSRF config, 20-min session timeout, permit `/api/auth/login`, authenticate all others | REQ-004, REQ-005, REQ-023, REQ-029, REQ-030 | BL-004 | `config/SecurityConfig.java` |
| 3.5 | Implement `UserService` — `login()` (BCrypt verify, create session), `logout()` (invalidate session), `getCurrentUser()` (from SecurityContext) | REQ-001, REQ-002, REQ-003 | BL-001, BL-002, BL-003 | `service/UserService.java` |
| 3.6 | Implement `ItemService` — `findAll()`, `create()`, `update()`, `delete()` delegating to ItemRepository; wrap exceptions in ItemServiceException | REQ-006, REQ-007, REQ-008, REQ-009 | BL-006, BL-007, BL-008, BL-009 | `service/ItemService.java` |
| 3.7 | Write tests: `UserRepositoryTest`, `ItemRepositoryTest`, `UserServiceTest` (Mockito), `ItemServiceTest` (Mockito) | REQ-026 | BL-001–BL-009 | `test/repository/*.java`, `test/service/*.java` |

**Completion Criteria**: All service and repository tests pass; `davis/davis` and `homer/homer` login correctly; BCrypt hash compatibility verified; session timeout = 20 minutes.

---

### Phase 4: REST Controllers
**Goal**: Implement all REST API endpoints matching the OpenAPI contract
**Requirements**: REQ-001, REQ-002, REQ-003, REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-021, REQ-022
**Contract**: `contracts/api-contract.yaml`

| # | Task | Requirements | Output |
|---|---|---|---|
| 4.1 | Implement `AuthController` — `POST /api/auth/login` (accepts LoginRequest, returns LoginResponse), `POST /api/auth/logout` (204), `GET /api/auth/me` (returns User) | REQ-001, REQ-002, REQ-003, REQ-021 | `controller/AuthController.java` |
| 4.2 | Implement `ItemController` — `GET /api/items`, `POST /api/items` (201), `PUT /api/items/{id}`, `DELETE /api/items` (accepts DeleteRequest, 204) | REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-022 | `controller/ItemController.java` |
| 4.3 | Write `@WebMvcTest` integration tests: `AuthControllerTest` (login success/failure, logout, me), `ItemControllerTest` (CRUD, auth required, batch delete) | REQ-026 | `test/controller/*.java` |

**Completion Criteria**: All 7 API endpoints return correct status codes and response formats; all controller tests pass; unauthenticated requests return 401.

---

### Phase 5: Frontend Project Scaffolding and Foundation
**Goal**: Create the React+TypeScript project with core infrastructure
**Requirements**: REQ-011, REQ-012, REQ-024, REQ-027

| # | Task | Requirements | Output |
|---|---|---|---|
| 5.1 | Initialize Vite + React + TypeScript project: `package.json`, `tsconfig.json`, `vite.config.ts` (with proxy to backend) | REQ-024, REQ-027 | `frontend/` project skeleton |
| 5.2 | Create `types.ts` — TypeScript interfaces: `Item`, `User`, `LoginRequest`, `LoginResponse`, `ErrorResponse`, `DeleteRequest` | REQ-018, REQ-019 | `src/types.ts` |
| 5.3 | Create `api/client.ts` — Fetch wrapper with: auth header injection, JSON parsing, 401 interceptor (clears auth + redirects to login) | REQ-004, REQ-017 | `src/api/client.ts` |
| 5.4 | Create `api/authApi.ts` — `login()`, `logout()`, `getMe()` using client.ts | REQ-021 | `src/api/authApi.ts` |
| 5.5 | Create `api/itemApi.ts` — `findAll()`, `create()`, `update()`, `deleteItems()` using client.ts | REQ-022 | `src/api/itemApi.ts` |
| 5.6 | Create `context/AuthContext.tsx` — AuthProvider, useAuth() hook, state: { user, token, isAuthenticated, loading }, actions: login, logout, checkSession | REQ-001, REQ-002, REQ-003, REQ-004 | `src/context/AuthContext.tsx` |
| 5.7 | Create `components/ProtectedRoute.tsx` — redirects to /login if not authenticated | REQ-012 | `src/components/ProtectedRoute.tsx` |
| 5.8 | Create `App.tsx` — React Router config: `/login` → LoginPage, `/main` → ProtectedRoute(MainPage), `/` → redirect to `/login` | REQ-011 | `src/App.tsx` |
| 5.9 | Verify build: `npm run build` succeeds | REQ-027 | Green build |

**Completion Criteria**: `npm run build` passes; routes configured; auth context provides login/logout/checkSession; 401 interceptor redirects to login.

---

### Phase 6: Frontend Pages — Login
**Goal**: Implement the Login page with full authentication flow
**Requirements**: REQ-001, REQ-013, REQ-017
**Business Logic**: BL-001 (frontend portion), BL-010 (client-side validation)

| # | Task | Requirements | Output |
|---|---|---|---|
| 6.1 | Implement `LoginPage.tsx` — Form with username input, password input (masked), Login button; centered layout | REQ-001, REQ-013 | `src/pages/LoginPage.tsx` |
| 6.2 | Add client-side validation: both fields non-empty → error "Please enter a username and password" | REQ-001 | LoginPage validation |
| 6.3 | Add ENTER key handler on both inputs to trigger login | REQ-001 | LoginPage key handlers |
| 6.4 | Integrate with AuthContext: on success → navigate to /main; on failure → display server error | REQ-001, REQ-017 | LoginPage auth integration |
| 6.5 | Error display: clear error on new input or submission attempt | REQ-013, REQ-017 | LoginPage error UX |
| 6.6 | Write tests: `LoginPage.test.tsx` — render, validation, successful login navigation, error display, ENTER key | REQ-026 | `__tests__/LoginPage.test.tsx` |

**Completion Criteria**: Login form renders correctly; client validation matches source; ENTER key works; errors display and clear properly; test passes.

---

### Phase 7: Frontend Pages — Main Page and Item Dialog
**Goal**: Implement the main item management page with full CRUD
**Requirements**: REQ-002, REQ-006, REQ-007, REQ-008, REQ-009, REQ-010, REQ-014, REQ-015, REQ-017

| # | Task | Requirements | Output |
|---|---|---|---|
| 7.1 | Implement `ItemTable.tsx` — Table with columns: Name (128px), Description (192px), Date (256px), Checkbox (25px); row click handler (except checkbox column); row selection highlighting | REQ-006, REQ-014 | `src/components/ItemTable.tsx` |
| 7.2 | Implement `MainPage.tsx` — Header (welcome username + logout link), Center (ItemTable), Footer (Refresh, Delete, New buttons + error label) | REQ-002, REQ-006, REQ-010, REQ-014, REQ-017 | `src/pages/MainPage.tsx` |
| 7.3 | Integrate item fetch: auto-fetch on mount (after login/session recovery); refresh button re-fetches | REQ-006, REQ-010 | MainPage CRUD hooks |
| 7.4 | Implement `ItemDialog.tsx` — Modal with glass overlay, centered, title "Create/Edit an Item", fields: Name, Description, Date (date picker), Save + Cancel buttons | REQ-007, REQ-008, REQ-015 | `src/components/ItemDialog.tsx` |
| 7.5 | Wire "New" button → opens ItemDialog in create mode (empty fields) → POST /api/items → refresh list | REQ-007 | MainPage create flow |
| 7.6 | Wire row click → opens ItemDialog in edit mode (pre-populated) → PUT /api/items/{id} → refresh list | REQ-008 | MainPage edit flow |
| 7.7 | Wire "Delete" button → collect checked item IDs → DELETE /api/items → refresh list; skip if none selected | REQ-009 | MainPage delete flow |
| 7.8 | Wire logout link → POST /api/auth/logout → clear auth state → (source: no auto-redirect; SessionTimedOut handles redirect) | REQ-002 | MainPage logout |
| 7.9 | Error display: item API errors (500) in footer error label; 401 errors handled by interceptor (redirect) | REQ-017 | MainPage error handling |
| 7.10 | Write tests: `MainPage.test.tsx`, `ItemDialog.test.tsx` — render, CRUD operations, checkbox selection, error display | REQ-026 | `__tests__/MainPage.test.tsx`, `__tests__/ItemDialog.test.tsx` |

**Completion Criteria**: Full CRUD cycle works (create, read, update, delete); table renders with correct columns; dialog opens/closes; batch delete with checkboxes; errors display; all tests pass.

---

### Phase 8: Integration, Polish, and Verification
**Goal**: End-to-end integration testing, build verification, technology removal verification
**Requirements**: REQ-023, REQ-024, REQ-025, REQ-026, REQ-027

| # | Task | Requirements | Output |
|---|---|---|---|
| 8.1 | CORS integration test: verify React dev server (localhost:5173) can reach Spring Boot (localhost:8080) | REQ-023 | Manual/automated verification |
| 8.2 | Full flow smoke test: Login (davis/davis) → see 9 items → create item → edit item → delete item → refresh → logout | REQ-001–REQ-010 | Verified flow |
| 8.3 | Session timeout test: set short timeout → verify 401 → auto-redirect to login | REQ-004, REQ-029 | Verified behavior |
| 8.4 | Session recovery test: login → refresh page → verify session recovered via GET /api/auth/me | REQ-003 | Verified behavior |
| 8.5 | Technology removal verification: grep for GWT imports, Spring 2.5.6, Log4j 1.x, JUnit 4, EasyMock, web.xml, applicationContext.xml → zero results | REQ-025 | Clean codebase |
| 8.6 | Independent build verification: `cd backend && mvn clean package` AND `cd frontend && npm run build` both succeed independently | REQ-027 | Green builds |
| 8.7 | Test suite verification: all backend tests pass (`mvn test`); all frontend tests pass (`npm test`) | REQ-026 | All tests green |
| 8.8 | Logging verification: backend outputs SLF4J logs, DEBUG for com.example, INFO for root | REQ-028 | Log output verified |

**Completion Criteria**: All integration tests pass; zero legacy technology references; both projects build independently; all automated tests green.

---

## Phase-to-Requirement Traceability Matrix

| Requirement | Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5 | Phase 6 | Phase 7 | Phase 8 |
|---|---|---|---|---|---|---|---|---|
| REQ-001 | | | 3.5 | 4.1 | 5.6 | 6.1–6.5 | | 8.2 |
| REQ-002 | | | | | | | 7.2, 7.8 | 8.2 |
| REQ-003 | | | 3.5 | 4.1 | 5.6 | | | 8.4 |
| REQ-004 | | | 3.4, 3.5 | | 5.3 | | | 8.3 |
| REQ-005 | | 2.2 | 3.1, 3.3, 3.4, 3.5 | | | | | |
| REQ-006 | | | 3.2, 3.6 | 4.2 | | | 7.1, 7.2, 7.3 | 8.2 |
| REQ-007 | | | 3.2, 3.6 | 4.2 | | | 7.4, 7.5 | 8.2 |
| REQ-008 | | | 3.2, 3.6 | 4.2 | | | 7.4, 7.6 | 8.2 |
| REQ-009 | | | 3.2, 3.6 | 4.2 | | | 7.7 | 8.2 |
| REQ-010 | | | | 4.2 | | | 7.2, 7.3 | 8.2 |
| REQ-011 | | | | | 5.8 | | | |
| REQ-012 | | | | | 5.7 | | | |
| REQ-013 | | | | | | 6.1, 6.5 | | |
| REQ-014 | | | | | | | 7.1, 7.2 | |
| REQ-015 | | | | | | | 7.4 | |
| REQ-016 | | 2.3, 2.4, 2.5 | | | | | | |
| REQ-017 | | | | | 5.3 | 6.4, 6.5 | 7.9 | |
| REQ-018 | | 2.1 | 3.2 | | 5.2 | | | |
| REQ-019 | | 2.2 | | | 5.2 | | | |
| REQ-020 | | | 3.1, 3.2, 3.3 | | | | | |
| REQ-021 | | 2.3 | 3.5 | 4.1 | 5.4 | | | |
| REQ-022 | | 2.3 | 3.6 | 4.2 | 5.5 | | | |
| REQ-023 | | | 3.4 | | | | | 8.1 |
| REQ-024 | 1.1, 1.2 | | | | 5.1 | | | 8.5 |
| REQ-025 | 1.1 | | | | | | | 8.5 |
| REQ-026 | | 2.6 | 3.7 | 4.3 | | 6.6 | 7.10 | 8.7 |
| REQ-027 | 1.3, 1.4 | | | | 5.1, 5.9 | | | 8.6 |
| REQ-028 | 1.3 | | | | | | | 8.8 |
| REQ-029 | | | 3.4 | | | | | 8.3 |
| REQ-030 | | | 3.4 | | | | | |

---

## Applied Guidelines

- **Guideline**: `spring-boot-scaffolding`
  - **Skills Used**: Project scaffolding, Spring Boot configuration, POM setup, logback config, GlobalExceptionHandler
  - **Reference**: `.github/skills/guidelines/spring-boot-scaffolding/SKILL.md`
  - **Applied to**: Phase 1 (Backend Scaffolding) — informs POM structure, application.properties format, logging configuration, and exception handler patterns

---

## Complexity Tracking

No constitution violations to justify. All decisions follow the constitution's prescribed technology choices.

---

## Risk Mitigations in Plan

| Risk | Phase | Mitigation |
|---|---|---|
| R1: BCrypt compatibility | Phase 3 (3.3, 3.5) | Test seed user login with BCrypt-hashed passwords; verify `$2a` format matches |
| R2: In-memory session | Phase 3 (3.4) | Spring Security session management is an improvement over custom `sessionMap` |
| R3: Data loss on restart | Phase 3 (3.3) | `DataInitializer` re-seeds on every startup; documented as known behavior |
| R4: Date serialization | Phase 2 (2.1), Phase 5 (5.2) | ISO-8601 in JSON; `LocalDate` on backend; frontend formats for display |
| R5: EventBus decoupling | Phase 5 (5.6) | React Context replaces EventBus; direct callbacks replace item events |
| R6: Cookie → token migration | Phase 3 (3.4), Phase 5 (5.3) | Session-based auth with `JSESSIONID` cookie; `withCredentials: true` on frontend |
