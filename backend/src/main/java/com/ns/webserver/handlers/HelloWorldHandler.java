package com.ns.webserver.handlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.reqeustHandlers.MethodeBasedHandler;

import java.nio.charset.StandardCharsets;

/**
 * A simple example handler that returns a "Hello, World!" HTML response.
 * <p>
 * This handler demonstrates the basic structure of a GET request handler in the framework.
 * It extends {@link MethodeBasedHandler} to provide HTTP method-specific handling and
 * implements only the GET method, making it suitable for simple read-only endpoints.
 * <p>
 * The handler always returns the same static HTML content regardless of the request
 * parameters, making it ideal for:
 * <ul>
 *   <li>Testing server functionality and route configuration</li>
 *   <li>Demonstrating basic handler implementation patterns</li>
 *   <li>Serving simple static content without file system access</li>
 *   <li>Health check or status endpoints</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * // Register in router configuration
 * router.register("/hello", new HelloWorldHandler());
 *
 * // Access via HTTP GET
 * GET /hello HTTP/1.1
 * Host: example.com
 *
 * // Response:
 * HTTP/1.1 200 OK
 * Content-Type: text/html; charset=UTF-8
 * Content-Length: 22
 *
 * &lt;h1&gt;Hello, World!&lt;/h1&gt;
 * </pre>
 * <p>
 * Supported HTTP methods:
 * <ul>
 *   <li><b>GET</b>: Returns the "Hello, World!" HTML content (implemented)</li>
 *   <li><b>HEAD</b>: Returns headers only without body (inherited from parent)</li>
 *   <li><b>POST, PUT, DELETE, etc.</b>: Returns 405 Method Not Allowed (inherited from parent)</li>
 * </ul>
 * <p>
 * Thread-safety: This handler is stateless and thread-safe. Multiple threads can
 * safely invoke {@link #handleGetRequest(HTTPRequest)} concurrently.
 *
 * @see MethodeBasedHandler
 * @see HTTPRequest
 * @see HTTPResponse
 */
public class HelloWorldHandler extends MethodeBasedHandler {

    /**
     * Handles HTTP GET requests by returning a simple "Hello, World!" HTML response.
     * <p>
     * This method creates a standard HTTP 200 OK response with HTML content displaying
     * a greeting message. The response includes proper Content-Type and Content-Length
     * headers automatically set by the {@link HTTPResponse#setBody(byte[], String)} method.
     * <p>
     * Response characteristics:
     * <ul>
     *   <li><b>Status</b>: 200 OK - Indicates successful request processing</li>
     *   <li><b>Content-Type</b>: text/html; charset=UTF-8 - HTML content with UTF-8 encoding</li>
     *   <li><b>Body</b>: "&lt;h1&gt;Hello, World!&lt;/h1&gt;" - Simple HTML heading</li>
     *   <li><b>Encoding</b>: UTF-8 - Ensures proper character representation</li>
     * </ul>
     * <p>
     * The method ignores all request parameters, headers, and body content, always
     * returning the same static response. This makes it deterministic and predictable
     * for testing purposes.
     *
     * @param request The HTTP GET request to handle. The request content is not used
     *                but must not be null.
     * @return An HTTPResponse with status 200 containing the "Hello, World!" HTML message.
     *         Never returns null.
     * @throws Exception This implementation does not throw exceptions under normal circumstances,
     *                   but the signature allows for future enhancements that might require
     *                   exception handling (e.g., template rendering, database access).
     */
    @Override
    protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setBody("<h1>Hello, World!</h1>".getBytes(StandardCharsets.UTF_8), "text/html; charset=UTF-8");
        return response;
    }

}
