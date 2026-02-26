# Component Inventory

> **Project:** gwt-demo
> **Analysis Date:** Auto-generated during Foundation Phase
> **Total Source Files:** 41 Java files (27 main + 14 test), 7 XML configs, 2 HTML files, 2 properties files

---

## 1. Component Summary

| Layer | Package | Components | Purpose |
|---|---|---|---|
| **Entry Point** | `com.example.client` | 2 classes | Application bootstrap, navigation control |
| **Presenters** | `com.example.client.presenter` | 5 classes (2 interfaces + 3 implementations) | MVP presentation logic |
| **Views** | `com.example.client.view` | 3 classes + 2 UiBinder XML | GWT UI rendering |
| **Models** | `com.example.client.model` | 2 classes | Domain objects (Item, User) |
| **Client Services** | `com.example.client.service` | 4 interfaces | GWT-RPC service contracts |
| **Events** | `com.example.client.event` | 10 classes (5 events + 5 handlers/interfaces) | Client-side event bus system |
| **Exceptions** | `com.example.client.exception` | 3 classes | GWT-serializable exceptions |
| **Cookies** | `com.example.client.cookies` | 2 classes (1 interface + 1 impl) | Cookie management abstraction |
| **Server Controller** | `com.example.server.controller` | 1 class | GWT-RPC front controller |
| **Server Services** | `com.example.server.service` | 4 classes (1 interface + 1 abstract + 2 impls) | Business logic |
| **DAOs** | `com.example.server.dao` | 4 classes (2 interfaces + 2 impls) | Data access (in-memory) |
| **Security** | `com.example.server.security` | 1 class | BCrypt password hashing |
| **Configuration** | XML files | 4 config files | Spring, servlet, URL rewrite config |

---

## 2. Detailed Component Catalog

### 2.1 Entry Point Layer

#### `com.example.client.Application`
- **Type:** GWT EntryPoint
- **Role:** Application bootstrap — creates EventBus, Cookies, AppController
- **Dependencies:** `DefaultEventBus`, `DefaultCookies`, `AppController`, `RootLayoutPanel`
- **GWT APIs Used:** `EntryPoint`, `RootLayoutPanel`
- **Migration Target:** `frontend/src/App.tsx` (React root component)

#### `com.example.client.AppController`
- **Type:** Presenter (without view) + ValueChangeHandler
- **Role:** Central navigation controller, history management, session recovery
- **Dependencies:** `EventBus`, `Cookies`, `UserServiceAsync`, `ItemServiceAsync`, `LoginPresenter`, `MainPresenter`, `ItemPresenter`, all Views
- **Key Behaviors:**
  - Listens to GWT `History` changes (ValueChangeHandler)
  - Handles `LoginEvent` → navigates to main view
  - Handles `SessionTimedOutEvent` → navigates to login
  - On startup: checks cookie `sid` → calls `userService.isLoggedIn()` to recover session
  - Creates all presenters and services via `GWT.create()`
- **GWT APIs Used:** `History`, `ValueChangeHandler`, `GWT.create()`, `AsyncCallback`, `HasWidgets`
- **Migration Target:** React Router configuration + `AuthContext` provider in `App.tsx`

---

### 2.2 Presenter Layer (MVP Pattern)

#### `com.example.client.presenter.Presenter` (Interface)
- **Type:** Interface
- **Methods:** `void go(HasWidgets container)`
- **Implementors:** `AppController`, `LoginPresenter`, `MainPresenter`, `ItemPresenter`
- **Migration Target:** React component lifecycle (mounting into DOM)

#### `com.example.client.presenter.WidgetDisplay` (Interface)
- **Type:** Interface
- **Methods:** `Widget asWidget()`
- **Purpose:** Root interface for all Presenter Display interfaces
- **Implementors:** `LoginPresenter.Display`, `MainPresenter.Display`, `ItemPresenter.Display`
- **Migration Target:** Eliminated — React components are their own "display"

#### `com.example.client.presenter.LoginPresenter`
- **Type:** Presenter implementation
- **Inner Interface:** `Display` with methods: `getUser()`, `loginButton()`, `keyDownHandlers()`, `setErrorMsg(String)`
- **Dependencies:** `UserServiceAsync`, `EventBus`, `Cookies`, `Display`
- **Key Behaviors:**
  - Binds click handler on login button → calls `doLogin()`
  - Binds key-down handler on input fields → ENTER triggers login
  - Validates `User.isValid()` before RPC call
  - On success: sets cookie `sid` with 14-day expiry, fires `LoginEvent`
  - On failure: displays error message from server
- **GWT APIs Used:** `ClickHandler`, `KeyDownHandler`, `KeyCodes`, `AsyncCallback`
- **Migration Target:** `frontend/src/pages/LoginPage.tsx` with `useAuth()` hook

#### `com.example.client.presenter.MainPresenter`
- **Type:** Presenter implementation + `SelectedItemListener`
- **Inner Interface:** `Display` with methods: `setNameText()`, `logoutLink()`, `refresh()`, `delete()`, `newItemClick()`, `setItems()`, `getSelectedItems()`, `setErrorMsg()`, `setSelectedItemListener()`
- **Dependencies:** `UserServiceAsync`, `ItemServiceAsync`, `EventBus`, `Cookies`, `ItemPresenter`, `Display`
- **Key Behaviors:**
  - On `LoginEvent`: updates welcome text, fetches all items via `itemService.findAll()`
  - Logout: calls `userService.logout()`, removes `sid` cookie
  - Refresh: re-fetches all items
  - Delete: sends selected items to `itemService.delete()`
  - New: opens `ItemPresenter` with empty Item
  - On `ItemCreateEvent`: calls `itemService.create()`
  - On `ItemUpdateEvent`: calls `itemService.update()`
  - On `SessionTimedOutException`: triggers logout + fires `SessionTimedOutEvent`
  - On row select: opens `ItemPresenter` with selected Item
- **GWT APIs Used:** `ClickHandler`, `AsyncCallback`, `HasClickHandlers`
- **Migration Target:** `frontend/src/pages/MainPage.tsx` with CRUD hooks

#### `com.example.client.presenter.ItemPresenter`
- **Type:** Presenter implementation
- **Inner Interface:** `Display` with methods: `getItem()`, `save()`, `cancel()`, `removePopUp()`, `showPopUp(Item)`
- **Dependencies:** `EventBus`, `Display`
- **Key Behaviors:**
  - `showView(Item)`: displays popup for create/edit
  - Save: if `item.getId() > 0` → fires `ItemUpdateEvent`; else → fires `ItemCreateEvent`
  - Cancel: removes popup
- **GWT APIs Used:** `ClickHandler`, `HasClickHandlers`
- **Migration Target:** `frontend/src/components/ItemDialog.tsx` (modal dialog)

---

### 2.3 View Layer

#### `com.example.client.view.LoginView`
- **Type:** GWT Composite implementing `LoginPresenter.Display`
- **UiBinder:** `LoginView.ui.xml`
- **UI Widgets:** `DecoratorPanel`, `TextBox` (username), `PasswordTextBox` (password), `Button` (login), `Label` (error)
- **Layout:** Centered panel at 35% left, 50px top
- **Migration Target:** React LoginPage component with form inputs

#### `com.example.client.view.MainView`
- **Type:** GWT ResizeComposite implementing `MainPresenter.Display`
- **UiBinder:** `MainView.ui.xml`
- **UI Widgets:** `DockLayoutPanel`, `Label` (name), `Hyperlink` (logout), `FlexTable` (header + data), `Button` (refresh, delete, new), `Label` (error), `CheckBox` (per row)
- **Inner Interfaces:** `SelectionStyle` (CSS), `SelectedItemListener`
- **Layout:** Header (welcome + logout), Center (table with columns: Name 128px, Description 192px, Date 256px, Checkbox 25px), Footer (buttons + error)
- **Key Behaviors:** Row selection styling, checkbox selection for multi-delete, item map by row index
- **Migration Target:** React MainPage with HTML table or data grid component

#### `com.example.client.view.ItemView`
- **Type:** GWT Composite implementing `ItemPresenter.Display` (no UiBinder — programmatic UI)
- **UI Widgets:** `DialogBox`, `FlexTable`, `TextBox` (name, description), `DatePicker`, `Button` (save, cancel), `VerticalPanel`
- **Key Behaviors:** Shows as modal dialog (`DialogBox.center().show()`), populates fields from Item, glass overlay enabled
- **Migration Target:** React modal/dialog component with form fields

---

### 2.4 Model Layer

#### `com.example.client.model.Item`
- **Type:** Domain model, implements `IsSerializable`
- **Fields:** `id` (long), `name` (String), `description` (String), `date` (Date)
- **Constructors:** No-arg (required by GWT-RPC), 3-arg (name, description, date)
- **Methods:** Getters/setters (all `final`), `equals()`, `hashCode()`, `toString()`
- **Migration Target:** Backend `Item.java` entity + Frontend `Item` TypeScript interface

#### `com.example.client.model.User`
- **Type:** Domain model, implements `IsSerializable`
- **Fields:** `username` (String), `password` (String)
- **Constructors:** No-arg (required by GWT-RPC), 2-arg (username, password)
- **Methods:** `isValid()` (both fields non-null/non-empty), `clone()` (copies username only, NOT password), getters/setters (all `final`), `equals()`, `hashCode()`, `toString()` (masks password)
- **Security Note:** `clone()` explicitly omits password — this pattern MUST be preserved
- **Migration Target:** Backend `User.java` entity with `@JsonIgnore` on password + Frontend `User` TypeScript interface

---

### 2.5 Client Service Layer (GWT-RPC Interfaces)

#### `com.example.client.service.UserService`
- **Type:** GWT `RemoteService` interface
- **RPC Path:** `login.rpc`
- **Methods:**
  - `String login(User user) throws LoginFailureException` — returns sessionId
  - `void logout(String sessionId)`
  - `User isLoggedIn(String sessionId) throws LoginFailureException` — returns User if valid
- **Migration Target:** `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`

#### `com.example.client.service.UserServiceAsync`
- **Type:** GWT async counterpart of UserService
- **Methods:** Same as UserService but with `AsyncCallback<T>` parameter
- **Migration Target:** Eliminated — replaced by REST API calls returning Promises

#### `com.example.client.service.ItemService`
- **Type:** GWT `RemoteService` interface
- **RPC Path:** `item.rpc`
- **Methods:**
  - `ArrayList<Item> findAll(String sessionId) throws SessionTimedOutException, ItemServiceException`
  - `void create(String sessionId, Item item) throws SessionTimedOutException, ItemServiceException`
  - `void update(String sessionId, Item item) throws SessionTimedOutException, ItemServiceException`
  - `void delete(String sessionId, ArrayList<Item> items) throws SessionTimedOutException, ItemServiceException`
- **Note:** All methods take `sessionId` as first param for session validation in `RpcController`
- **Migration Target:** `GET /api/items`, `POST /api/items`, `PUT /api/items/{id}`, `DELETE /api/items`

#### `com.example.client.service.ItemServiceAsync`
- **Type:** GWT async counterpart of ItemService
- **Migration Target:** Eliminated — replaced by REST API calls returning Promises

---

### 2.6 Event System

#### `com.example.client.event.EventBus` (Interface)
- **Type:** Custom interface wrapping GWT `HandlerManager` API
- **Methods:** `addHandler()`, `fireEvent()`, `getHandler()`, `getHandlerCount()`, `isEventHandled()`, `removeHandler()`
- **Migration Target:** React Context API + state management

#### `com.example.client.event.DefaultEventBus`
- **Type:** Extends `HandlerManager` implements `EventBus`
- **Migration Target:** Eliminated

#### Events and Handlers:

| Event Class | Handler Interface | Payload | Fired By | Consumed By |
|---|---|---|---|---|
| `LoginEvent` | `LoginEventHandler` | `User` | `LoginPresenter` (on login success), `AppController` (on session recovery) | `AppController` (navigate to main), `MainPresenter` (fetch items) |
| `SessionTimedOutEvent` | `SessionTimedOutEventHandler` | None | `MainPresenter` (on session timeout) | `AppController` (navigate to login) |
| `ItemCreateEvent` | `ItemCreateEventHandler` | `Item` | `ItemPresenter` (save new item) | `MainPresenter` (call create API) |
| `ItemUpdateEvent` | `ItemUpdateEventHandler` | `Item` | `ItemPresenter` (save existing item) | `MainPresenter` (call update API) |

**Migration Target:** These events map to:
- `LoginEvent` → Auth context state change + React Router navigation
- `SessionTimedOutEvent` → 401 response handler → redirect to login
- `ItemCreateEvent` → Direct API call from ItemDialog callback → refresh list
- `ItemUpdateEvent` → Direct API call from ItemDialog callback → refresh list

---

### 2.7 Exception Classes

| Exception | Implements | Default Message | Thrown By |
|---|---|---|---|
| `LoginFailureException` | `IsSerializable` | "The email or password you entered is incorrect." | `UserServiceImpl.login()`, `UserServiceImpl.isLoggedIn()` |
| `SessionTimedOutException` | `IsSerializable` | "The session has timed out. You will need to login again." | `RpcController.validateSession()` |
| `ItemServiceException` | `IsSerializable` | "Oops, we're having problems with the server. Try again later." | `ItemServiceImpl.*()` |

**Migration Target:** Custom exception classes + `@ControllerAdvice` global exception handler returning structured JSON error responses.

---

### 2.8 Cookie Management

#### `com.example.client.cookies.Cookies` (Interface)
- **Purpose:** Abstraction over `com.google.gwt.user.client.Cookies` for testability
- **Methods:** `getCookie()`, `setCookie()`, `removeCookies()`, `isCookieEnabled()`, etc.

#### `com.example.client.cookies.DefaultCookies`
- **Purpose:** Delegates all calls to GWT's static `Cookies` class
- **Usage:** Session ID stored as cookie `sid` with 14-day expiry

**Migration Target:** Browser cookie handling or localStorage in React auth utilities.

---

### 2.9 Server Controller

#### `com.example.server.controller.RpcController`
- **Type:** Extends `RemoteServiceServlet` implements `Controller`, `ServletContextAware`
- **Role:** Front-door controller for ALL GWT-RPC traffic
- **Key Behaviors:**
  - `handleRequest()`: Sets `HttpSession` on service, delegates to `doPost()`
  - `processCall()`: Decodes RPC request, validates session (except for UserService), invokes service method
  - `validateSession()`: Checks `params[0]` (sessionId) against `BaseService.isSessionValid()`
  - Spring-injected `RemoteService service` and `serviceClass`
- **Wiring:** Two instances in `dispatcher-servlet.xml`:
  - `userController` → maps `/**/login.rpc` → injects `userService`
  - `itemController` → maps `/**/item.rpc` → injects `itemService`
- **Migration Target:** Eliminated entirely. Replaced by `@RestController` classes with Spring Security filter chain.

---

### 2.10 Server Service Layer

#### `com.example.server.service.BaseService` (Interface)
- **Methods:** `setHttpSession(HttpSession)`, `isSessionValid(String sessionId)`
- **Purpose:** Contract for session injection and validation

#### `com.example.server.service.AbstractService`
- **Type:** Abstract base implementing `BaseService`
- **State:** `protected HttpSession httpSession`, `protected static Map<String, User> sessionMap`
- **Key Logic:** `isSessionValid()` checks sessionMap + XSRF check (httpSession.getId() matches sessionId)
- **Migration Target:** Spring Security session management (eliminates manual session tracking)

#### `com.example.server.service.UserServiceImpl`
- **Type:** Extends `AbstractService`, implements `UserService`, `HttpSessionBindingListener`
- **Dependencies:** `UserDao`, `BCrypt`
- **Key Behaviors:**
  - `login()`: Validates user, looks up via DAO, checks BCrypt password, stores in sessionMap, returns session ID
  - `logout()`: Validates session, removes from sessionMap, invalidates HttpSession
  - `isLoggedIn()`: Checks session validity, returns user from sessionMap
  - `setHttpSession()`: Also registers self as `HttpSessionBindingListener` to clean up expired sessions
  - `valueUnbound()`: Auto-removes expired sessions from sessionMap
- **Injected by:** Spring XML — `userDao` property
- **Migration Target:** `UserService` + `AuthController` in Spring Boot with Spring Security

#### `com.example.server.service.ItemServiceImpl`
- **Type:** Extends `AbstractService`, implements `ItemService`
- **Dependencies:** `ItemDao`
- **Methods:** `create()`, `delete()`, `findAll()`, `update()` — all delegate to DAO, wrap exceptions in `ItemServiceException`
- **Note:** Session validation is handled by `RpcController`, not in this service
- **Injected by:** Spring XML — `itemDao` property
- **Migration Target:** `ItemService` in Spring Boot (session validation moves to Security filter)

---

### 2.11 DAO Layer

#### `com.example.server.dao.UserDao` (Interface)
- **Methods:** `User findByUserName(String username)`
- **Migration Target:** `UserRepository` (Spring Data JPA or in-memory equivalent)

#### `com.example.server.dao.UserDaoImpl`
- **Type:** In-memory mock implementation
- **Storage:** `HashMap<String, User>`
- **Seed Data:** `davis/davis`, `homer/homer` (BCrypt-hashed passwords)
- **Migration Target:** `UserRepository` with same seed data for development

#### `com.example.server.dao.ItemDao` (Interface)
- **Methods:** `findAll()`, `create(Item)`, `update(Item)`, `delete(ArrayList<Item>)`
- **Migration Target:** `ItemRepository` (Spring Data JPA or in-memory equivalent)

#### `com.example.server.dao.ItemDaoImpl`
- **Type:** In-memory mock implementation
- **Storage:** `ConcurrentHashMap<Long, Item>` with `AtomicLong` sequence
- **Seed Data:** 9 mock items with random dates
- **Key Logic:** Assigns auto-incrementing IDs on `create()`, null-safe operations
- **Migration Target:** `ItemRepository` with same seed data and ID generation

---

### 2.12 Security

#### `com.example.server.security.BCrypt`
- **Type:** Third-party BCrypt implementation (Damien Miller, ISC license)
- **Methods:** `hashpw(String password, String salt)`, `checkpw(String plaintext, String hashed)`, `gensalt()`, `gensalt(int log_rounds)`
- **Usage:** `UserDaoImpl` hashes passwords on init; `UserServiceImpl.login()` verifies passwords
- **Migration Target:** Spring Security `BCryptPasswordEncoder` (compatible $2a format)

---

### 2.13 Configuration Files

#### `conf/applicationContext.xml`
- **Type:** Spring application context (beans 2.0 schema)
- **Beans Defined:**
  - `userService` → `UserServiceImpl` (depends on `userDao`)
  - `itemService` → `ItemServiceImpl` (depends on `itemDao`)
  - `userDao` → `UserDaoImpl`
  - `itemDao` → `ItemDaoImpl`
- **Migration Target:** Spring Boot auto-configuration + `@Service`, `@Repository` annotations

#### `war/WEB-INF/dispatcher-servlet.xml`
- **Type:** Spring MVC dispatcher configuration
- **URL Mappings:**
  - `/**/login.rpc` → `userController` (RpcController with userService)
  - `/**/item.rpc` → `itemController` (RpcController with itemService)
- **Migration Target:** `@RestController` with `@RequestMapping` annotations

#### `war/WEB-INF/web.xml`
- **Type:** Servlet 2.4 web application descriptor
- **Configuration:**
  - Session timeout: 20 minutes
  - Spring `ContextLoaderListener`
  - `dispatcher` servlet mapping `*.rpc`
  - Welcome file: `Application.html`
- **Migration Target:** `application.properties` in Spring Boot

#### `war/WEB-INF/urlrewrite.xml`
- **Type:** Tuckey URL rewrite filter configuration
- **Rules:** Maps `/user/` URLs to GWT application HTML
- **Migration Target:** React Router (client-side) + Spring Boot request mappings (server-side)

#### `war/WEB-INF/jboss-web.xml`
- **Type:** JBoss deployment descriptor (empty)
- **Migration Target:** Eliminated

#### `src/main/resources/com/example/Application.gwt.xml`
- **Type:** GWT module descriptor
- **Configuration:** Inherits `com.google.gwt.user.User`, entry point `com.example.client.Application`, standard CSS theme
- **Migration Target:** Eliminated

#### `conf/log4j.properties`
- **Type:** Log4j 1.2 configuration
- **Configuration:** Console appender, debug level for `com.example`, info for root
- **Migration Target:** `application.properties` with `logging.level.com.example=DEBUG`
