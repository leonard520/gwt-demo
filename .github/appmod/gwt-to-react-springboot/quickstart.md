# Quick Start Guide: GWT-to-React+SpringBoot Rewrite

**Feature**: gwt-to-react-springboot
**Date**: 2025-01-30

---

## Prerequisites

- Java 17+ (JDK installed)
- Maven 3.9+
- Node.js 18+ with npm
- Git

---

## 1. Backend Quick Start

### Setup

```bash
cd backend
mvn clean compile
```

### Run

```bash
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`.

### Verify

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"davis","password":"davis"}' \
  -c cookies.txt

# Get current user (session recovery)
curl http://localhost:8080/api/auth/me \
  -b cookies.txt

# List items
curl http://localhost:8080/api/items \
  -b cookies.txt

# Create item
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Item","description":"Test description","date":"2025-01-30"}' \
  -b cookies.txt

# Update item (ID 1)
curl -X PUT http://localhost:8080/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Item","description":"Updated description","date":"2025-02-01"}' \
  -b cookies.txt

# Delete items (batch)
curl -X DELETE http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"ids":[1,2]}' \
  -b cookies.txt

# Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

### Run Tests

```bash
mvn test
```

---

## 2. Frontend Quick Start

### Setup

```bash
cd frontend
npm install
```

### Run (Development)

```bash
npm run dev
```

Frontend starts on `http://localhost:5173` with proxy to backend on `http://localhost:8080`.

### Build (Production)

```bash
npm run build
```

Output: `frontend/dist/` (static files for deployment)

### Run Tests

```bash
npm test
```

---

## 3. Full Integration Flow

1. **Start backend**: `cd backend && mvn spring-boot:run`
2. **Start frontend**: `cd frontend && npm run dev`
3. **Open browser**: Navigate to `http://localhost:5173`
4. **Login**: Use `davis/davis` or `homer/homer`
5. **Verify**:
   - 9 seed items visible in table
   - Welcome message shows username
   - Create new item via "New" button
   - Edit item by clicking a table row
   - Delete items by checking checkboxes and clicking "Delete"
   - Refresh items via "Refresh" button
   - Logout via logout link

---

## 4. API Contract Summary

| Method | Endpoint | Auth Required | Request Body | Response |
|---|---|---|---|---|
| POST | `/api/auth/login` | No | `{ username, password }` | `{ token, user: { username } }` (200) |
| POST | `/api/auth/logout` | Yes | — | 204 No Content |
| GET | `/api/auth/me` | Yes | — | `{ username }` (200) |
| GET | `/api/items` | Yes | — | `[{ id, name, description, date }]` (200) |
| POST | `/api/items` | Yes | `{ name, description, date }` | `{ id, name, description, date }` (201) |
| PUT | `/api/items/{id}` | Yes | `{ name, description, date }` | `{ id, name, description, date }` (200) |
| DELETE | `/api/items` | Yes | `{ ids: [1, 2, ...] }` | 204 No Content |

---

## 5. Seed Data

### Users
| Username | Password |
|---|---|
| `davis` | `davis` |
| `homer` | `homer` |

### Items (9 seed items)
Item 1, Item 2, Foo, Bar, Baz, Widget, FooBar, BarFoo, FooBaz

---

## 6. Error Response Format

All errors return:
```json
{
  "timestamp": "2025-01-30T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "The email or password you entered is incorrect.",
  "path": "/api/auth/login"
}
```

| Scenario | Status | Message |
|---|---|---|
| Wrong password | 401 | "The email or password you entered is incorrect." |
| User not found | 401 | "Sorry, we couldn't locate you in our records." |
| Session expired | 401 | "The session has timed out. You will need to login again." |
| Server error | 500 | "Oops, we're having problems with the server. Try again later." |

---

## 7. Configuration

### Backend (`application.properties`)

```properties
server.port=8080
server.servlet.session.timeout=20m
logging.level.com.example=DEBUG
logging.level.root=INFO
app.cors.allowed-origins=http://localhost:5173
```

### Frontend (`vite.config.ts`)

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```
