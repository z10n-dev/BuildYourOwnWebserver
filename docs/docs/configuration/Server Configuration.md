---
sidebar_position: 1
---

# Server Configuration

Glass Box is configured using YAML files with the naming convention `config.{environment}.yaml`. The environment is specified as a command-line argument when starting the server.

## Config File Location

The `ConfigLoader` searches for the config file in two locations, in order:

1. **External file** — `config/config.{env}.yaml` relative to the working directory
2. **Classpath** — `config/config.{env}.yaml` inside the JAR (in `src/main/resources/`)

If the external file exists, it takes priority. This allows overriding the bundled config without rebuilding the JAR.

## Example Configuration

```yaml
environment: dev
port: 8080
logLevel: DEBUG
defaultHost: localhost

hosts:
  localhost:
    documentRoot: "frontend/out"
    routes:
      /hello: HelloWorldHandler
      /api/todos/*: ToDoHandler
      /api/sse: SSEHandler

  a.localhost:
    documentRoot: "static/example"
    routes:
      /: HelloWorldHandler

  docs.localhost:
    documentRoot: "docs/build"
    routes:
      /hello: HelloWorldHandler
```

## Configuration Parameters

### Top-Level

| Parameter | Type | Required | Description |
|---|---|---|---|
| `environment` | `string` | Yes | Label identifying the config profile (e.g., `dev`, `prod`). Used for logging, not for file resolution. |
| `port` | `int` | Yes | TCP port the server listens on. |
| `logLevel` | `string` | Yes | Minimum log level for console output. One of: `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`. |
| `defaultHost` | `string` | Yes | Hostname used as fallback when the request's `Host` header doesn't match any configured host. Must match a key in `hosts`. |
| `hosts` | `map` | Yes | Map of hostname to virtual host configuration. See below. |

### Virtual Host

Each entry under `hosts` configures a virtual host:

| Parameter | Type | Required | Description |
|---|---|---|---|
| `documentRoot` | `string` | Yes | Directory for static file serving. Paths are relative to the working directory. The `StaticFileHandler` resolves files relative to this path. |
| `routes` | `map` | No | Maps URL path patterns to handler class names. If omitted, only static files are served. |

### Route Patterns

Routes map URL patterns to handler class names. The handler is resolved at startup using reflection (see [Handler Discovery](#handler-discovery)).

| Pattern | Example | Matches |
|---|---|---|
| Exact path | `/hello` | Only `/hello` |
| Wildcard suffix | `/api/todos/*` | `/api/todos`, `/api/todos/123`, `/api/todos/123/sub` |

Routes are evaluated in this order:
1. Exact match against the full path
2. Wildcard pattern match (iterates over all registered patterns)
3. Default `StaticFileHandler` if no route matches

## Handler Discovery

Handler class names in the `routes` config are resolved by the `HandlerFactory`, which uses the [Reflections](https://github.com/ronmamo/reflections) library to scan the `com.ns` package at startup. Both simple class names and fully qualified names work:

```yaml
routes:
  /hello: HelloWorldHandler                                          # Simple name
  /api/sse: com.ns.tcpframework.reqeustHandlers.sse.SSEHandler      # Fully qualified
```

## Built-in Handlers

| Handler | Description |
|---|---|
| `StaticFileHandler` | Serves files from the virtual host's `documentRoot`. Automatically serves `index.html` for directory paths. Detects MIME types via `Files.probeContentType()`. Used as the default handler when no route matches. |
| `HelloWorldHandler` | Returns `<h1>Hello, World!</h1>` with `text/html` content type. Useful for testing connectivity. |
| `ToDoHandler` | RESTful CRUD API for to-do items at `/api/todos/*`. See [ToDo API](../api/ToDoHandler.md). |
| `SSEHandler` | Server-Sent Events endpoint that streams logs and metrics. See [SSE Handler](../api/SSE%20Handler.md). |

## Environment Profiles

### Development (`config.dev.yaml`)

- `logLevel: DEBUG` — verbose logging for development
- `defaultHost: localhost`
- Multiple virtual hosts for testing (e.g., `a.localhost`, `docs.localhost`)

### Production (`config.prod.yaml`)

- `logLevel: INFO` — reduced log noise
- `defaultHost` set to the production domain
- Document roots point to absolute paths inside the Docker container

```yaml
environment: prod
port: 8080
logLevel: INFO
defaultHost: ipro.programmierwelt.ch

hosts:
  ipro.programmierwelt.ch:
    documentRoot: "/app/www/glassbox"
    routes:
      /hello: HelloWorldHandler
      /api/todos/*: ToDoHandler
      /api/sse: SSEHandler
```

:::info
To use custom subdomains like `a.localhost` in development, you may need to add entries to your `/etc/hosts` file:
```bash
127.0.0.1   a.localhost
127.0.0.1   docs.localhost
::1         a.localhost
::1         docs.localhost
```
:::
