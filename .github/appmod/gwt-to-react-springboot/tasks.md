# Tasks: GWT-to-React+SpringBoot Rewrite

> Governed by: `.github/appmod/constitution.md`
**Input**: Design documents from `.github/appmod/gwt-to-react-springboot/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/api-contract.yaml, business-logic-inventory.md
**Mode**: Rewrite (Full Re-architecture)
**Source Project**: `/` (repository root — `src/main/java/com/example/`)
**Target Backend**: `backend/`
**Target Frontend**: `frontend/`

**Tests**: YES — tests are requested per REQ-026 and the plan (every phase includes test tasks).

**Organization**: Tasks are grouped by implementation batch (max 15 tasks, max 25 files per batch), aligned with plan phases, to enable incremental implementation and testing.

## Applied Guidelines

- **spring-boot-scaffolding**: 4 scaffolding tasks generated (Phase 1) — informs POM structure, application.properties, logging, and exception handler patterns

## Format: `[ID] [P?] [Story?] [Plan:X.Y] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., US-AUTH, US-CRUD, US-NAV)
- **[Plan:X.Y]**: Traceability reference to plan.md phase and item
- **[Source:]**: Original source file(s) being rewritten (rewrite mode)
- **[BL:]**: Business logic unit reference from business-logic-inventory.md

## User Story Mapping

| Story ID | Description | Priority | Plan Phases | Requirements |
|----------|-------------|----------|-------------|--------------|
| US-AUTH | User Authentication (login, logout, session) | P1 | 3, 4, 5, 6 | REQ-001–005, REQ-021 |
| US-CRUD | Item CRUD Operations | P1 | 3, 4, 5, 7 | REQ-006–010, REQ-022 |
| US-NAV | Navigation & Routing | P1 | 5 | REQ-011, REQ-012 |
| US-UI | UI Components (Login, Main, Dialog) | P2 | 6, 7 | REQ-013–015, REQ-017 |
| US-NFR | Non-Functional (tech stack, logging, CORS, tests) | P1 | 1, 8 | REQ-016, REQ-023–030 |

---

## Phase 1: Backend Scaffolding (Setup)

**Purpose**: Create the Spring Boot project skeleton with build configuration
**Plan Phase**: Phase 1
**Batch Size**: 4 tasks, 4 files
**Guideline**: spring-boot-scaffolding

- [x] T001 [Plan:1.1] [GUIDELINE:spring-boot-scaffolding] Create `backend/pom.xml` with Spring Boot 3.x parent, Java 17, dependencies: spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-test, jackson-datatype-jsr310
- [x] T002 [Plan:1.2] [GUIDELINE:spring-boot-scaffolding] Create `backend/src/main/java/com/example/GwtDemoApplication.java` with `@SpringBootApplication` entry point
- [x] T003 [Plan:1.3] [GUIDELINE:spring-boot-scaffolding] Create `backend/src/main/resources/application.properties` with server.port=8080, session timeout (20min), SLF4J logging (DEBUG for com.example, INFO for root), CORS allowed-origins
- [x] T004 [Plan:1.4] Verify backend build: `cd backend && mvn clean compile` succeeds with zero GWT, Spring 2.5.6, Log4j 1.2, or JUnit 4 dependencies

**Checkpoint**: Backend compiles cleanly. No legacy dependencies present.

---

## Phase 2: Domain Models, DTOs, and Exceptions (Foundational)

**Purpose**: Implement all domain objects, data transfer types, and exception handling — MUST complete before any service/controller work
**Plan Phase**: Phase 2
**Batch Size**: 11 tasks, 13 files

**⚠️ CRITICAL**: No service or controller work can begin until this phase is complete.

- [x] T005 [P] [Plan:2.1] Implement `backend/src/main/java/com/example/model/Item.java` — id (long), name (String), description (String), date (LocalDate); equals/hashCode on all fields; no-arg + 3-arg constructors; getters/setters [Source: src/main/java/com/example/client/model/Item.java]
- [x] T006 [P] [Plan:2.2] Implement `backend/src/main/java/com/example/model/User.java` — username (String), password (String + `@JsonIgnore`); `isValid()` method; equals/hashCode on both fields; `toString()` masks password as `*******`; no-arg + 2-arg constructors [Source: src/main/java/com/example/client/model/User.java#isValid,clone,toString] [BL: BL-010, BL-011]
- [x] T007 [P] [Plan:2.3] Implement `backend/src/main/java/com/example/dto/LoginRequest.java` — username (String), password (String); getters/setters [Source: src/main/java/com/example/client/service/UserService.java]
- [x] T008 [P] [Plan:2.3] Implement `backend/src/main/java/com/example/dto/LoginResponse.java` — token (String), user (User); getters/setters
- [x] T009 [P] [Plan:2.3] Implement `backend/src/main/java/com/example/dto/DeleteRequest.java` — ids (List<Long>); getters/setters
- [x] T010 [P] [Plan:2.3] Implement `backend/src/main/java/com/example/dto/ErrorResponse.java` — timestamp (String), status (int), error (String), message (String), path (String); constructor; getters [Source: src/main/java/com/example/client/exception/LoginFailureException.java] [Source: src/main/java/com/example/client/exception/SessionTimedOutException.java] [Source: src/main/java/com/example/client/exception/ItemServiceException.java]
- [x] T011 [P] [Plan:2.4] Implement `backend/src/main/java/com/example/exception/LoginFailureException.java` — extends RuntimeException; default message "The email or password you entered is incorrect."; custom message constructor [Source: src/main/java/com/example/client/exception/LoginFailureException.java]
- [x] T012 [P] [Plan:2.4] Implement `backend/src/main/java/com/example/exception/SessionExpiredException.java` — extends RuntimeException; default message "The session has timed out. You will need to login again." [Source: src/main/java/com/example/client/exception/SessionTimedOutException.java]
- [x] T013 [P] [Plan:2.4] Implement `backend/src/main/java/com/example/exception/ItemServiceException.java` — extends RuntimeException; default message "Oops, we're having problems with the server. Try again later." [Source: src/main/java/com/example/client/exception/ItemServiceException.java]
- [x] T014 [Plan:2.5] [GUIDELINE:spring-boot-scaffolding] Implement `backend/src/main/java/com/example/exception/GlobalExceptionHandler.java` — @ControllerAdvice mapping LoginFailureException→401, SessionExpiredException→401, ItemServiceException→500; returns ErrorResponse JSON with timestamp, status, error, message, path [Source: src/main/java/com/example/server/controller/RpcController.java#handleRequest]
- [x] T015 [Plan:2.6] Write unit tests in `backend/src/test/java/com/example/model/ItemTest.java` and `backend/src/test/java/com/example/model/UserTest.java` — test constructors, getters/setters, equals, hashCode, toString, isValid(), @JsonIgnore verification [Source: src/test/java/com/example/client/model/ItemTest.java] [Source: src/test/java/com/example/client/model/UserTest.java]

**Checkpoint**: All model tests pass. `@JsonIgnore` verified on User.password. `User.isValid()` matches source behavior. All DTOs and exceptions compile.

---

## Phase 3: Repositories, Services, and Security Configuration (Foundational)

**Purpose**: Implement data access, business logic services, security configuration, and seed data
**Plan Phase**: Phase 3
**Batch Size**: 14 tasks, 14 files

**⚠️ CRITICAL**: No controller or frontend work can begin until this phase is complete.

- [x] T016 [P] [Plan:3.1] Implement `backend/src/main/java/com/example/repository/UserRepository.java` — @Component; in-memory HashMap<String, User> keyed by username; `findByUsername(String)` returns Optional<User>; `save(User)` method [Source: src/main/java/com/example/server/dao/UserDaoImpl.java#findByUserName]
- [x] T017 [P] [Plan:3.2] Implement `backend/src/main/java/com/example/repository/ItemRepository.java` — @Component; in-memory ConcurrentHashMap<Long, Item> + AtomicLong sequence; `findAll()` returns List<Item>; `save(Item)` assigns ID via incrementAndGet(); `update(Item)` no-op if ID not found; `deleteAll(List<Long>)` removes by ID [Source: src/main/java/com/example/server/dao/ItemDaoImpl.java#findAll,create,update,delete] [BL: BL-006, BL-007, BL-008, BL-009]
- [x] T018 [Plan:3.3] Implement `backend/src/main/java/com/example/config/DataInitializer.java` — @Component implementing CommandLineRunner; seeds 2 users (davis/davis, homer/homer) with BCryptPasswordEncoder-hashed passwords; seeds 9 items (Item 1, Item 2, Foo, Bar, Baz, Widget, FooBar, BarFoo, FooBaz) with descriptions and dates [Source: src/main/java/com/example/server/dao/UserDaoImpl.java] [Source: src/main/java/com/example/server/dao/ItemDaoImpl.java]
- [x] T019 [Plan:3.4] Implement `backend/src/main/java/com/example/config/SecurityConfig.java` — @Configuration @EnableWebSecurity; BCryptPasswordEncoder @Bean; SecurityFilterChain: permit POST /api/auth/login, authenticate all others; session management (IF_REQUIRED, 20-min timeout); CORS config via CorsConfigurationSource (allowed origins from app.cors.allowed-origins property, methods GET/POST/PUT/DELETE/OPTIONS, credentials true); CSRF disable for REST API [Source: src/main/java/com/example/server/service/AbstractService.java#isSessionValid] [Source: src/main/java/com/example/server/controller/RpcController.java#handleRequest] [BL: BL-004]
- [x] T020 [Plan:3.5] Implement `backend/src/main/java/com/example/service/UserService.java` — @Service; `login(LoginRequest)`: validate non-null/non-empty, lookup by username (throw LoginFailureException "Sorry, we couldn't locate you in our records." if not found), BCrypt verify (throw LoginFailureException if mismatch), create authenticated session via SecurityContext, return LoginResponse(sessionId, user without password); `logout(HttpSession)`: invalidate session; `getCurrentUser(Authentication)`: return username from SecurityContext [Source: src/main/java/com/example/server/service/UserServiceImpl.java#login,logout,isLoggedIn] [BL: BL-001, BL-002, BL-003]
- [x] T021 [Plan:3.6] Implement `backend/src/main/java/com/example/service/ItemService.java` — @Service; `findAll()`: delegate to ItemRepository; `create(Item)`: delegate to repository.save(); `update(long id, Item)`: set ID and delegate to repository.update(); `delete(List<Long> ids)`: delegate to repository.deleteAll(); wrap all operations in try-catch, throw ItemServiceException on failure [Source: src/main/java/com/example/server/service/ItemServiceImpl.java#findAll,create,update,delete] [BL: BL-006, BL-007, BL-008, BL-009]
- [x] T022 [P] [Plan:3.7] Write `backend/src/test/java/com/example/repository/UserRepositoryTest.java` — test findByUsername (found/not found), save, multiple users [Source: src/test/java/com/example/server/service/UserServiceImplTest.java]
- [x] T023 [P] [Plan:3.7] Write `backend/src/test/java/com/example/repository/ItemRepositoryTest.java` — test findAll, save (auto-increment ID), update (existing/non-existing), deleteAll (existing/non-existing/empty list), concurrent access [Source: src/test/java/com/example/server/service/ItemServiceImplTest.java]
- [x] T024 [P] [Plan:3.7] Write `backend/src/test/java/com/example/service/UserServiceTest.java` — Mockito-based; test login success, login user not found (custom message), login wrong password, login null/empty fields, logout, getCurrentUser; verify BCrypt usage [Source: src/test/java/com/example/server/service/UserServiceImplTest.java] [BL: BL-001, BL-002, BL-003]
- [x] T025 [P] [Plan:3.7] Write `backend/src/test/java/com/example/service/ItemServiceTest.java` — Mockito-based; test findAll, create, update, delete (batch), exception wrapping in ItemServiceException [Source: src/test/java/com/example/server/service/ItemServiceImplTest.java] [BL: BL-006, BL-007, BL-008, BL-009]

**Checkpoint**: All service and repository tests pass. `davis/davis` and `homer/homer` login correctly. BCrypt hash compatibility verified. Session timeout = 20 minutes configured.

---

## Phase 4: REST Controllers (US-AUTH + US-CRUD Backend)

**Purpose**: Implement all REST API endpoints matching the OpenAPI contract
**Plan Phase**: Phase 4
**Batch Size**: 5 tasks, 5 files

- [x] T026 [US-AUTH] [Plan:4.1] Implement `backend/src/main/java/com/example/controller/AuthController.java` — @RestController @RequestMapping("/api/auth"); `POST /login` accepts LoginRequest returns LoginResponse (200); `POST /logout` invalidates session (204); `GET /me` returns current User from SecurityContext (200) or 401 [Source: src/main/java/com/example/server/service/UserServiceImpl.java#login,logout,isLoggedIn] [Source: src/main/java/com/example/server/controller/RpcController.java#handleRequest] [BL: BL-001, BL-002, BL-003]
- [x] T027 [US-CRUD] [Plan:4.2] Implement `backend/src/main/java/com/example/controller/ItemController.java` — @RestController @RequestMapping("/api/items"); `GET /` returns List<Item> (200); `POST /` creates item returns created Item (201); `PUT /{id}` updates item returns Item (200); `DELETE /` accepts DeleteRequest (204) [Source: src/main/java/com/example/server/service/ItemServiceImpl.java#findAll,create,update,delete] [Source: src/main/java/com/example/server/controller/RpcController.java#handleRequest] [BL: BL-006, BL-007, BL-008, BL-009]
- [x] T028 [P] [US-AUTH] [Plan:4.3] Write `backend/src/test/java/com/example/controller/AuthControllerTest.java` — @WebMvcTest; test login success (200), login failure (401, custom messages), logout (204), get current user (200), unauthenticated access (401) [Source: src/test/java/com/example/server/controller/RpcControllerTest.java]
- [x] T029 [P] [US-CRUD] [Plan:4.3] Write `backend/src/test/java/com/example/controller/ItemControllerTest.java` — @WebMvcTest; test findAll (200), create (201), update (200), batch delete (204), unauthenticated access (401) [Source: src/test/java/com/example/server/controller/RpcControllerTest.java]
- [x] T030 [Plan:1.4,4.3] Verify full backend build: `cd backend && mvn clean package` — all tests pass, JAR produced

**Checkpoint**: All 7 API endpoints return correct status codes and response formats. All controller tests pass. Unauthenticated requests return 401. Backend packages as executable JAR.

---

## Phase 5: Frontend Project Scaffolding and Foundation (Setup + US-NAV)

**Purpose**: Create the React+TypeScript project with core infrastructure, types, API client, auth context, and routing
**Plan Phase**: Phase 5
**Batch Size**: 13 tasks, 13 files

- [x] T031 [Plan:5.1] Initialize Vite + React + TypeScript project in `frontend/`: create `frontend/package.json` with dependencies (react, react-dom, react-router-dom, typescript, @types/react, @types/react-dom), devDependencies (vite, @vitejs/plugin-react, jest, @testing-library/react, @testing-library/jest-dom, ts-jest, jest-environment-jsdom, @types/jest)
- [x] T032 [P] [Plan:5.1] Create `frontend/tsconfig.json` with strict mode, JSX react-jsx, ES2020 target, module ESNext, moduleResolution bundler
- [x] T033 [P] [Plan:5.1] Create `frontend/vite.config.ts` with React plugin and dev proxy: `/api` → `http://localhost:8080` (for CORS-free development)
- [x] T034 [P] [Plan:5.1] Create `frontend/public/index.html` with root div and `frontend/src/main.tsx` with ReactDOM.createRoot entry point
- [x] T035 [P] [Plan:5.2] Create `frontend/src/types.ts` — TypeScript interfaces: Item (id, name, description, date), User (username only), LoginRequest, LoginResponse, ErrorResponse, DeleteRequest [Source: src/main/java/com/example/client/model/Item.java] [Source: src/main/java/com/example/client/model/User.java]
- [x] T036 [Plan:5.3] Create `frontend/src/api/client.ts` — Fetch wrapper with: base URL config, JSON content-type headers, credentials: 'include' for session cookies, response JSON parsing, 401 interceptor (clears auth state + redirects to /login) [Source: src/main/java/com/example/client/cookies/Cookies.java] [Source: src/main/java/com/example/client/presenter/MainPresenter.java#handleThrowable] [BL: BL-012]
- [x] T037 [P] [Plan:5.4] Create `frontend/src/api/authApi.ts` — `login(req: LoginRequest): Promise<LoginResponse>`, `logout(): Promise<void>`, `getMe(): Promise<User>` using client.ts [Source: src/main/java/com/example/client/service/UserService.java] [Source: src/main/java/com/example/client/service/UserServiceAsync.java]
- [x] T038 [P] [Plan:5.5] Create `frontend/src/api/itemApi.ts` — `findAll(): Promise<Item[]>`, `create(item: Omit<Item,'id'>): Promise<Item>`, `update(item: Item): Promise<Item>`, `deleteItems(ids: number[]): Promise<void>` using client.ts [Source: src/main/java/com/example/client/service/ItemService.java] [Source: src/main/java/com/example/client/service/ItemServiceAsync.java]
- [x] T039 [Plan:5.6] Create `frontend/src/context/AuthContext.tsx` — AuthProvider component, useAuth() hook; state: { user: User|null, token: string|null, isAuthenticated: boolean, loading: boolean }; actions: login(username, password), logout(), checkSession(); on mount calls getMe() for session recovery; on 401 clears state [Source: src/main/java/com/example/client/AppController.java#onValueChange,navigateToMain] [Source: src/main/java/com/example/client/event/LoginEvent.java] [Source: src/main/java/com/example/client/event/SessionTimedOutEvent.java] [BL: BL-003]
- [x] T040 [US-NAV] [Plan:5.7] Create `frontend/src/components/ProtectedRoute.tsx` — wraps children; if not authenticated redirects to /login via Navigate; if loading shows loading indicator [Source: src/main/java/com/example/client/AppController.java#navigateToMain,callback]
- [x] T041 [US-NAV] [Plan:5.8] Create `frontend/src/App.tsx` — BrowserRouter + Routes config: `/login` → LoginPage, `/main` → ProtectedRoute(MainPage), `/` → Navigate to /login; wrap in AuthProvider [Source: src/main/java/com/example/client/AppController.java#onValueChange] [Source: src/main/java/com/example/client/Application.java]
- [x] T042 [Plan:5.1] Create Jest configuration: `frontend/jest.config.ts` with ts-jest preset, jsdom environment, moduleNameMapper for CSS/assets, setupFilesAfterSetup for @testing-library/jest-dom
- [x] T043 [Plan:5.9] Verify frontend build: `cd frontend && npm install && npm run build` succeeds

**Checkpoint**: Frontend compiles. Routes configured. Auth context provides login/logout/checkSession. 401 interceptor redirects to login. `npm run build` passes.

---

## Phase 6: Frontend Login Page (US-AUTH Frontend)

**Purpose**: Implement the Login page with full authentication flow
**Plan Phase**: Phase 6
**Batch Size**: 4 tasks, 2 files

- [x] T044 [US-AUTH] [Plan:6.1,6.2,6.3,6.4,6.5] Implement `frontend/src/pages/LoginPage.tsx` — Form with: username input, password input (type="password" masked), Login button; centered layout; client-side validation (both fields non-empty → error "Please enter a username and password"); ENTER key handler on both inputs triggers login; integrates with AuthContext (on success → navigate to /main, on failure → display server error); error display clears on new input or submission attempt [Source: src/main/java/com/example/client/view/LoginView.java] [Source: src/main/java/com/example/client/presenter/LoginPresenter.java#doLogin,callback] [BL: BL-001, BL-010]
- [x] T045 [US-AUTH] [Plan:6.6] Write `frontend/src/__tests__/LoginPage.test.tsx` — test: renders form elements, client-side validation (empty fields), successful login navigates to /main, failed login shows error, ENTER key triggers login, error clears on new input [Source: src/test/java/com/example/client/presenter/LoginPresenterTest.java]

**Checkpoint**: Login form renders correctly. Client validation matches source. ENTER key works. Errors display and clear properly. Tests pass.

---

## Phase 7: Frontend Main Page and Item Dialog (US-CRUD Frontend + US-UI)

**Purpose**: Implement the main item management page with full CRUD and item dialog
**Plan Phase**: Phase 7
**Batch Size**: 10 tasks, 6 files

- [x] T046 [US-CRUD] [Plan:7.1] Implement `frontend/src/components/ItemTable.tsx` — Table with columns: Name (128px), Description (192px), Date (256px), Checkbox (25px); header row; data rows with checkbox per row; row click handler fires on non-checkbox columns (cell index != 3 equivalent); selected row highlighting (CSS class); checkbox click does NOT trigger row selection [Source: src/main/java/com/example/client/view/MainView.java#setItems,getSelectedItems] [Source: src/main/java/com/example/client/presenter/MainPresenter.java#onSelectedItem]
- [x] T047 [US-CRUD] [Plan:7.2] Implement `frontend/src/pages/MainPage.tsx` — Layout: Header (welcome {username} text + logout hyperlink), Center (ItemTable component), Footer (Refresh button, Delete button, New button + error label) [Source: src/main/java/com/example/client/view/MainView.java] [Source: src/main/java/com/example/client/presenter/MainPresenter.java#bind]
- [x] T048 [US-CRUD] [Plan:7.3] Integrate item fetching in MainPage: auto-fetch items on mount (after login/session recovery); Refresh button re-fetches all items via GET /api/items; clear selection state on refresh [Source: src/main/java/com/example/client/presenter/MainPresenter.java#loginHandler,refreshClickHandler] [BL: BL-006]
- [x] T049 [US-UI] [Plan:7.4] Implement `frontend/src/components/ItemDialog.tsx` — Modal dialog with glass overlay (dimmed background), centered, title "Create/Edit an Item"; fields: Name (text input), Description (text input), Date (date input/picker); Save button + Cancel button; animation on open; create mode (empty fields, default date today) vs edit mode (pre-populated from selected item) [Source: src/main/java/com/example/client/view/ItemView.java#showPopUp,setItem] [Source: src/main/java/com/example/client/presenter/ItemPresenter.java#saveHandler]
- [x] T050 [US-CRUD] [Plan:7.5] Wire "New" button in MainPage → opens ItemDialog in create mode (empty fields) → on Save: POST /api/items → refresh item list → close dialog [Source: src/main/java/com/example/client/presenter/MainPresenter.java#newClickHandler] [Source: src/main/java/com/example/client/event/ItemCreateEvent.java] [BL: BL-007]
- [x] T051 [US-CRUD] [Plan:7.6] Wire row click in MainPage → opens ItemDialog in edit mode (pre-populated) → on Save: PUT /api/items/{id} → refresh item list → close dialog [Source: src/main/java/com/example/client/presenter/MainPresenter.java#onSelectedItem] [Source: src/main/java/com/example/client/event/ItemUpdateEvent.java] [BL: BL-008]
- [x] T052 [US-CRUD] [Plan:7.7] Wire "Delete" button in MainPage → collect checked item IDs from ItemTable → if none selected, skip (no API call); otherwise DELETE /api/items with ids → refresh item list [Source: src/main/java/com/example/client/presenter/MainPresenter.java#deleteClickHandler] [Source: src/main/java/com/example/client/view/MainView.java#getSelectedItems] [BL: BL-009]
- [x] T053 [US-AUTH] [Plan:7.8] Wire logout link in MainPage header → POST /api/auth/logout → clear auth state via AuthContext → user is not auto-redirected (source behavior: SessionTimedOutEvent handles redirect) [Source: src/main/java/com/example/client/presenter/MainPresenter.java#logoutClickHandler] [BL: BL-002]
- [x] T054 [US-UI] [Plan:7.9] Implement error display in MainPage: item API errors (500) shown in footer error label; 401 errors handled by client.ts interceptor (redirect to /login); clear error on successful operations [Source: src/main/java/com/example/client/presenter/MainPresenter.java#handleThrowable] [BL: BL-012]
- [x] T055 [US-CRUD] [Plan:7.10] Write `frontend/src/__tests__/MainPage.test.tsx` and `frontend/src/__tests__/ItemDialog.test.tsx` — test: MainPage renders with header/table/buttons, item list fetch on mount, create flow, edit flow, delete flow, checkbox selection, error display; ItemDialog renders fields, save/cancel, create vs edit mode [Source: src/test/java/com/example/client/presenter/MainPresenterTest.java] [Source: src/test/java/com/example/client/presenter/ItemPresenterTest.java]

**Checkpoint**: Full CRUD cycle works (create, read, update, delete). Table renders with correct columns. Dialog opens/closes. Batch delete with checkboxes. Errors display. All tests pass.

---

## Phase 8: Integration, Polish, and Verification (Final)

**Purpose**: End-to-end integration testing, build verification, technology removal verification
**Plan Phase**: Phase 8
**Batch Size**: 8 tasks, 0 new files (verification only)

- [x] T056 [Plan:8.1] CORS integration verification: verify React dev server (localhost:5173) can reach Spring Boot (localhost:8080) — test preflight OPTIONS and actual API calls with credentials
- [x] T057 [Plan:8.2] Full flow smoke test: Login (davis/davis) → see 9 items in table → create new item → edit existing item → delete item via checkbox → refresh → logout → verify redirected to login
- [x] T058 [Plan:8.3] Session timeout test: configure short session timeout → make API call after expiry → verify 401 response → verify auto-redirect to login page
- [x] T059 [Plan:8.4] Session recovery test: login → refresh browser page → verify session recovered via GET /api/auth/me → verify items displayed without re-login
- [x] T060 [Plan:8.5] Technology removal verification: grep entire codebase for GWT imports, Spring 2.5.6 artifacts, Log4j 1.x, JUnit 4, EasyMock, web.xml, applicationContext.xml, dispatcher-servlet.xml → zero results in backend/ and frontend/
- [x] T061 [Plan:8.6] Independent build verification: `cd backend && mvn clean package` AND `cd frontend && npm run build` both succeed independently — no circular or cross-project build dependencies
- [x] T062 [Plan:8.7] Test suite verification: `cd backend && mvn test` all pass AND `cd frontend && npm test` all pass — zero failures
- [x] T063 [Plan:8.8] Logging verification: start backend → verify SLF4J log output → confirm DEBUG level for com.example package, INFO for root logger, console appender active

**Checkpoint**: All integration tests pass. Zero legacy technology references. Both projects build independently. All automated tests green. Application is fully functional.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Backend Scaffolding)**: No dependencies — can start immediately
- **Phase 2 (Domain Models/DTOs/Exceptions)**: Depends on Phase 1 — BLOCKS all service/controller work
- **Phase 3 (Repositories/Services/Security)**: Depends on Phase 2 — BLOCKS all controller work
- **Phase 4 (REST Controllers)**: Depends on Phase 3
- **Phase 5 (Frontend Scaffolding)**: Can start after Phase 1 (independent of backend Phases 2–4)
- **Phase 6 (Login Page)**: Depends on Phase 5 (frontend foundation) + Phase 4 (backend API for integration)
- **Phase 7 (Main Page + Dialog)**: Depends on Phase 5 + Phase 6 (login must work first)
- **Phase 8 (Integration/Polish)**: Depends on ALL previous phases

### Parallel Opportunities

```
Phase 1 ──────────────────────────────────────────────────────►
              │
              ├──► Phase 2 ──► Phase 3 ──► Phase 4 ──┐
              │                                        │
              └──► Phase 5 (frontend, parallel) ──────┤
                              │                        │
                              ├──► Phase 6 ◄──────────┤
                              │                        │
                              └──► Phase 7 ◄──────────┘
                                         │
                                         └──► Phase 8
```

### Within Each Phase

- Tasks marked [P] can run in parallel (different files, no dependencies)
- Models before services (T005-T006 before T020-T021)
- Services before controllers (T020-T021 before T026-T027)
- Foundation types before UI components (T035 before T044)

### Parallel Execution Examples

```bash
# Phase 2: All models, DTOs, and exceptions in parallel
T005 (Item.java) || T006 (User.java) || T007-T010 (DTOs) || T011-T013 (Exceptions)
# Then: T014 (GlobalExceptionHandler) + T015 (Tests)

# Phase 3: Repositories in parallel, then services
T016 (UserRepository) || T017 (ItemRepository)
# Then: T018 (DataInitializer) + T019 (SecurityConfig)
# Then: T020 (UserService) || T021 (ItemService)
# Then: T022-T025 (Tests in parallel)

# Phase 5: Frontend scaffolding files in parallel
T032 (tsconfig) || T033 (vite.config) || T034 (main.tsx) || T035 (types.ts)
# Then: T036 (client.ts)
# Then: T037 (authApi) || T038 (itemApi) || T042 (jest.config)
```

---

## Implementation Strategy

### MVP First (Phase 1–4: Backend Only)

1. Complete Phase 1: Backend Scaffolding
2. Complete Phase 2: Domain Models, DTOs, Exceptions
3. Complete Phase 3: Repositories, Services, Security
4. Complete Phase 4: REST Controllers
5. **STOP and VALIDATE**: All backend tests pass, API is functional via curl/Postman

### Full Stack Incremental

1. Complete backend (Phases 1–4)
2. Complete frontend foundation (Phase 5)
3. Add login page (Phase 6) → **Test login flow end-to-end**
4. Add main page + dialog (Phase 7) → **Test full CRUD end-to-end**
5. Integration verification (Phase 8) → **Ship it**

### Batch Size Summary

| Phase | Tasks | New Files | Description |
|-------|-------|-----------|-------------|
| 1 | 4 | 4 | Backend scaffolding |
| 2 | 11 | 13 | Models, DTOs, exceptions, tests |
| 3 | 10 | 14 | Repos, services, security, tests |
| 4 | 5 | 5 | Controllers, tests, build verify |
| 5 | 13 | 13 | Frontend scaffolding + foundation |
| 6 | 2 | 2 | Login page + tests |
| 7 | 10 | 6 | Main page, dialog, CRUD wiring, tests |
| 8 | 8 | 0 | Integration verification |
| **Total** | **63** | **57** | |

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- [Plan:X.Y] links each task to its source plan item
- [Source:] annotations reference original GWT source files for rewrite fidelity
- [BL:] references link to business-logic-inventory.md behavioral specifications
- This is a REWRITE — all tasks create NEW files in `backend/` or `frontend/`
- The original `src/`, `conf/`, `war/`, `pom.xml` remain untouched for reference
- Verify tests fail before implementing (for test-first tasks)
- Commit after each phase or logical group
- Stop at any checkpoint to validate independently
