package com.ns.webserver;

import com.ns.tcpframework.*;
import com.ns.webserver.handlers.*;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ns.tcpframework.logger.Loglevel.*;

/**
 * Main application entry point for the HTTP web server.
 * <p>
 * This class bootstraps and initializes the complete HTTP server infrastructure, including:
 * <ul>
 *   <li>Configuration loading from YAML files based on the environment</li>
 *   <li>Logging system initialization with SSE broadcasting support</li>
 *   <li>Virtual host management for multi-domain hosting</li>
 *   <li>Thread pool configuration using virtual threads for high concurrency</li>
 *   <li>TCP server setup and startup</li>
 * </ul>
 * <p>
 * The server supports multiple deployment environments (development, staging, production)
 * with environment-specific configurations loaded from YAML files in the config directory.
 * The environment is specified as a command-line argument, defaulting to "prod" if not provided.
 * <p>
 * Initialization sequence:
 * <ol>
 *   <li>Initialize ServerLogger with DEBUG level (will be overridden by config)</li>
 *   <li>Load environment-specific configuration from config/config.{environment}.yaml</li>
 *   <li>Update logger configuration with the loaded log level</li>
 *   <li>Initialize VirtualHostManager with the default host from configuration</li>
 *   <li>Register all configured virtual hosts for name-based virtual hosting</li>
 *   <li>Create HTTPHandler with the virtual host manager</li>
 *   <li>Initialize virtual thread executor for concurrent request handling</li>
 *   <li>Configure ServerLogger with SSE handler for real-time log broadcasting</li>
 *   <li>Create and start TCPServer on the configured port</li>
 * </ol>
 * <p>
 * Virtual Threads: The server uses Java's virtual threads (Project Loom) via
 * {@link Executors#newVirtualThreadPerTaskExecutor()}, enabling high-concurrency
 * request handling with minimal resource overhead compared to platform threads.
 * <p>
 * Server-Sent Events: If an SSE handler is configured in any virtual host, the
 * server logger will broadcast log messages to all connected SSE clients in real-time,
 * enabling live monitoring and debugging capabilities.
 * <p>
 * Example usage:
 * <pre>
 * // Start server in development mode
 * java -jar webserver.jar dev
 *
 * // Start server in production mode (default)
 * java -jar webserver.jar
 * // or
 * java -jar webserver.jar prod
 * </pre>
 * <p>
 * Error handling: If the server fails to start (e.g., port already in use, configuration
 * error), an error is logged via ServerLogger and the application continues to run without
 * the TCP server. Consider adding System.exit(1) for production deployments to ensure
 * failed startups are properly reported.
 *
 * @see TCPServer
 * @see ServerConfig
 * @see ConfigLoader
 * @see VirtualHostManager
 * @see HTTPHandler
 * @see ServerLogger
 * @see SSEHandler
 */
public class Main {
    /**
     * Main entry point for the HTTP web server application.
     * <p>
     * This method orchestrates the complete server initialization and startup process.
     * It loads configuration, sets up virtual hosting, initializes the thread pool,
     * configures logging with SSE support, and starts the TCP server.
     * <p>
     * Command-line arguments:
     * <ul>
     *   <li><b>args[0]</b> (optional): Environment name (e.g., "dev", "staging", "prod").
     *       Defaults to "prod" if not specified. This determines which configuration
     *       file is loaded from config/config.{environment}.yaml</li>
     * </ul>
     * <p>
     * Configuration files: The server expects YAML configuration files in the following locations:
     * <ul>
     *   <li>External: config/config.{environment}.yaml (checked first)</li>
     *   <li>Classpath: config/config.{environment}.yaml (fallback)</li>
     * </ul>
     * <p>
     * Initialization steps performed:
     * <ol>
     *   <li>Initialize ServerLogger with temporary DEBUG level</li>
     *   <li>Load ServerConfig from environment-specific YAML file</li>
     *   <li>Apply configured log level from ServerConfig</li>
     *   <li>Create VirtualHostManager with default host from config</li>
     *   <li>Register all virtual hosts from configuration</li>
     *   <li>Initialize HTTPHandler for request processing</li>
     *   <li>Create virtual thread executor for request concurrency</li>
     *   <li>Configure SSE handler for real-time log broadcasting</li>
     *   <li>Create and start TCPServer on configured port</li>
     * </ol>
     * <p>
     * Thread model: The server uses virtual threads for request handling, allowing
     * thousands of concurrent connections without the overhead of platform threads.
     * Each request is processed in its own virtual thread, providing natural blocking
     * I/O patterns without performance penalties.
     * <p>
     * Error handling: Exceptions during server startup are caught and logged via
     * ServerLogger. The application continues running even if the TCP server fails
     * to start, which may not be desirable in production environments.
     * <p>
     * Example invocations:
     * <pre>
     * // Development environment with debug logging
     * java com.ns.webserver.Main dev
     *
     * // Production environment (default)
     * java com.ns.webserver.Main
     * java com.ns.webserver.Main prod
     *
     * // Staging environment
     * java com.ns.webserver.Main staging
     * </pre>
     *
     * @param args Command-line arguments. The first argument (if present) specifies
     *             the environment name for configuration loading. Defaults to "prod"
     *             if no arguments are provided.
     * @throws IOException If an I/O error occurs during configuration loading. This is
     *                     currently not handled and will terminate the application.
     * @see ConfigLoader#load(String)
     * @see ServerConfig
     * @see VirtualHostManager
     * @see TCPServer
     */
    public static void main(String[] args) throws IOException {
        ServerLogger.initialize(null, DEBUG);
        ServerConfig config = ConfigLoader.load(args.length > 0 ? args[0] : "prod");

        ServerLogger.getInstance().setLogLevel(config.getLoglevel());

        VirtualHostManager vhost = new VirtualHostManager(config.getDefaultHost());

        config.getHosts().forEach((hostname, vHostConfig) -> {
            vhost.registerVirtualHost(vHostConfig);
        });

        HTTPHandler serverHandler = new HTTPHandler(vhost);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        SSEHandler sseHandler = vhost.getSSEHandler();
        ServerLogger.getInstance().setSseHandler(sseHandler);


        try {
            TCPServer server = new TCPServer(config.getPort(), serverHandler, executor);
            server.start();
        } catch (Exception e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Server failed to start: " + e.getMessage(), LogDestination.SERVER);
        }



    }
}

