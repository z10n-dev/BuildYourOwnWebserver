package com.ns.tcpframework;

import com.ns.tcpframework.logger.Loglevel;

import java.util.Map;

/**
 * Configuration container for HTTP server settings and virtual host management.
 * <p>
 * This class encapsulates all configuration parameters needed to initialize and run
 * an HTTP server instance. It includes server-wide settings (port, log level, environment)
 * and virtual host configurations that enable hosting multiple websites on a single server.
 * <p>
 * Key configuration aspects:
 * <ul>
 *   <li><b>Environment</b>: Deployment environment identifier (development, staging, production)</li>
 *   <li><b>Port</b>: TCP port number on which the server listens for connections</li>
 *   <li><b>Log Level</b>: Minimum severity level for logging messages</li>
 *   <li><b>Virtual Hosts</b>: Name-based virtual hosting configuration for serving multiple domains</li>
 *   <li><b>Default Host</b>: Fallback virtual host for requests without a matching Host header</li>
 * </ul>
 * <p>
 * Virtual hosting allows a single server to serve multiple domain names, each with its own
 * configuration, document root, and route mappings. The server selects the appropriate
 * virtual host based on the HTTP Host header in incoming requests.
 * <p>
 * Configuration lifecycle:
 * <ol>
 *   <li>Configuration is loaded from YAML files by {@link ConfigLoader}</li>
 *   <li>ServerConfig instance is created with all parameters</li>
 *   <li>Server components are initialized using these settings</li>
 *   <li>Configuration remains immutable during server runtime (except via setters)</li>
 * </ol>
 * <p>
 * Usage example:
 * <pre>
 * ServerConfig config = ConfigLoader.load("production");
 * int port = config.getPort();                    // 8080
 * String env = config.getEnvironment();           // "production"
 * VirtualHostConfig host = config.getDefaultHost();
 * Map&lt;String, VirtualHostConfig&gt; hosts = config.getHosts();
 * </pre>
 * <p>
 * Thread-safety: This class is not inherently thread-safe. If configuration values need
 * to be modified after server startup, external synchronization should be used. However,
 * in typical usage, configuration is set once during initialization and read-only thereafter.
 *
 * @see ConfigLoader
 * @see VirtualHostConfig
 * @see Loglevel
 * @see VirtualHostManager
 */
public class ServerConfig {
    /**
     * The deployment environment name for this server configuration.
     * <p>
     * This identifier indicates which environment the server is running in, allowing
     * for environment-specific behavior and configuration selection. Common values:
     * <ul>
     *   <li>"development" - Local development with debug features enabled</li>
     *   <li>"staging" - Pre-production testing environment</li>
     *   <li>"production" - Live production environment</li>
     * </ul>
     */
    private String environment;

    /**
     * The TCP port number on which the server listens for HTTP connections.
     * <p>
     * Valid port numbers range from 1 to 65535. Common HTTP ports:
     * <ul>
     *   <li>80 - Standard HTTP (requires root/admin privileges on most systems)</li>
     *   <li>8080 - Common alternative HTTP port (no special privileges required)</li>
     *   <li>443 - Standard HTTPS (for secure connections)</li>
     *   <li>3000, 8000, 9000 - Common development ports</li>
     * </ul>
     */
    private int port;

    /**
     * The minimum log level for server logging.
     * <p>
     * Only log messages with a severity equal to or higher than this level will be
     * recorded. This controls the verbosity of server logs and can be adjusted per
     * environment (e.g., DEBUG for development, WARN for production).
     *
     * @see Loglevel
     */
    private Loglevel loglevel;

    /**
     * The default virtual host configuration used as a fallback.
     * <p>
     * This host is selected when:
     * <ul>
     *   <li>The HTTP request contains no Host header</li>
     *   <li>The Host header value doesn't match any configured virtual host</li>
     *   <li>The request uses HTTP/1.0 without a Host header</li>
     * </ul>
     * <p>
     * The default host ensures that all requests can be handled even if virtual host
     * resolution fails. It typically serves the primary website or a catch-all response.
     */
    private VirtualHostConfig defaultHost;

    /**
     * Map of hostname to virtual host configuration for name-based virtual hosting.
     * <p>
     * Keys are hostname strings (e.g., "example.com", "www.example.com", "api.example.com")
     * and values are the corresponding {@link VirtualHostConfig} objects containing
     * the document root, router configuration, and other host-specific settings.
     * <p>
     * The server uses this map to select the appropriate virtual host based on the
     * HTTP Host header in incoming requests, enabling multiple domains to be served
     * from a single server instance with independent configurations.
     */
    private Map<String, VirtualHostConfig> hosts;

    /**
     * Constructs a ServerConfig instance with all configuration parameters.
     * <p>
     * This constructor is typically invoked by {@link ConfigLoader} after parsing
     * configuration files. All parameters are required to ensure the server has
     * complete configuration information.
     * <p>
     * The constructor performs simple assignment without validation. Configuration
     * validation should be performed by the loading mechanism or during server
     * initialization.
     *
     * @param environment  The deployment environment name (e.g., "development", "production").
     *                     Must not be null.
     * @param port         The TCP port number for the server. Should be in the range 1-65535.
     * @param loglevel     The minimum logging level for the server. Must not be null.
     * @param defaultHost  The fallback virtual host configuration. Must not be null.
     * @param hosts        Map of hostname to virtual host configurations. Must not be null,
     *                     but may be empty (defaultHost will handle all requests).
     */
    public ServerConfig(String environment, int port, Loglevel loglevel, VirtualHostConfig defaultHost, Map<String, VirtualHostConfig> hosts) {
        this.environment = environment;
        this.port = port;
        this.loglevel = loglevel;
        this.defaultHost = defaultHost;
        this.hosts = hosts;
    }

    /**
     * Gets the deployment environment name.
     *
     * @return The environment name (e.g., "development", "staging", "production").
     *         Never null.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the deployment environment name.
     * <p>
     * Changing the environment at runtime is unusual and may require reloading
     * configuration or restarting server components to take full effect.
     *
     * @param environment The new environment name. Should not be null.
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Gets the TCP port number on which the server listens.
     *
     * @return The port number (1-65535).
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the TCP port number for the server.
     * <p>
     * Note: Changing the port at runtime requires restarting the server socket
     * to bind to the new port. This setter is primarily useful for testing or
     * pre-initialization configuration adjustments.
     *
     * @param port The new port number. Should be in the range 1-65535 and not
     *             already in use by another process.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the minimum log level for server logging.
     *
     * @return The configured log level. Never null.
     */
    public Loglevel getLoglevel() {
        return loglevel;
    }

    /**
     * Sets the minimum log level for server logging.
     * <p>
     * Changing the log level at runtime will immediately affect which messages
     * are logged by the server. This can be useful for temporary debugging or
     * reducing log verbosity in production.
     *
     * @param loglevel The new log level. Should not be null.
     */
    public void setLoglevel(Loglevel loglevel) {
        this.loglevel = loglevel;
    }

    /**
     * Gets the map of all configured virtual hosts.
     * <p>
     * The returned map contains hostname-to-configuration mappings for all virtual
     * hosts. Modifying this map will affect server routing behavior, though this
     * is typically not recommended during runtime.
     *
     * @return The map of hostname to virtual host configurations. Never null,
     *         but may be empty.
     */
    public Map<String, VirtualHostConfig> getHosts() {
        return hosts;
    }

    /**
     * Sets the map of virtual host configurations.
     * <p>
     * This replaces the entire virtual host configuration. Use with caution as
     * it will affect all virtual host resolution for incoming requests.
     *
     * @param hosts The new map of hostname to virtual host configurations.
     *              Should not be null.
     */
    public void setHosts(Map<String, VirtualHostConfig> hosts) {
        this.hosts = hosts;
    }

    /**
     * Gets the default virtual host configuration used as a fallback.
     * <p>
     * This host handles requests that don't match any configured virtual host
     * or lack a Host header. It ensures all requests can be processed even if
     * virtual host resolution fails.
     *
     * @return The default virtual host configuration. Never null.
     */
    public VirtualHostConfig getDefaultHost() {
        return defaultHost;
    }
}

