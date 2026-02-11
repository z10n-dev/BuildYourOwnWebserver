package com.ns.tcpframework;

/**
 * Functional interface representing a command to handle HTTP requests for a specific route.
 * <p>
 * This interface provides a functional abstraction for route handlers, allowing them to be
 * defined as lambda expressions, method references, or traditional implementations. It follows
 * the Command pattern, encapsulating the request processing logic for individual routes.
 * <p>
 * As a functional interface, RouteCommand can be implemented using various Java 8+ constructs:
 * <ul>
 *   <li>Lambda expressions: {@code (request) -> new HTTPResponse(200, "OK")}</li>
 *   <li>Method references: {@code MyClass::handleRequest}</li>
 *   <li>Anonymous classes: Traditional implementation approach</li>
 *   <li>Named classes: Full-featured implementations with additional state</li>
 * </ul>
 * <p>
 * The single abstract method {@link #run(HTTPRequest)} processes an incoming HTTP request
 * and produces an appropriate HTTP response. Implementations should:
 * <ul>
 *   <li>Parse and validate the request data</li>
 *   <li>Execute the business logic for the route</li>
 *   <li>Construct and return an appropriate HTTPResponse</li>
 *   <li>Throw exceptions for error conditions (handled by the framework)</li>
 * </ul>
 * <p>
 * Usage examples:
 * <pre>
 * // Lambda expression
 * RouteCommand helloRoute = (request) -> {
 *     HTTPResponse response = new HTTPResponse(200, "OK");
 *     response.setBody("Hello, World!".getBytes(), "text/plain");
 *     return response;
 * };
 *
 * // Method reference
 * RouteCommand userRoute = UserHandler::handleUserRequest;
 *
 * // Registration with router
 * router.register("/api/hello", helloRoute);
 * </pre>
 * <p>
 * Thread-safety: Implementations should be stateless or thread-safe, as the same
 * RouteCommand instance may be invoked concurrently by multiple request processing threads.
 *
 * @see HTTPRequest
 * @see HTTPResponse
 * @see com.ns.tcpframework.reqeustHandlers.RouteBasedHandler
 * @see RouterConfig
 */
@FunctionalInterface
public interface RouteCommand {

    /**
     * Executes the route command to process an HTTP request and generate a response.
     * <p>
     * This method is invoked by the routing framework when an incoming HTTP request matches
     * the route associated with this command. Implementations should process the request
     * according to their specific business logic and return an appropriate HTTP response.
     * <p>
     * The method should handle various aspects of request processing:
     * <ul>
     *   <li><b>Request validation</b>: Verify request method, headers, and body format</li>
     *   <li><b>Parameter extraction</b>: Parse path parameters, query strings, and body data</li>
     *   <li><b>Business logic</b>: Execute the core functionality for this route</li>
     *   <li><b>Response construction</b>: Create an HTTPResponse with appropriate status,
     *       headers, and body content</li>
     * </ul>
     * <p>
     * Error handling: Rather than catching exceptions internally, implementations should
     * typically allow exceptions to propagate to the framework, which will convert them
     * to appropriate HTTP error responses via {@link HTTPErrorHandler}. Common exceptions:
     * <ul>
     *   <li>{@code BadRequestException} - Invalid or malformed request data</li>
     *   <li>{@code NotFoundException} - Requested resource doesn't exist</li>
     *   <li>{@code InternalServerErrorException} - Unexpected server errors</li>
     * </ul>
     * <p>
     * Example implementation:
     * <pre>
     * public HTTPResponse run(HTTPRequest request) throws Exception {
     *     // Validate request method
     *     if (request.getMethod() != HTTPMethode.GET) {
     *         throw new MethodNotAllowedException("Only GET is supported");
     *     }
     *
     *     // Process request
     *     String data = fetchData();
     *
     *     // Build response
     *     HTTPResponse response = new HTTPResponse(200, "OK");
     *     response.setBody(data.getBytes(), "application/json");
     *     return response;
     * }
     * </pre>
     *
     * @param request The HTTP request to process. Contains method, path, headers, and body.
     *                Must not be null.
     * @return An HTTPResponse object containing the status code, headers, and body to send
     *         to the client. Must not be null.
     * @throws Exception If an error occurs during request processing. The framework will
     *                   catch these exceptions and convert them to appropriate HTTP error
     *                   responses. Specific exception types (BadRequestException, NotFoundException,
     *                   etc.) will be mapped to their corresponding HTTP status codes.
     */
    HTTPResponse run(HTTPRequest request) throws Exception;
}
