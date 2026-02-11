package com.ns.tcpframework;

import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

/**
 * Configuration for a single virtual host in a name-based virtual hosting setup.
 * <p>
 * A virtual host represents a distinct website or domain served by the HTTP server.
 * This class encapsulates all configuration necessary to serve content for a specific
 * hostname, including the document root for static files and the routing configuration
 * for dynamic request handling.
 * <p>
 * Virtual hosting enables a single HTTP server to serve multiple domain names, each with
 * its own configuration. The server selects the appropriate virtual host based on the
 * HTTP Host header in incoming requests. This is commonly used to host multiple websites
 * on a single server instance.
 * <p>
 * Key components:
 * <ul>
 *   <li><b>Host</b>: The hostname this configuration applies to (e.g., "example.com", "www.example.com")</li>
 *   <li><b>Document Root</b>: The file system path where static content is located</li>
 *   <li><b>Router</b>: The routing configuration that maps URL paths to request handlers</li>
 * </ul>
 * <p>
 * The router is automatically initialized with the document root, enabling seamless
 * integration of static file serving with dynamic route handling. Routes can be registered
 * on the router after construction to add API endpoints and other dynamic functionality.
 * <p>
 * Usage example:
 * <pre>
 * // Create virtual host for example.com
 * VirtualHostConfig vhost = new VirtualHostConfig("example.com", "/var/www/example");
 *
 * // Register dynamic routes
 * vhost.getRouter().register("/api/users", new UserHandler());
 * vhost.getRouter().register("/api/products/*", new ProductHandler());
 *
 * // Register SSE endpoint
 * vhost.getRouter().register("/events", new SSEHandler());
 *
 * // Static files are automatically served from /var/www/example
 * </pre>
 * <p>
 * Thread-safety: This class is immutable after construction (all fields are final).
 * The router can be modified after construction by registering routes, so care should
 * be taken to complete route registration before concurrent request handling begins.
 *
 * @see RouterConfig
 * @see VirtualHostManager
 * @see ServerConfig
 * @see SSEHandler
 */
public class VirtualHostConfig {
    /**
     * The hostname this virtual host configuration applies to.
     * <p>
     * This is the value that will be matched against the HTTP Host header to select
     * this virtual host. It should be a fully qualified domain name (FQDN) or hostname.
     * <p>
     * Examples:
     * <ul>
     *   <li>"example.com" - Primary domain</li>
     *   <li>"www.example.com" - WWW subdomain</li>
     *   <li>"api.example.com" - API subdomain</li>
     *   <li>"localhost" - Local development</li>
     * </ul>
     * <p>
     * Note: This is the bare hostname without port numbers. Port numbers in the
     * Host header are stripped before matching.
     */
    private final String host;

    /**
     * The file system path to the document root for this virtual host.
     * <p>
     * This directory contains the static content (HTML, CSS, JavaScript, images, etc.)
     * to be served for this virtual host. When no dynamic route matches a request,
     * the router falls back to serving static files from this location.
     * <p>
     * The path can be:
     * <ul>
     *   <li>Absolute: "/var/www/example" - Full path from root</li>
     *   <li>Relative: "public/example" - Relative to application working directory</li>
     * </ul>
     * <p>
     * The directory should exist and be readable by the server process. Missing
     * or inaccessible document roots will result in 404 errors for static content.
     */
    private final String documentRoot;

    /**
     * The routing configuration for this virtual host.
     * <p>
     * This router manages all URL path mapping for the virtual host, including both
     * registered dynamic routes and the default static file handler. It is automatically
     * initialized with the document root to enable static file serving.
     * <p>
     * Routes can be registered on this router to handle dynamic requests:
     * <pre>
     * router.register("/api/users", userHandler);
     * router.register("/api/todos/*", todoHandler);
     * </pre>
     *
     * @see RouterConfig
     */
    private final RouterConfig router;

    /**
     * Constructs a VirtualHostConfig with the specified hostname and document root.
     * <p>
     * This constructor initializes the virtual host with its essential configuration
     * and creates a router configured to serve static files from the document root.
     * Additional dynamic routes can be registered on the router after construction.
     * <p>
     * The router is created with the document root as its static file handler base path,
     * enabling automatic serving of static content for requests that don't match any
     * registered dynamic route.
     * <p>
     * Configuration steps performed:
     * <ol>
     *   <li>Store the hostname for virtual host matching</li>
     *   <li>Store the document root path for static file serving</li>
     *   <li>Create a RouterConfig initialized with the document root</li>
     * </ol>
     *
     * @param host         The hostname this virtual host will serve (e.g., "example.com", "www.example.com").
     *                     Must not be null. Should not include port numbers.
     * @param documentRoot The file system path to the document root directory for static content.
     *                     Must not be null. Should be a valid, readable directory path.
     */
    public VirtualHostConfig(String host, String documentRoot) {
        this.host = host;
        this.documentRoot = documentRoot;
        this.router = new RouterConfig(documentRoot);
    }

    /**
     * Gets the hostname for this virtual host.
     * <p>
     * This is the hostname value used for virtual host matching against incoming
     * HTTP Host headers. It represents the domain or hostname this virtual host serves.
     *
     * @return The hostname (e.g., "example.com", "www.example.com"). Never null.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the document root path for this virtual host.
     * <p>
     * This is the file system path from which static content is served. The path
     * is used by the default static file handler to locate and serve files when
     * no dynamic route matches the request.
     *
     * @return The document root directory path. Never null.
     */
    public String getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Gets the router configuration for this virtual host.
     * <p>
     * The router manages all request routing for this virtual host, including both
     * dynamic routes that have been registered and the default static file handler.
     * Use this router to register additional routes after virtual host construction.
     * <p>
     * Common usage:
     * <pre>
     * RouterConfig router = vhost.getRouter();
     * router.register("/api/users", new UserHandler());
     * router.register("/api/products/*", new ProductHandler());
     * </pre>
     *
     * @return The RouterConfig instance for this virtual host. Never null.
     */
    public RouterConfig getRouter() {
        return router;
    }

    /**
     * Gets the first registered SSE handler from this virtual host's router.
     * <p>
     * This is a convenience method that delegates to {@link RouterConfig#getSSEHandler()}
     * to find an SSE handler registered on this virtual host's router. SSE handlers
     * provide Server-Sent Events functionality for real-time server-to-client communication.
     * <p>
     * This method is useful for:
     * <ul>
     *   <li>Initializing server-wide logging with SSE broadcast support</li>
     *   <li>Broadcasting events to all clients connected to this virtual host</li>
     *   <li>Checking if SSE functionality is available for this virtual host</li>
     * </ul>
     * <p>
     * If multiple SSE handlers are registered, only the first one found will be returned.
     * Typically, each virtual host should have at most one SSE handler.
     *
     * @return The first SSEHandler registered on this virtual host's router,
     *         or {@code null} if no SSE handler is registered.
     * @see SSEHandler
     * @see RouterConfig#getSSEHandler()
     */
    public SSEHandler getSSEHandler() {
        return router.getSSEHandler();
    }
}
