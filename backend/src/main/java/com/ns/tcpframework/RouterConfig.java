package com.ns.tcpframework;

import com.ns.tcpframework.reqeustHandlers.MethodeBasedHandler;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;
import com.ns.tcpframework.reqeustHandlers.StaticFileHandler;
import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the routing configuration for HTTP request dispatching.
 * <p>
 * The RouterConfig class serves as the central routing mechanism for the HTTP server,
 * mapping URL path patterns to their corresponding {@link RequestHandler} implementations.
 * It provides intelligent pattern matching, including support for wildcard patterns and
 * parameterized routes, with fallback to a default static file handler when no route matches.
 * <p>
 * Key features:
 * <ul>
 *   <li>Dynamic route registration for custom handlers</li>
 *   <li>Pattern matching with wildcard support (e.g., /api/*, /users/:id)</li>
 *   <li>Exact path matching with O(1) lookup performance</li>
 *   <li>Fallback to default handler for unmatched routes (typically static file serving)</li>
 *   <li>SSE handler discovery for Server-Sent Events support</li>
 * </ul>
 * <p>
 * Route matching strategy (in order of precedence):
 * <ol>
 *   <li>Exact path match - Direct O(1) map lookup</li>
 *   <li>Pattern match - Iterates through registered patterns</li>
 *   <li>Default handler - Static file serving from configured document root</li>
 * </ol>
 * <p>
 * Supported pattern formats:
 * <ul>
 *   <li><b>Exact paths</b>: {@code /api/users} - Matches only this exact path</li>
 *   <li><b>Wildcard suffix</b>: {@code /api/todos/*} - Matches /api/todos and all subpaths</li>
 *   <li><b>Regex patterns</b>: {@code /users/[0-9]+} - Custom regex patterns</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * RouterConfig router = new RouterConfig("/var/www/html");
 * router.register("/api/users", new UserHandler());
 * router.register("/api/todos/*", new TodoHandler());
 * router.register("/events", new SSEHandler());
 *
 * // During request handling
 * RequestHandler handler = router.findHandler(request);
 * HTTPResponse response = handler.handle(request);
 * </pre>
 * <p>
 * Thread-safety: This class is not thread-safe. Routes should be registered during
 * initialization before concurrent request handling begins. The findHandler method
 * can be called concurrently once initialization is complete.
 *
 * @see RequestHandler
 * @see HTTPRequest
 * @see StaticFileHandler
 * @see SSEHandler
 */
public class RouterConfig {
    /**
     * Map of URL path patterns to their corresponding request handlers.
     * <p>
     * Keys are path patterns (e.g., "/api/users", "/api/todos/*") and values are
     * handler instances. Exact path matches are checked first for O(1) performance
     * before falling back to pattern matching.
     */
    private final Map<String, RequestHandler> routes = new HashMap<>();

    /**
     * The default handler used when no route matches the request path.
     * <p>
     * This is typically a {@link StaticFileHandler} that serves files from the
     * configured document root. It acts as a fallback for serving static content
     * like HTML, CSS, JavaScript, and image files.
     */
    private final MethodeBasedHandler defaultHandler;

    /**
     * Constructs a RouterConfig instance with a default static file handler.
     * <p>
     * The static file handler is configured with the specified root path and will
     * serve files from this location when no matching route is found. This allows
     * the router to handle both dynamic routes (registered handlers) and static
     * content (files from the file system).
     * <p>
     * The static path should point to a directory containing the static assets
     * to be served (HTML, CSS, JavaScript, images, etc.). This is typically set
     * to the document root of the virtual host.
     *
     * @param staticPath The root directory path for serving static files.
     *                   Should be an absolute or relative path to a valid directory.
     *                   Must not be null.
     */
    public RouterConfig(String staticPath){
        this.defaultHandler = new StaticFileHandler(staticPath);
    }

    /**
     * Registers a new route with its corresponding request handler.
     * <p>
     * This method maps a URL path pattern to a handler implementation. When an incoming
     * request matches the pattern, the associated handler will be invoked to process
     * the request. Multiple handlers can be registered for different patterns.
     * <p>
     * Path pattern formats:
     * <ul>
     *   <li><b>Exact paths</b>: "/api/users" - Matches only this exact path</li>
     *   <li><b>Wildcard suffix</b>: "/api/todos/*" - Matches the prefix and all subpaths</li>
     *   <li><b>Regex patterns</b>: Any pattern containing wildcards will be treated as regex</li>
     * </ul>
     * <p>
     * If a pattern is registered multiple times, the last registration will overwrite
     * previous ones. Routes should typically be registered during application startup
     * before request handling begins.
     * <p>
     * Examples:
     * <pre>
     * router.register("/api/users", userHandler);           // Exact match
     * router.register("/api/todos/*", todoHandler);         // Wildcard suffix
     * router.register("/files/[a-z]+\\.txt", fileHandler);  // Regex pattern
     * </pre>
     *
     * @param pathPattern The URL path pattern for the route. Can be an exact path,
     *                    wildcard pattern, or regex pattern. Must not be null.
     * @param handler     The request handler that will process requests matching this pattern.
     *                    Must not be null.
     */
    public void register(String pathPattern, RequestHandler handler){
        routes.put(pathPattern, handler);
    }


    /**
     * Finds the appropriate request handler for the given HTTP request.
     * <p>
     * This method implements a three-tier matching strategy to find the best handler
     * for the request:
     * <ol>
     *   <li><b>Exact match</b>: Checks if the request path exactly matches a registered route (O(1))</li>
     *   <li><b>Pattern match</b>: Iterates through patterns to find a matching wildcard or regex route</li>
     *   <li><b>Default handler</b>: Returns the static file handler if no route matches</li>
     * </ol>
     * <p>
     * The matching algorithm prioritizes exact matches for performance, then falls back
     * to pattern matching which may involve regex evaluation. This ensures common paths
     * (like API endpoints) are resolved quickly while still supporting flexible routing.
     * <p>
     * Pattern matching supports:
     * <ul>
     *   <li>Wildcard suffix patterns: "/api/todos/*" matches "/api/todos" and "/api/todos/123"</li>
     *   <li>Regex patterns: Any pattern with wildcards is converted to regex</li>
     * </ul>
     * <p>
     * If no route matches, the default handler (typically {@link StaticFileHandler}) is
     * returned, which will attempt to serve the requested path as a static file from the
     * document root. This provides seamless integration of dynamic and static content.
     *
     * @param request The HTTP request for which to find a handler. Contains the request
     *                path used for matching. Must not be null.
     * @return The matching request handler if a route matches, or the default handler
     *         (static file handler) if no route matches. Never returns null.
     */
    public RequestHandler findHandler(HTTPRequest request) {
        String path = request.getPath();

        if (routes.containsKey(path)) {
            return routes.get(path);
        }

        for (String pattern : routes.keySet()) {
            if (matchPattern(path, pattern)) {
                return routes.get(pattern);
            }
        }

        return defaultHandler;
    }

    /**
     * Checks if a given path matches a pattern using wildcard and regex matching.
     * <p>
     * This method implements pattern matching logic to determine if a request path
     * matches a registered route pattern. It supports two types of patterns:
     * <p>
     * Wildcard suffix patterns (ending with "/*"):
     * <ul>
     *   <li>Pattern "/api/todos/*" matches "/api/todos" (exact prefix)</li>
     *   <li>Pattern "/api/todos/*" matches "/api/todos/123" (prefix with subpath)</li>
     *   <li>The wildcard represents zero or more path segments</li>
     * </ul>
     * <p>
     * General wildcard patterns:
     * <ul>
     *   <li>Wildcards (*) are converted to regex (.*) for matching</li>
     *   <li>Pattern "/files/*.txt" becomes regex "/files/.*\\.txt"</li>
     *   <li>The entire path must match the regex pattern</li>
     * </ul>
     * <p>
     * Examples:
     * <pre>
     * matchPattern("/api/todos", "/api/todos/*")      → true
     * matchPattern("/api/todos/123", "/api/todos/*")  → true
     * matchPattern("/api/users", "/api/todos/*")      → false
     * matchPattern("/file.txt", "/files/*.txt")       → false (wrong directory)
     * matchPattern("/files/doc.txt", "/files/*.txt")  → true
     * </pre>
     *
     * @param path    The request path to check. Should not include query parameters.
     *                Must not be null.
     * @param pattern The pattern to match against. Can contain wildcards or be an exact path.
     *                Must not be null.
     * @return {@code true} if the path matches the pattern, {@code false} otherwise.
     */
    private boolean matchPattern(String path, String pattern) {
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }

        return path.matches(pattern.replace("*", ".*"));
    }

    /**
     * Retrieves the first registered SSE handler from the routes.
     * <p>
     * This method searches through all registered handlers to find an instance of
     * {@link SSEHandler}. SSE handlers provide Server-Sent Events functionality for
     * real-time, unidirectional server-to-client communication.
     * <p>
     * This method is useful for:
     * <ul>
     *   <li>Initializing the {@link com.ns.tcpframework.logger.ServerLogger} with SSE support</li>
     *   <li>Broadcasting server events to all connected SSE clients</li>
     *   <li>Checking if SSE functionality is available in the current configuration</li>
     * </ul>
     * <p>
     * Note: If multiple SSE handlers are registered, only the first one encountered
     * during iteration will be returned. The order is not guaranteed due to HashMap
     * iteration behavior. Typically, only one SSE handler should be registered per
     * router configuration.
     *
     * @return The first {@link SSEHandler} found in the registered routes, or {@code null}
     *         if no SSE handler is registered.
     */
    public SSEHandler getSSEHandler() {
        for (RequestHandler handler : routes.values()) {
            if (handler instanceof SSEHandler) {
                return (SSEHandler) handler;
            }
        }
        return null;
    }
}
