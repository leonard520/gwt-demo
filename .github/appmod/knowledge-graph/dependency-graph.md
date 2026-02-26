# Dependency Graph

> **Project:** gwt-demo
> **Analysis Date:** Auto-generated during Foundation Phase

---

## 1. Package Dependency Matrix

```
com.example.client                    → com.example.client.cookies
                                      → com.example.client.event
                                      → com.example.client.model
                                      → com.example.client.presenter
                                      → com.example.client.service
                                      → com.example.client.view

com.example.client.presenter          → com.example.client.cookies
                                      → com.example.client.event
                                      → com.example.client.exception
                                      → com.example.client.model
                                      → com.example.client.service
                                      → com.example.client.view (MainView.SelectedItemListener)

com.example.client.view               → com.example.client.model
                                      → com.example.client.presenter (Display interfaces)

com.example.client.event              → com.example.client.model

com.example.client.service            → com.example.client.exception
                                      → com.example.client.model

com.example.client.model              → (no internal deps)

com.example.client.cookies            → (no internal deps)

com.example.client.exception          → com.example.client.service (javadoc only)

com.example.server.controller         → com.example.client.exception
                                      → com.example.client.service
                                      → com.example.server.service

com.example.server.service            → com.example.client.exception
                                      → com.example.client.model
                                      → com.example.client.service
                                      → com.example.server.dao
                                      → com.example.server.security

com.example.server.dao                → com.example.client.model
                                      → com.example.server.security

com.example.server.security           → (no internal deps)
```

---

## 2. Class-Level Dependency Graph

### 2.1 Client Entry & Navigation

```
Application
├── DefaultEventBus (→ EventBus)
├── DefaultCookies (→ Cookies)
└── AppController
    ├── EventBus
    ├── Cookies
    ├── UserServiceAsync (via GWT.create)
    ├── ItemServiceAsync (via GWT.create)
    ├── LoginPresenter
    │   ├── UserServiceAsync
    │   ├── EventBus
    │   ├── Cookies
    │   └── LoginView (→ LoginPresenter.Display)
    │       └── User (model)
    ├── MainPresenter
    │   ├── UserServiceAsync
    │   ├── ItemServiceAsync
    │   ├── EventBus
    │   ├── Cookies
    │   ├── ItemPresenter
    │   │   ├── EventBus
    │   │   └── ItemView (→ ItemPresenter.Display)
    │   │       └── Item (model)
    │   └── MainView (→ MainPresenter.Display)
    │       └── Item (model)
    ├── LoginEvent
    ├── LoginEventHandler
    ├── SessionTimedOutEvent
    └── SessionTimedOutEventHandler
```

### 2.2 Server-Side Processing Chain

```
RpcController (front controller)
├── RemoteService (injected: UserServiceImpl or ItemServiceImpl)
├── BaseService (interface, for session validation)
├── RPCRequest/RPC (GWT server-side)
└── SessionTimedOutException

UserServiceImpl
├── AbstractService
│   └── sessionMap (static Map<String, User>)
├── UserDao (interface)
│   └── UserDaoImpl
│       ├── User (model)
│       └── BCrypt
├── BCrypt
├── LoginFailureException
└── User (model)

ItemServiceImpl
├── AbstractService
│   └── sessionMap
├── ItemDao (interface)
│   └── ItemDaoImpl
│       └── Item (model)
├── ItemServiceException
└── Item (model)
```

---

## 3. Event Flow Graph

```
                    ┌─────────────┐
                    │   LOGIN     │
                    │   FLOW      │
                    └──────┬──────┘
                           │
        LoginPresenter.doLogin()
                           │
                    UserServiceAsync.login()
                           │ (GWT-RPC)
                    UserServiceImpl.login()
                           │
                    BCrypt.checkpw()
                           │
                ┌──────────┴──────────┐
                │ Success             │ Failure
                │                     │
        set cookie "sid"     display.setErrorMsg()
                │
        fire LoginEvent(user)
                │
        ┌───────┴───────┐
        │               │
   AppController    MainPresenter
   .doMain()       .loginHandler
        │               │
   History.newItem   display.setNameText()
   ("main")          itemService.findAll()
        │               │
   mainPresenter    display.setItems()
   .go(container)
```

```
                    ┌─────────────┐
                    │   ITEM      │
                    │   CRUD      │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   New Button         Row Click          Delete Button
        │                  │                  │
   ItemPresenter      ItemPresenter      getSelectedItems()
   .showView(new)     .showView(item)         │
        │                  │            itemService.delete()
        └────────┬─────────┘                  │
                 │                       serviceCallback
          Save clicked                        │
                 │                       refreshClickHandler
        ┌────────┴────────┐
        │                 │
   item.id > 0       item.id == 0
        │                 │
   ItemUpdateEvent   ItemCreateEvent
        │                 │
   MainPresenter     MainPresenter
   .updateHandler    .createHandler
        │                 │
   itemService       itemService
   .update()         .create()
        │                 │
        └────────┬────────┘
                 │
          serviceCallback
                 │
          refreshClickHandler
                 │
          itemService.findAll()
                 │
          display.setItems()
```

```
                    ┌─────────────┐
                    │  SESSION    │
                    │  TIMEOUT   │
                    └──────┬──────┘
                           │
               Any ItemService call fails
               with SessionTimedOutException
                           │
               MainPresenter.handleThrowable()
                           │
               logoutClickHandler.onClick()
               (calls userService.logout, removes cookie)
                           │
               fire SessionTimedOutEvent
                           │
               AppController.doLogin()
                           │
               History.newItem("login")
               loginPresenter.go(container)
```

---

## 4. Request Flow (HTTP Level)

```
Browser → HTTP POST /*.rpc
       → web.xml: DispatcherServlet
       → dispatcher-servlet.xml: SimpleUrlHandlerMapping
           ├── /**/login.rpc → userController (RpcController + UserServiceImpl)
           └── /**/item.rpc  → itemController (RpcController + ItemServiceImpl)
       → RpcController.handleRequest()
           ├── Get/create HttpSession
           ├── Inject session into BaseService
           └── RemoteServiceServlet.doPost()
               → RpcController.processCall()
                   ├── RPC.decodeRequest()
                   ├── validateSession() [skip for UserService]
                   │   └── BaseService.isSessionValid()
                   │       ├── sessionMap.containsKey(sessionId)
                   │       └── httpSession.getId().equals(sessionId) [XSRF]
                   └── RPC.invokeAndEncodeResponse(service, method, params)
       → GWT-RPC encoded response
```

---

## 5. Spring Bean Wiring

```
applicationContext.xml:
┌──────────────┐      ┌──────────────┐
│  userService │─────→│   userDao    │
│ (UserService │      │ (UserDaoImpl)│
│    Impl)     │      └──────────────┘
└──────────────┘

┌──────────────┐      ┌──────────────┐
│  itemService │─────→│   itemDao    │
│ (ItemService │      │ (ItemDaoImpl)│
│    Impl)     │      └──────────────┘
└──────────────┘

dispatcher-servlet.xml:
┌────────────────┐      ┌──────────────┐
│ userController │─────→│  userService │ (ref from appContext)
│ (RpcController)│      └──────────────┘
└────────────────┘

┌────────────────┐      ┌──────────────┐
│ itemController │─────→│  itemService │ (ref from appContext)
│ (RpcController)│      └──────────────┘
└────────────────┘

SimpleUrlHandlerMapping:
  /**/login.rpc → userController
  /**/item.rpc  → itemController
```

---

## 6. External Dependencies (Maven)

| GroupId | ArtifactId | Version | Scope | Migration Action |
|---|---|---|---|---|
| `com.google.gwt` | `gwt-servlet` | 2.0.0 | runtime | **REMOVE** |
| `com.google.gwt` | `gwt-user` | 2.0.0 | provided | **REMOVE** |
| `org.springframework` | `spring-webmvc` | 2.5.6 | compile | **REPLACE** with `spring-boot-starter-web` |
| `org.springframework` | `spring-aop` | 2.5.6 | compile | **REPLACE** — included in Spring Boot starter |
| `log4j` | `log4j` | 1.2.13 | compile | **REPLACE** with SLF4J/Logback (Spring Boot default) |
| `junit` | `junit` | 4.4 | test | **REPLACE** with JUnit 5 (`spring-boot-starter-test`) |
| `org.easymock` | `easymock` | 2.5.1 | test | **REPLACE** with Mockito (`spring-boot-starter-test`) |
| `org.easymock` | `easymockclassextension` | 2.4 | test | **REPLACE** with Mockito |

### New Dependencies (Target)

| GroupId | ArtifactId | Purpose |
|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-web` | Web + REST + embedded Tomcat |
| `org.springframework.boot` | `spring-boot-starter-security` | Authentication + CSRF + BCrypt |
| `org.springframework.boot` | `spring-boot-starter-test` | JUnit 5 + Mockito + AssertJ |
| `org.springframework.boot` | `spring-boot-starter-validation` | Bean validation |

---

## 7. Test Coverage Map

| Test Class | Tests | Source Class |
|---|---|---|
| `BeanTestCase.java` | Base test class | — |
| `ItemTest.java` | Item model tests | `Item.java` |
| `UserTest.java` | User model tests | `User.java` |
| `LoginPresenterTest.java` | Login presenter logic | `LoginPresenter.java` |
| `MainPresenterTest.java` | Main presenter logic | `MainPresenter.java` |
| `ItemPresenterTest.java` | Item presenter logic | `ItemPresenter.java` |
| `ItemServiceImplTest.java` | Item service logic | `ItemServiceImpl.java` |
| `UserServiceImplTest.java` | User service logic | `UserServiceImpl.java` |
| `AbstractServiceTest.java` | Session validation | `AbstractService.java` |
| `TestBCrypt.java` | BCrypt hashing | `BCrypt.java` |
| `RpcControllerTest.java` | RPC controller | `RpcController.java` |
| `MockHasClickHandlers.java` | Test mock | — |
| `MockHasKeyDownHandlers.java` | Test mock | — |
| `MockHasWidgets.java` | Test mock | — |

**Coverage Areas Preserved in Rewrite:**
- Model validation (Item, User) → JUnit 5 tests
- Service logic (create, read, update, delete, login, logout) → JUnit 5 + Mockito
- Session validation → Spring Security integration tests
- BCrypt verification → Covered by Spring Security tests
- Controller logic (formerly RpcController) → `@WebMvcTest` integration tests
- Presenter logic → React component tests (Jest + React Testing Library)
