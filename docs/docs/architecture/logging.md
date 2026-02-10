---
sidebar_position: 2
---

# Logging and Metrics

Glass Box includes a custom logging system and real-time metrics collection, both of which can stream data to connected SSE clients.

## ServerLogger

`ServerLogger` is a thread-safe singleton that runs on its own virtual thread. It processes log entries from a `BlockingQueue`, ensuring that logging never blocks the request-handling threads.

### Initialization

The logger is initialized once at startup in `Main.java`:

```java
ServerLogger.initialize(null, Loglevel.DEBUG);
```

After the SSE handler is available (post-config), the handler is attached:

```java
ServerLogger.getInstance().setSseHandler(sseHandler);
```

### Usage

Log a message from anywhere in the codebase:

```java
ServerLogger.getInstance().log(Loglevel.INFO, "Request processed", LogDestination.EVERYWHERE);
```

### Log Levels

Log levels control which messages are displayed. Messages below the configured threshold are suppressed on the server console (but may still be sent to SSE clients).

| Level | Severity | Use Case |
|-------|----------|----------|
| `DEBUG` | 0 | Detailed diagnostic information |
| `INFO` | 1 | General operational messages |
| `WARN` | 2 | Potential issues that don't prevent operation |
| `ERROR` | 3 | Errors that affect a single request |
| `FATAL` | 4 | Critical failures |

The log level is set in the YAML config:

```yaml
logLevel: DEBUG   # Show all messages
logLevel: INFO    # Hide DEBUG messages
logLevel: WARN    # Only warnings and above
```

### Log Destinations

Each log entry has a destination that controls where it is delivered:

| Destination | Behavior |
|---|---|
| `SERVER` | Printed to the server console only |
| `CLIENT` | Broadcast to SSE clients only |
| `EVERYWHERE` | Both console and SSE clients |

### Log Entry Format

Log entries broadcast via SSE are serialized as JSON:

```json
{
  "level": "INFO",
  "message": "GET /api/todos from /127.0.0.1:54321",
  "timestamp": 1700000000000
}
```

## Stats

The `Stats` singleton tracks server metrics using atomic counters for thread safety.

### Available Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `uptime` | `long` | Milliseconds since server start |
| `totalRequests` | `long` | Total HTTP requests processed |
| `activeConnections` | `long` | Current number of SSE clients |
| `cpuUsage` | `double` | System CPU load (0.0 to 1.0) |
| `memoryUsage` | `double` | Used memory in GB |

CPU and memory metrics are collected using the [OSHI](https://github.com/oshi/oshi) library for cross-platform system monitoring.

### Stats JSON Format

Stats are broadcast to SSE clients as the `stats` event:

```json
{
  "uptime": 120000,
  "totalRequests": 42,
  "activeConnections": 3,
  "cpuUsage": 0.15,
  "memoryUsage": 2.4
}
```

### How Stats Are Updated

- **Request count** — incremented by `HTTPHandler` after each successful request parse
- **Active connections** — updated by `SSEHandler` when clients connect or disconnect
- **CPU and memory** — polled on demand when stats are requested by an SSE broadcast cycle
