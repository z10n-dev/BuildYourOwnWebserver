package com.ns.tcpframework;

import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages virtual host configurations and resolves hostnames to their corresponding virtual hosts.
 * <p>
 * The VirtualHostManager acts as a registry and resolver for name-based virtual hosting,
 * maintaining a collection of virtual host configurations and selecting the appropriate
 * configuration based on the HTTP Host header in incoming requests. This enables a single
 * HTTP server instance to serve multiple domains or websites with distinct configurations.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Virtual host registration and storage</li>
 *   <li>Hostname-to-configuration resolution with case-insensitive matching</li>
 *   <li>Default virtual host fallback for unmatched hostnames</li>
 *   <li>SSE handler discovery across all virtual hosts</li>
 * </ul>
 * <p>
 * Hostname matching behavior:
 * <ul>
 *   <li>Hostnames are normalized to lowercase for case-insensitive matching</li>
 *   <li>Exact hostname match takes precedence</li>
 *   <li>Default virtual host is returned when no match is found</li>
 *   <li>Port numbers should be stripped from hostnames before lookup</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * // Create default virtual host
 * VirtualHostConfig defaultHost = new VirtualHostConfig("default", "/var/www/default");
 *
 * // Create manager
 * VirtualHostManager manager = new VirtualHostManager(defaultHost);
 *
 * // Register virtual hosts
 * VirtualHostConfig exampleHost = new VirtualHostConfig("example.com", "/var/www/example");
 * manager.registerVirtualHost(exampleHost);
 *
 * VirtualHostConfig apiHost = new VirtualHostConfig("api.example.com", "/var/www/api");
 * manager.registerVirtualHost(apiHost);
 *
 * // Resolve during request handling
 * String requestHost = request.getHost(); // "example.com"
 * VirtualHostConfig host = manager.getVirtualHost(requestHost);
 * </pre>
 * <p>
 * Thread-safety: This class is not inherently thread-safe. Virtual hosts should be
 * registered during initialization before concurrent request handling begins. The
 * {@link #getVirtualHost(String)} method can be safely called concurrently once
 * registration is complete.
 *
 * @see VirtualHostConfig
 * @see HTTPHandler
 * @see ServerConfig
 * @see SSEHandler
 */
public class VirtualHostManager {
    /**
     * Map of lowercase hostnames to their corresponding virtual host configurations.
     * <p>
     * Hostnames are stored in lowercase to enable case-insensitive matching.
     * This ensures that "Example.com", "EXAMPLE.COM", and "example.com" all
     * resolve to the same virtual host configuration.
     * <p>
     * Keys are hostname strings (e.g., "example.com", "www.example.com", "api.example.com")
     * and values are the corresponding {@link VirtualHostConfig} objects.
     */
    private final HashMap<String, VirtualHostConfig> virtualHosts;

    /**
     * The default virtual host configuration used as a fallback.
     * <p>
     * This virtual host is returned when:
     * <ul>
     *   <li>The requested hostname is not found in the registry</li>
     *   <li>The HTTP request lacks a Host header</li>
     *   <li>The Host header is malformed or empty</li>
     * </ul>
     * <p>
     * The default host ensures all requests can be handled even if virtual host
     * resolution fails, typically serving a primary website or catch-all response.
     */
    private final VirtualHostConfig defaultVirtualHost;

    /**
     * Constructs a VirtualHostManager with the specified default virtual host.
     * <p>
     * The default virtual host is used as a fallback when hostname resolution fails,
     * ensuring that all requests can be processed even if no matching virtual host
     * is found. This is essential for handling:
     * <ul>
     *   <li>Requests with missing or malformed Host headers</li>
     *   <li>Requests to unregistered hostnames</li>
     *   <li>HTTP/1.0 requests that may not include a Host header</li>
     * </ul>
     * <p>
     * After construction, virtual hosts can be registered using
     * {@link #registerVirtualHost(VirtualHostConfig)}.
     *
     * @param defaultVirtualHost The virtual host configuration to use as a fallback
     *                          when no matching host is found. Must not be null.
     */
    public VirtualHostManager(VirtualHostConfig defaultVirtualHost) {
        this.virtualHosts = new HashMap<>();
        this.defaultVirtualHost = defaultVirtualHost;
    }

    /**
     * Registers a virtual host configuration with this manager.
     * <p>
     * The virtual host's hostname is extracted and normalized to lowercase before
     * being stored in the registry. This enables case-insensitive hostname matching
     * during request handling.
     * <p>
     * If a virtual host with the same hostname (case-insensitive) already exists,
     * it will be replaced with the new configuration. This allows for dynamic
     * reconfiguration if needed, though this is uncommon in production environments.
     * <p>
     * Virtual hosts should typically be registered during server initialization
     * before request handling begins to avoid race conditions.
     *
     * @param vhost The virtual host configuration to register. Must not be null.
     *              The hostname from {@code vhost.getHost()} is used as the key.
     */
    public void registerVirtualHost(VirtualHostConfig vhost) {
        virtualHosts.put(vhost.getHost().toLowerCase(), vhost);
    }

    /**
     * Resolves a hostname to its corresponding virtual host configuration.
     * <p>
     * This method performs case-insensitive hostname matching by normalizing the
     * provided hostname to lowercase before lookup. If no matching virtual host
     * is found, the default virtual host is returned as a fallback.
     * <p>
     * Hostname resolution process:
     * <ol>
     *   <li>Convert the requested hostname to lowercase</li>
     *   <li>Look up the hostname in the virtual hosts registry</li>
     *   <li>Return the matched configuration, or the default if no match found</li>
     * </ol>
     * <p>
     * Expected hostname format: The hostname should be the bare domain name without
     * port numbers (e.g., "example.com" not "example.com:8080"). Port stripping
     * should be performed by the caller before invoking this method.
     * <p>
     * This method is designed to be called during request handling for each incoming
     * HTTP request to determine which virtual host should process the request.
     *
     * @param host The hostname to resolve (e.g., "example.com", "www.example.com").
     *             Must not be null. Should not include port numbers. Case-insensitive.
     * @return The matching VirtualHostConfig if found, or the default virtual host
     *         if no match exists. Never returns null.
     */
    public VirtualHostConfig getVirtualHost(String host) {
        return virtualHosts.getOrDefault(host.toLowerCase(), defaultVirtualHost);
    }

    /**
     * Gets the map of all registered virtual host configurations.
     * <p>
     * The returned map contains lowercase hostname-to-configuration mappings for
     * all registered virtual hosts (excluding the default host). Modifying this
     * map will affect hostname resolution, though direct modification is not
     * recommended - use {@link #registerVirtualHost(VirtualHostConfig)} instead.
     * <p>
     * This method is primarily useful for:
     * <ul>
     *   <li>Iterating over all configured virtual hosts</li>
     *   <li>Discovering SSE handlers across virtual hosts</li>
     *   <li>Administrative or monitoring operations</li>
     * </ul>
     *
     * @return A map of lowercase hostnames to VirtualHostConfig objects.
     *         Never null, but may be empty if no virtual hosts have been registered.
     */
    public Map<String, VirtualHostConfig> getVirtualHosts() {
        return virtualHosts;
    }

    /**
     * Finds and returns the first SSE handler from any registered virtual host.
     * <p>
     * This method searches through all registered virtual hosts (not including the
     * default host) to find a virtual host that has an {@link SSEHandler} registered
     * on its router. The first SSE handler found is returned.
     * <p>
     * This method is useful for:
     * <ul>
     *   <li>Initializing the {@link com.ns.tcpframework.logger.ServerLogger} with
     *       SSE broadcast capability</li>
     *   <li>Broadcasting server-wide events to connected clients</li>
     *   <li>Checking if SSE functionality is available in the server configuration</li>
     * </ul>
     * <p>
     * Discovery behavior:
     * <ul>
     *   <li>Iterates through registered virtual hosts in no particular order</li>
     *   <li>Returns the first non-null SSE handler found</li>
     *   <li>Returns null if no virtual host has an SSE handler registered</li>
     *   <li>Does not check the default virtual host</li>
     * </ul>
     * <p>
     * Note: If multiple SSE handlers are registered across different virtual hosts,
     * only one will be returned. For server-wide broadcasting, typically only one
     * SSE endpoint should be configured.
     *
     * @return The first SSEHandler found in any registered virtual host,
     *         or {@code null} if no SSE handler is registered on any virtual host.
     * @see SSEHandler
     * @see VirtualHostConfig#getSSEHandler()
     */
    public SSEHandler getSSEHandler() {
        for (Map.Entry<String, VirtualHostConfig> entry : virtualHosts.entrySet()) {
            if (entry.getValue().getSSEHandler() != null) {
                return entry.getValue().getSSEHandler();
            }
        }

        return null;
    }
}

