# Architecture Patterns & Migration Order

> **Project:** gwt-demo
> **Analysis Date:** Auto-generated during Foundation Phase

---

## 1. Detected Architecture Patterns

### 1.1 Model-View-Presenter (MVP)
The application follows the MVP pattern on the client side:
- **Model:** `Item`, `User` — plain data objects with `IsSerializable`
- **View:** `LoginView`, `MainView`, `ItemView` — GWT widgets implementing Display interfaces
- **Presenter:** `LoginPresenter`, `MainPresenter`, `ItemPresenter` — contain all logic, interact with view through Display interfaces

**Migration Mapping:** MVP → React component pattern
- Presenter logic → React hooks (`useState`, `useEffect`, custom hooks)
- Display interface → React component props/state
- View → React JSX/TSX components

### 1.2 Event Bus (Observer Pattern)
Client-side communication via custom `EventBus` wrapping GWT `HandlerManager`:
- `LoginEvent` / `LoginEventHandler`
- `SessionTimedOutEvent` / `SessionTimedOutEventHandler`
- `ItemCreateEvent` / `ItemCreateEventHandler`
- `ItemUpdateEvent` / `ItemUpdateEventHandler`

**Migration Mapping:** EventBus → React Context + callback props
- Login state changes → `AuthContext` with `useAuth()` hook
- Item CRUD callbacks → Direct prop callbacks from modal to parent
- Session timeout → Axios/fetch interceptor + auth context

### 1.3 Front Controller
Single `RpcController` handles all GWT-RPC traffic:
- Decodes RPC requests
- Validates sessions (except for auth endpoints)
- Dispatches to Spring-managed service beans

**Migration Mapping:** Front Controller → Spring Security filter chain + individual `@RestController`s

### 1.4 DAO Pattern (Repository)
Data access abstracted through interfaces:
- `UserDao` / `UserDaoImpl` — in-memory HashMap
- `ItemDao` / `ItemDaoImpl` — in-memory ConcurrentHashMap with AtomicLong sequence

**Migration Mapping:** DAO → Spring `@Repository` or Spring Data JPA

### 1.5 Service Layer
Business logic in service classes extending `AbstractService`:
- `UserServiceImpl` — authentication logic with BCrypt
- `ItemServiceImpl` — CRUD delegation to DAO

**Migration Mapping:** Service Layer → Spring `@Service` classes

### 1.6 Dependency Injection (XML-based)
All wiring done via Spring XML (`applicationContext.xml`, `dispatcher-servlet.xml`):
- Property injection for DAOs into Services
- Property injection for Services into Controllers

**Migration Mapping:** XML DI → Spring Boot annotation-based DI (`@Autowired`, `@Service`, `@Repository`)

---

## 2. Technology Stack Inventory

### Source Stack
| Category | Technology | Version |
|---|---|---|
| Language | Java | 1.6 |
| UI Framework | GWT (Google Web Toolkit) | 2.0.0 |
| UI Binding | GWT UiBinder | (bundled with GWT 2.0) |
| Backend Framework | Spring MVC | 2.5.6 |
| RPC Protocol | GWT-RPC | (bundled with GWT 2.0) |
| Security | Custom BCrypt (Damien Miller) | N/A |
| Logging | Log4j | 1.2.13 |
| Build Tool | Maven | (with gwt-maven-plugin 1.2) |
| Test Framework | JUnit | 4.4 |
| Mock Framework | EasyMock | 2.5.1 |
| App Server | JBoss 4.2.x / Jetty 6.1.10 | N/A |
| Packaging | WAR | N/A |

### Target Stack
| Category | Technology | Version |
|---|---|---|
| Language (Backend) | Java | 17+ |
| Language (Frontend) | TypeScript | 5.x |
| UI Framework | React | 18+ |
| Backend Framework | Spring Boot | 3.x |
| API Protocol | REST (JSON) | N/A |
| Security | Spring Security + BCryptPasswordEncoder | (Spring Boot managed) |
| Logging | SLF4J + Logback | (Spring Boot default) |
| Build Tool (Backend) | Maven | 3.9+ |
| Build Tool (Frontend) | Vite / npm | Latest |
| Test Framework (Backend) | JUnit 5 | (Spring Boot managed) |
| Test Framework (Frontend) | Jest + React Testing Library | Latest |
| Mock Framework | Mockito | (Spring Boot managed) |
| App Server | Embedded Tomcat | (Spring Boot default) |
| Packaging (Backend) | JAR | N/A |
| Packaging (Frontend) | Static files (HTML/JS/CSS) | N/A |

---

## 3. Migration Order

The migration should follow a bottom-up, dependency-ordered approach. Components with fewer dependencies are migrated first.

### Phase 1: Foundation (No dependencies)
**Priority: Highest — These are leaf nodes in the dependency graph**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 1.1 | Domain Models | `client/model/Item.java`, `client/model/User.java` | Backend `model/` + Frontend `types.ts` | Zero internal dependencies; everything else depends on these |
| 1.2 | Exception Classes | `client/exception/*.java` | Backend `exception/` package + `@ControllerAdvice` | No dependencies; needed by services and controllers |
| 1.3 | Security (BCrypt) | `server/security/BCrypt.java` | Spring Security `BCryptPasswordEncoder` | No internal dependencies; needed by UserService and UserDao |

### Phase 2: Data Access Layer
**Priority: High — Depends only on models**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 2.1 | UserDao | `server/dao/UserDao.java`, `UserDaoImpl.java` | `repository/UserRepository.java` | Depends on User model + BCrypt only |
| 2.2 | ItemDao | `server/dao/ItemDao.java`, `ItemDaoImpl.java` | `repository/ItemRepository.java` | Depends on Item model only |

### Phase 3: Service Layer
**Priority: High — Depends on DAOs and models**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 3.1 | Session Management | `server/service/BaseService.java`, `AbstractService.java` | Spring Security config | Depends on User model; needed by all services |
| 3.2 | UserService | `server/service/UserServiceImpl.java` | `service/UserService.java` + Spring Security | Depends on UserDao, BCrypt, AbstractService |
| 3.3 | ItemService | `server/service/ItemServiceImpl.java` | `service/ItemService.java` | Depends on ItemDao, AbstractService |

### Phase 4: API Layer
**Priority: High — Depends on services**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 4.1 | Auth Controller | `server/controller/RpcController.java` (user part) | `controller/AuthController.java` | Depends on UserService; defines REST API contract |
| 4.2 | Item Controller | `server/controller/RpcController.java` (item part) | `controller/ItemController.java` | Depends on ItemService; defines REST API contract |
| 4.3 | Spring Boot Config | `applicationContext.xml`, `dispatcher-servlet.xml`, `web.xml` | `application.properties` + auto-config | Consolidates all XML config |

### Phase 5: Frontend Foundation
**Priority: Medium — Can start in parallel with Phase 4**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 5.1 | Project Setup | `Application.gwt.xml`, `pom.xml` | `frontend/package.json`, `tsconfig.json` | Build system + TypeScript types |
| 5.2 | Type Definitions | `client/model/*.java` | `frontend/src/types.ts` | TypeScript interfaces matching API DTOs |
| 5.3 | API Client | `client/service/*Async.java` | `frontend/src/api/` | HTTP client utilities for REST endpoints |
| 5.4 | Auth Context | `client/event/LoginEvent*`, `client/cookies/*` | `frontend/src/context/AuthContext.tsx` | State management for authentication |
| 5.5 | Router Config | `AppController.java` (history/navigation) | `frontend/src/App.tsx` + React Router | Route definitions + protected routes |

### Phase 6: Frontend Views
**Priority: Medium — Depends on Phase 5**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 6.1 | Login Page | `LoginPresenter.java`, `LoginView.java`, `LoginView.ui.xml` | `frontend/src/pages/LoginPage.tsx` | Login form + auth integration |
| 6.2 | Main Page | `MainPresenter.java`, `MainView.java`, `MainView.ui.xml` | `frontend/src/pages/MainPage.tsx` | Item table + CRUD operations |
| 6.3 | Item Dialog | `ItemPresenter.java`, `ItemView.java` | `frontend/src/components/ItemDialog.tsx` | Create/edit modal dialog |

### Phase 7: Integration & Polish
**Priority: Final**

| # | Component | Source | Target | Rationale |
|---|---|---|---|---|
| 7.1 | End-to-End Testing | Original test suite | E2E tests (Cypress or Playwright) | Verify full flow equivalence |
| 7.2 | Build & Deploy Config | `pom.xml` (GWT plugin), JBoss/Jetty config | Multi-module Maven or separate builds | CI/CD pipeline configuration |

---

## 4. Critical Migration Paths

### Path A: Authentication (Highest Risk)
```
BCrypt.java → Spring Security BCryptPasswordEncoder
UserDaoImpl → UserRepository (preserve seed data)
AbstractService.sessionMap → Spring Security session store
UserServiceImpl.login() → AuthController + Spring Security
RpcController.validateSession() → Security filter chain
LoginPresenter → LoginPage.tsx
Cookies (sid) → JWT tokens or session cookies
```

### Path B: Item CRUD (Core Functionality)
```
Item model → Item entity + DTO + TypeScript type
ItemDaoImpl → ItemRepository (preserve in-memory behavior)
ItemServiceImpl → ItemService (remove sessionId parameter)
RpcController (item) → ItemController @RestController
MainPresenter + ItemPresenter → MainPage + ItemDialog
GWT-RPC AsyncCallback → Fetch/Axios Promises
```

### Path C: Navigation & State (UX Continuity)
```
History + ValueChangeHandler → React Router
EventBus → React Context/State
AppController → App.tsx router config
SessionTimedOutException → 401 response → redirect
LoginEvent → AuthContext state update
```

---

## 5. Shared vs. Isolated Components

### Shared Between Client and Server (Source)
These classes live in `com.example.client` but are used by both client and server:
- `Item.java` — serialized over GWT-RPC
- `User.java` — serialized over GWT-RPC
- `UserService.java` — GWT-RPC interface
- `ItemService.java` — GWT-RPC interface
- `LoginFailureException.java` — thrown on server, caught on client
- `SessionTimedOutException.java` — thrown on server, caught on client
- `ItemServiceException.java` — thrown on server, caught on client

**Migration Impact:** In the rewrite, these are SPLIT:
- Backend: Java classes (models, exceptions, service interfaces)
- Frontend: TypeScript interfaces/types (only data shapes, not behavior)
- Contract: REST API JSON schema bridges the two

### Server-Only Components
- `RpcController`, `AbstractService`, `BaseService`
- `UserServiceImpl`, `ItemServiceImpl`
- `UserDao/Impl`, `ItemDao/Impl`
- `BCrypt`
- All XML configuration

### Client-Only Components
- All presenters, views, events, cookies
- `Application.java` (EntryPoint)
- `AppController.java`
- UiBinder XML files
