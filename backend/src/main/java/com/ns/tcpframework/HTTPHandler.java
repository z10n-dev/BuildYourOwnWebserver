package com.ns.tcpframework;

import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.logger.Stats;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Handles incoming HTTP connections and delegates request processing to appropriate handlers.
 * <p>
 * This class serves as the primary entry point for processing HTTP requests in the server.
 * It manages the request lifecycle from initial socket connection through response delivery,
 * including request parsing, routing, handler execution, and error handling.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Accepting client socket connections and submitting them to a thread pool</li>
 *   <li>Parsing raw HTTP requests from socket input streams</li>
 *   <li>Resolving virtual hosts based on the Host header</li>
 *   <li>Routing requests to appropriate {@link RequestHandler} implementations</li>
 *   <li>Executing handlers and managing HTTP responses</li>
 *   <li>Tracking server statistics (request counts, active connections)</li>
 *   <li>Logging request activity and errors</li>
 *   <li>Handling exceptions and generating error responses</li>
 * </ul>
 * <p>
 * Request processing flow:
 * <ol>
 *   <li>Client connects and socket is submitted to thread pool via {@link #handle(Socket, ExecutorService)}</li>
 *   <li>HTTP request is parsed from socket input stream</li>
 *   <li>Virtual host is resolved based on Host header</li>
 *   <li>Router finds appropriate handler for the request path</li>
 *   <li>Handler processes request and generates response</li>
 *   <li>Response is sent to client (if not already sent by handler)</li>
 *   <li>Socket is closed and resources are cleaned up</li>
 * </ol>
 * <p>
 * Thread-safety: This class is thread-safe and designed for concurrent request handling.
 * Each request is processed in its own thread from the provided {@link ExecutorService}.
 *
 * @see VirtualHostManager
 * @see HTTPRequest
 * @see HTTPResponse
 * @see RequestHandler
 */
public class HTTPHandler {
    /**
     * Manager responsible for resolving virtual hosts based on request Host headers.
     * <p>
     * The virtual host manager maintains a registry of configured virtual hosts
     * and selects the appropriate host configuration for each incoming request,
     * enabling the server to serve multiple websites from a single IP address.
     */
    private final VirtualHostManager vManager;

    /**
     * Constructs an HTTPHandler with the specified virtual host manager.
     * <p>
     * The virtual host manager is used to resolve which virtual host configuration
     * should handle each incoming request based on the HTTP Host header.
     *
     * @param vManager The VirtualHostManager managing virtual hosts and their configurations.
     *                 Must not be null.
     */
    public HTTPHandler(VirtualHostManager vManager) {
        this.vManager = vManager;
    }

    /**
     * Handles an incoming socket connection by submitting it to a thread pool for processing.
     * <p>
     * This method immediately returns after submitting the connection handling task to the
     * thread pool, allowing the server to accept new connections without blocking. The actual
     * request processing occurs asynchronously in a worker thread.
     * <p>
     * The method ensures proper resource cleanup by using try-with-resources to automatically
     * close the socket after processing completes, regardless of whether processing succeeds
     * or fails.
     * <p>
     * Error handling: Any exceptions thrown during request processing are caught, logged
     * via {@link ServerLogger}, and printed to standard error. The socket is closed
     * automatically even if an error occurs.
     *
     * @param socket The client socket representing the incoming connection. Must not be null
     *               and should be open and connected.
     * @param pool   The thread pool (ExecutorService) used to execute the handling task
     *               asynchronously. Must not be null and should be running.
     */
    public final void handle(Socket socket, ExecutorService pool) {
        pool.execute(() -> {
            try (socket) {
                var socketAddress = socket.getRemoteSocketAddress();
                runTask(socket);
            } catch (Exception e) {
                System.err.println("Handler Error: " + e.getMessage());
                ServerLogger.getInstance().log(Loglevel.ERROR, e.getMessage(), LogDestination.EVERYWHERE);
            }
        });
    }

    /**
     * Processes the HTTP request and delegates it to the appropriate request handler.
     * <p>
     * This method orchestrates the complete request processing pipeline:
     * <ol>
     *   <li>Parses the raw HTTP request from the socket input stream</li>
     *   <li>Increments the server statistics request counter</li>
     *   <li>Logs the request details (excluding certain paths like _next and HEAD requests)</li>
     *   <li>Resolves the virtual host configuration based on the Host header</li>
     *   <li>Uses the router to find the appropriate handler for the request path</li>
     *   <li>Invokes the handler to process the request and generate a response</li>
     *   <li>Sends the response to the client if not already sent by the handler</li>
     * </ol>
     * <p>
     * Selective logging: To reduce noise in logs, certain requests are excluded from
     * debug logging:
     * <ul>
     *   <li>Requests containing "_next" in the path (Next.js internal resources)</li>
     *   <li>HEAD method requests (metadata-only requests)</li>
     *   <li>Requests to "/api/active-clients" (high-frequency polling endpoint)</li>
     * </ul>
     * <p>
     * Error handling: Any exceptions thrown during request processing are caught and
     * passed to {@link HTTPErrorHandler#handleException(Socket, Exception)}, which
     * generates and sends an appropriate HTTP error response to the client.
     * <p>
     * Response handling: Some handlers (e.g., SSE handlers) may send responses directly
     * to the socket. The {@link HTTPResponse#isSended()} flag is checked to avoid
     * sending duplicate responses.
     *
     * @param socket The client socket representing the connection. Must not be null,
     *               should be open and connected.
     * @throws Exception If an unrecoverable error occurs during request processing.
     *                   Note that most exceptions are caught internally and converted
     *                   to HTTP error responses.
     */
    protected void runTask(Socket socket) throws Exception {
        try {
            HTTPRequest request = HTTPRequestParser.parseHTTPRequest(socket);
            Stats.getInstance().incrementRequests();
            if (!request.getPath().contains("_next") && !request.getMethod().equals(HTTPMethode.HEAD) && !request.getPath().startsWith("/api/active-clients")) {
                ServerLogger.getInstance().log(Loglevel.DEBUG, request.getRequestHead() + " from " + socket.getRemoteSocketAddress()  + request.getHost(), LogDestination.EVERYWHERE);
            }

            VirtualHostConfig vhost = vManager.getVirtualHost(request.getHost());

            RouterConfig router = vhost.getRouter();
            RequestHandler handler = router.findHandler(request);
            HTTPResponse response = handler.handle(request, socket);
            if(!response.isSended()){
                response.send(socket);
            }
        } catch (Exception e) {
            HTTPErrorHandler.handleException(socket, e);
        }
    }
}
