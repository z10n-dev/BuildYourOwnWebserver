---
sidebar_position: 2
---

# SSE (Server-Sent Events)

The SSE endpoint maintains a persistent HTTP connection and streams real-time events from the server to the client. It powers the Glass Box dashboard with live logs, metrics, and connection status.

**Endpoint:** `GET /api/sse`

## Connecting

The client opens a standard HTTP request to `/api/sse`. The server responds with `Content-Type: text/event-stream` and keeps the connection open indefinitely.

```bash
curl -N http://localhost:8080/api/sse
```

### Response Headers

```http
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
Access-Control-Allow-Origin: *
```

## Event Format

Each event follows the [SSE specification](https://html.spec.whatwg.org/multipage/server-sent-events.html):

```
event: <event-type>
data: <payload>

```

Events are terminated by a blank line (`\n\n`), which signals the browser to dispatch the event.

## Event Types

### `connected`

Sent immediately when a client connects and whenever the client count changes (connect/disconnect). The data payload is the current number of connected SSE clients.

```
event: connected
data: 3
```

### `heartbeat`

Sent every second to keep the connection alive. The data payload is the current client count.

```
event: heartbeat
data: 3
```

### `log`

Sent whenever the server logs a message with destination `CLIENT` or `EVERYWHERE`. The data payload is a JSON object.

```
event: log
data: {"level":"INFO","message":"GET /api/todos from /127.0.0.1:54321","timestamp":1700000000000}
```

**Log data fields:**

| Field | Type | Description |
|-------|------|-------------|
| `level` | `string` | Log level: `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL` |
| `message` | `string` | The log message |
| `timestamp` | `number` | Unix timestamp in milliseconds |

### `stats`

Sent every second alongside the heartbeat. Contains current server metrics as JSON.

```
event: stats
data: {"uptime":120000,"totalRequests":42,"activeConnections":3,"cpuUsage":0.15,"memoryUsage":2.4}
```

**Stats data fields:**

| Field | Type | Description |
|-------|------|-------------|
| `uptime` | `number` | Server uptime in milliseconds |
| `totalRequests` | `number` | Total HTTP requests since startup |
| `activeConnections` | `number` | Current SSE client count |
| `cpuUsage` | `number` | System CPU load (0.0 – 1.0) |
| `memoryUsage` | `number` | Used memory in GB |

## JavaScript Client Example

```javascript
const evtSource = new EventSource("/api/sse");

evtSource.addEventListener("connected", (e) => {
  console.log("Connected clients:", e.data);
});

evtSource.addEventListener("log", (e) => {
  const log = JSON.parse(e.data);
  console.log(`[${log.level}] ${log.message}`);
});

evtSource.addEventListener("stats", (e) => {
  const stats = JSON.parse(e.data);
  console.log("Uptime:", stats.uptime, "ms");
  console.log("Requests:", stats.totalRequests);
  console.log("CPU:", (stats.cpuUsage * 100).toFixed(1) + "%");
});

evtSource.addEventListener("heartbeat", (e) => {
  // Connection alive
});

evtSource.onerror = () => {
  console.error("SSE connection lost, reconnecting...");
};
```

:::tip
The browser's `EventSource` API automatically reconnects if the connection is lost.
:::

## Connection Lifecycle

1. Client sends `GET /api/sse`
2. Server sends response headers and adds the socket to the client set
3. Server broadcasts a `connected` event to all clients with the new count
4. Every second, the server sends `heartbeat` and `stats` events
5. Log events are sent as they occur
6. When the client disconnects (or the socket errors), the server removes it from the set and broadcasts an updated `connected` count

## Extending with Custom Events

The `SSEEvent` enum defines the available event types:

```java
public enum SSEEvent {
    LOG,
    STATS,
    CONNECTED,
    HEARTBEAT
}
```

To add a custom event type:

1. Add a new value to the `SSEEvent` enum
2. Call `broadcast()` on the `SSEHandler` with your new event type:

```java
sseHandler.broadcast(SSEEvent.MY_EVENT, "custom payload");
```

The event name sent to clients will be the lowercase enum name (e.g., `my_event`).
