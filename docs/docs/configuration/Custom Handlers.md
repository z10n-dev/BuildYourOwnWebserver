---
sidebar_position: 2
---

# Custom Handlers

Glass Box is designed to be extended with custom handlers. Create a handler class, and it becomes available in the YAML config by name — no factory registration needed.

## Handler Base Classes

There are three base classes to choose from, each providing a different level of abstraction:

| Base Class | Best For | Pattern |
|---|---|---|
| `RequestHandler` | Full control over request and socket access | Direct override |
| `MethodeBasedHandler` | Handling specific HTTP methods (GET, POST, etc.) | Template Method |
| `RouteBasedHandler` | Mapping sub-routes within a handler | Command / Strategy |

## Option 1: MethodeBasedHandler

Best for handlers that serve a single path and differentiate by HTTP method. Override only the methods you need — unimplemented methods automatically return `501 Not Implemented`.

```java
package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.reqeustHandlers.MethodeBasedHandler;

import java.nio.charset.StandardCharsets;

public class StatusHandler extends MethodeBasedHandler {

    @Override
    protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody(
            "{\"status\": \"running\"}".getBytes(StandardCharsets.UTF_8),
            "application/json"
        );
        return response;
    }
}
```

**Available methods to override:**
- `handleGetRequest(HTTPRequest)` — `GET` requests
- `handlePostRequest(HTTPRequest)` — `POST` requests
- `handlePutRequest(HTTPRequest)` — `PUT` requests
- `handleDeleteRequest(HTTPRequest)` — `DELETE` requests
- `handleHeadRequest(HTTPRequest)` — `HEAD` requests

## Option 2: RouteBasedHandler

Best for handlers that manage multiple sub-routes under a wildcard path (like the built-in `ToDoHandler`). Define routes in the constructor using method references or lambdas.

```java
package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.reqeustHandlers.RouteBasedHandler;

import java.nio.charset.StandardCharsets;

public class UserHandler extends RouteBasedHandler {

    public UserHandler() {
        routes.put("GET /api/users", this::listUsers);
        routes.put("POST /api/users", this::createUser);
        routes.put("GET /api/users/:id", this::getUser);
        routes.put("DELETE /api/users/:id", this::deleteUser);
    }

    private HTTPResponse listUsers(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody("[]".getBytes(StandardCharsets.UTF_8), "application/json");
        return response;
    }

    private HTTPResponse createUser(HTTPRequest request) throws Exception {
        // request.getBody() contains the JSON payload
        HTTPResponse response = new HTTPResponse(201, "Created");
        response.setBody(request.getBody().getBytes(StandardCharsets.UTF_8), "application/json");
        return response;
    }

    private HTTPResponse getUser(HTTPRequest request) throws Exception {
        // Extract the ID from the request path
        String id = request.getRequestHead().substring(
            request.getRequestHead().lastIndexOf("/") + 1
        );
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody(
            ("{\"id\": \"" + id + "\"}").getBytes(StandardCharsets.UTF_8),
            "application/json"
        );
        return response;
    }

    private HTTPResponse deleteUser(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(204, "No Content");
        return response;
    }
}
```

**Route key format:** `METHOD /path` or `METHOD /path/:param`

The `:id` parameter routes match any path segment in that position. The actual value must be extracted from the request path manually (see `getUser` above).

## Option 3: RequestHandler

For full control, extend `RequestHandler` directly. This is useful when you need access to the raw `Socket` (like `SSEHandler` does for persistent connections).

```java
package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class EchoHandler extends RequestHandler {

    @Override
    public HTTPResponse handle(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody(
            request.getBody().getBytes(StandardCharsets.UTF_8),
            "text/plain"
        );
        return response;
    }

    // Override this method if you need socket access
    @Override
    public HTTPResponse handle(HTTPRequest request, Socket socket) throws Exception {
        return handle(request);
    }
}
```

## Registering Your Handler

Once you've created a handler class under the `com.ns` package, it is automatically discovered at startup by the `HandlerFactory`. Just add it to your YAML config:

```yaml
hosts:
  localhost:
    documentRoot: "frontend/out"
    routes:
      /api/status: StatusHandler
      /api/users/*: UserHandler
      /echo: EchoHandler
```

:::tip
Handler classes can be placed anywhere under the `com.ns` package. The `HandlerFactory` uses classpath scanning to find all `RequestHandler` subclasses, so no manual registration is needed.
:::

## HTTPRequest API

Key methods available on the `HTTPRequest` object:

| Method | Returns | Description |
|---|---|---|
| `getMethod()` | `HTTPMethode` | The HTTP method (GET, POST, PUT, DELETE, HEAD, OPTIONS) |
| `getPath()` | `String` | The request path without query string |
| `getBody()` | `String` | The request body as a string |
| `getHost()` | `String` | The `Host` header value (without port) |
| `getRequestHead()` | `String` | Combined `METHOD /path` string (e.g., `GET /api/users`) |
| `toString()` | `String` | Full request representation including headers |

## HTTPResponse API

Key methods for building responses:

| Method | Description |
|---|---|
| `new HTTPResponse(statusCode, statusMessage)` | Create a response with a status code and message |
| `setHeader(key, value)` | Add a response header (supports multiple values per key) |
| `setBody(byte[], contentType)` | Set the body and `Content-Type`/`Content-Length` headers |
| `send(socket)` | Manually send the response (usually done automatically) |
| `isSended()` | Check if the response has already been sent |
