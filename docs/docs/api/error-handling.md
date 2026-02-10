---
sidebar_position: 3
---

# Error Handling

Glass Box maps exceptions to standard HTTP error responses. When any stage of request processing throws an exception, `HTTPErrorHandler` catches it, sends the appropriate error response, and logs the error.

## Error Responses

All error responses follow the same format:

```http
HTTP/1.1 {code} {message}
Content-Type: text/html; charset=utf-8
Connection: close

<h1>{code} {message}</h1>
```

## Status Codes

| Status Code | Message | Exception | Cause |
|---|---|---|---|
| 400 | Bad Request | `BadRequestException` | Malformed request line, missing method/path/version, invalid headers |
| 404 | Not Found | `NotFoundException` | No matching route, requested file does not exist, ToDo ID not found |
| 405 | Method Not Allowed | — | Sent directly by `HTTPErrorHandler` (not exception-mapped) |
| 500 | Internal Server Error | `InternalServerErrorException` | File read failures, unhandled exceptions |
| 501 | Not Implemented | `NotImplementedException` | Unsupported HTTP method, handler method not overridden |

:::note
Any exception that doesn't match a specific type (i.e., is not one of the four custom exceptions) is treated as a 500 Internal Server Error.
:::

## Exception Classes

All custom exceptions extend `Exception` and live in the `com.ns.tcpframework.exceptions` package:

```
com.ns.tcpframework.exceptions
├── BadRequestException
├── NotFoundException
├── InternalServerErrorException
└── NotImplementedException
```

### Usage in Handlers

Throw the appropriate exception from any handler and it will be automatically converted to an HTTP error response:

```java
@Override
protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception {
    Path filePath = Paths.get(rootPath, request.getPath());

    if (!Files.exists(filePath)) {
        throw new NotFoundException("File not found: " + filePath);
    }

    // ... serve file
}
```

### Usage in MethodeBasedHandler

If a handler extends `MethodeBasedHandler` but does not override a specific method (e.g., `handlePostRequest`), calling that method throws `NotImplementedException`, resulting in a 501 response.

## Error Logging

All errors handled by `HTTPErrorHandler` are logged via `ServerLogger` at the `ERROR` level with destination `EVERYWHERE`, meaning they appear on both the server console and connected SSE clients:

```
[ERROR] Exception handled: File not found: /nonexistent.html
```
