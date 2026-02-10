---
sidebar_position: 1
---

# ToDo API

The ToDo API provides a RESTful interface for managing to-do items. It demonstrates CRUD operations, JSON serialization, route-based handling, and CORS support.

**Base path:** `/api/todos`

:::info
ToDo items are stored in-memory. Data is lost when the server restarts.
:::

## Endpoints

### `GET /api/todos`

Retrieves all to-do items.

**Response:** `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Learn HTTP",
    "completed": false
  }
]
```

**Example:**

```bash
curl http://localhost:8080/api/todos
```

---

### `POST /api/todos`

Creates a new to-do item. The server generates a UUID for the `id` field.

**Request Body:**

```json
{
  "title": "Learn HTTP",
  "completed": false
}
```

**Response:** `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Learn HTTP",
  "completed": false
}
```

**Response Headers:**
- `Location: /api/todos/{id}` — URI of the created resource

**Example:**

```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn HTTP", "completed": false}'
```

---

### `PUT /api/todos/:id`

Updates an existing to-do item by its UUID.

**Path Parameters:**
- `id` (UUID) — The to-do item identifier

**Request Body:**

```json
{
  "title": "Learn HTTP/1.1",
  "completed": true
}
```

**Response:** `200 OK`

**Error:** `404 Not Found` if the ID does not exist.

**Example:**

```bash
curl -X PUT http://localhost:8080/api/todos/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn HTTP/1.1", "completed": true}'
```

---

### `DELETE /api/todos/:id`

Deletes a to-do item by its UUID.

**Path Parameters:**
- `id` (UUID) — The to-do item identifier

**Response:** `200 OK`

**Error:** `404 Not Found` if the ID does not exist.

**Example:**

```bash
curl -X DELETE http://localhost:8080/api/todos/550e8400-e29b-41d4-a716-446655440000
```

## CORS

All ToDo endpoints include the `Access-Control-Allow-Origin: *` header, allowing cross-origin requests from any domain.

Preflight `OPTIONS` requests are handled automatically:

| Endpoint | Allowed Methods |
|---|---|
| `OPTIONS /api/todos` | `GET, POST, OPTIONS` |
| `OPTIONS /api/todos/:id` | `PUT, DELETE, OPTIONS` |

Preflight responses include:
- `Access-Control-Allow-Headers: Content-Type`
- `Access-Control-Max-Age: 86400` (24 hours)

## Data Model

The `ToDo` record is defined as:

```java
public record ToDo(UUID id, String title, boolean completed) {}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Server-generated unique identifier |
| `title` | `String` | Title or description of the task |
| `completed` | `boolean` | Whether the task is marked as done |
