# Data Model: GWT-to-React+SpringBoot Rewrite

**Feature**: gwt-to-react-springboot
**Date**: 2025-01-30
**Source**: Spec REQ-018, REQ-019, REQ-020 + Knowledge Graph component-inventory.md

---

## 1. Domain Entities

### 1.1 Item

**Source**: `com.example.client.model.Item` (implements `IsSerializable`)
**Requirements**: REQ-018

| Field | Type (Backend) | Type (Frontend) | Type (JSON) | Constraints | Notes |
|---|---|---|---|---|---|
| `id` | `long` | `number` | `number` | Auto-assigned by server | `AtomicLong.incrementAndGet()` pattern |
| `name` | `String` | `string` | `string` | — | |
| `description` | `String` | `string` | `string` | — | |
| `date` | `LocalDate` | `string` (ISO-8601) | `"YYYY-MM-DD"` | Default: current date | Source used `java.util.Date` |

**Backend Entity** (`com.example.model.Item`):
```java
public class Item {
    private long id;
    private String name;
    private String description;
    private LocalDate date;

    // No-arg constructor (for Jackson)
    // 3-arg constructor: (name, description, date)
    // Getters/setters (all final in source — keep immutable style)
    // equals() and hashCode() — include all fields
    // toString() — include all fields
}
```

**Frontend Interface** (`types.ts`):
```typescript
export interface Item {
  id: number;
  name: string;
  description: string;
  date: string; // ISO-8601 format "YYYY-MM-DD"
}
```

**Behavioral Contracts**:
- `equals()` and `hashCode()` include all four fields (id, name, description, date) — matching source
- Server assigns ID on create (client sends item without `id` or with `id = 0`)
- Update is keyed on ID; items with non-existent IDs are silently ignored (matching `ItemDaoImpl.update()`)
- Jackson serialization uses ISO-8601 for `LocalDate`

---

### 1.2 User

**Source**: `com.example.client.model.User` (implements `IsSerializable`)
**Requirements**: REQ-019, REQ-005

| Field | Type (Backend) | Type (Frontend) | Type (JSON) | Constraints | Notes |
|---|---|---|---|---|---|
| `username` | `String` | `string` | `string` | Required, non-null, non-empty | Unique identifier |
| `password` | `String` | — (never exposed) | — (never serialized) | Required for login | `@JsonIgnore` on serialization |

**Backend Entity** (`com.example.model.User`):
```java
public class User {
    private String username;
    
    @JsonIgnore // NEVER expose in API responses (Constitution P3, BC1)
    private String password;

    // No-arg constructor
    // 2-arg constructor: (username, password)
    
    public boolean isValid() {
        return username != null && !username.isEmpty()
            && password != null && !password.isEmpty();
    }
    
    // equals() and hashCode() — include both username and password
    // toString() — masks password as "*******" (source behavior)
}
```

**Frontend Interface** (`types.ts`):
```typescript
export interface User {
  username: string;
  // No password field — never exposed in API responses
}
```

**Behavioral Contracts**:
- `isValid()`: returns `true` only when both `username` and `password` are non-null AND non-empty
- Password is NEVER included in API responses (source: `User.clone()` explicitly omits password)
- `toString()` masks password as `*******` (matching source behavior)
- `equals()` and `hashCode()` include both fields (matching source)

---

## 2. DTOs (Data Transfer Objects)

### 2.1 LoginRequest

**Used by**: `POST /api/auth/login` (REQ-021)

```java
public class LoginRequest {
    private String username;
    private String password;
    // Getters/setters, validation
}
```

```typescript
export interface LoginRequest {
  username: string;
  password: string;
}
```

### 2.2 LoginResponse

**Used by**: `POST /api/auth/login` response (REQ-021)

```java
public class LoginResponse {
    private String token;   // Session ID
    private User user;      // Username only (password excluded by @JsonIgnore)
}
```

```typescript
export interface LoginResponse {
  token: string;
  user: User; // { username: string }
}
```

### 2.3 ErrorResponse

**Used by**: All error responses (REQ-016)

```java
public class ErrorResponse {
    private String timestamp;  // ISO-8601
    private int status;        // HTTP status code
    private String error;      // Error type (e.g., "Unauthorized")
    private String message;    // Human-readable message
    private String path;       // Request path
}
```

```typescript
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
```

### 2.4 DeleteRequest

**Used by**: `DELETE /api/items` request body (REQ-022)

```java
public class DeleteRequest {
    private List<Long> ids;
}
```

```typescript
export interface DeleteRequest {
  ids: number[];
}
```

---

## 3. Seed Data

**Requirements**: REQ-020

### 3.1 Users

| Username | Password (plaintext) | Password (stored) |
|---|---|---|
| `davis` | `davis` | BCrypt hash (`$2a$10$...`) |
| `homer` | `homer` | BCrypt hash (`$2a$10$...`) |

Hashed at application startup using `BCryptPasswordEncoder.encode()`.

### 3.2 Items

| ID | Name | Description | Date |
|---|---|---|---|
| 1 | Item 1 | First item description | Random date |
| 2 | Item 2 | Second item description | Random date |
| 3 | Foo | Foo description | Random date |
| 4 | Bar | Bar description | Random date |
| 5 | Baz | Baz description | Random date |
| 6 | Widget | Widget description | Random date |
| 7 | FooBar | FooBar description | Random date |
| 8 | BarFoo | BarFoo description | Random date |
| 9 | FooBaz | FooBaz description | Random date |

IDs auto-assigned via `AtomicLong` starting at 1.

---

## 4. Data Storage

**Implementation**: In-memory stores (matching source behavior, Constitution R3)

### 4.1 UserRepository
- Storage: `Map<String, User>` keyed by username
- Source: `UserDaoImpl` used `HashMap<String, User>`
- Operations: `findByUsername(String username) → Optional<User>`

### 4.2 ItemRepository
- Storage: `ConcurrentHashMap<Long, Item>` with `AtomicLong` ID sequence
- Source: `ItemDaoImpl` used `ConcurrentHashMap<Long, Item>` + `AtomicLong`
- Operations:
  - `findAll() → List<Item>`
  - `save(Item item) → Item` (assigns ID if new)
  - `update(Item item)` (no-op if ID not found)
  - `deleteAll(List<Long> ids)`

---

## 5. Relationships

```
User ──(authenticates via)──→ Session (Spring Security)
User ──(no direct relationship)──→ Item
Item ──(belongs to global store)──→ ItemRepository
```

**Note**: In the source application, Items are NOT user-scoped. All users see the same items. This is preserved in the rewrite.
