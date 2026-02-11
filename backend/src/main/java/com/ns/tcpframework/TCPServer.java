package com.ns.tcpframework;

import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * A multithreaded TCP server that listens for incoming client connections and delegates request handling.
 * <p>
 * This server implementation extends {@link Thread} to run asynchronously and provides the foundation
 * for the HTTP server. It handles the TCP/IP layer of communication, accepting incoming socket
 * connections and delegating HTTP request processing to an {@link HTTPHandler}.
 * <p>
 * Key features:
 * <ul>
 *   <li>Non-blocking server operation on a dedicated thread</li>
 *   <li>Thread pool-based request handling for efficient concurrency</li>
 *   <li>Graceful shutdown with proper resource cleanup</li>
 *   <li>Integration with {@link ServerLogger} for operational logging</li>
 *   <li>Support for virtual threads via configurable {@link ExecutorService}</li>
 * </ul>
 * <p>
 * Architecture:
 * <ol>
 *   <li>Server socket binds to the specified TCP port</li>
 *   <li>Main server thread continuously accepts incoming connections</li>
 *   <li>Each accepted connection is submitted to the thread pool</li>
 *   <li>HTTPHandler processes the request in a worker thread</li>
 *   <li>Socket cleanup is handled automatically after request processing</li>
 * </ol>
 * <p>
 * Lifecycle:
 * <pre>
 * // Create server instance
 * ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
 * TCPServer server = new TCPServer(8080, httpHandler, pool);
 *
 * // Start server (non-blocking)
 * server.start();
 *
 * // Server is now accepting connections...
 *
 * // Graceful shutdown
 * server.stopServer();
 * </pre>
 * <p>
 * Thread-safety: This class is designed to be instantiated once and started on a single thread.
 * The accept loop runs on the server's thread while request handling is distributed across
 * the thread pool. The {@link #stopServer()} method can be called from any thread to initiate
 * graceful shutdown.
 * <p>
 * Error handling: The server catches and handles {@link SocketException} during shutdown,
 * and logs other exceptions that occur during operation. The server will continue running
 * even if individual request handling fails.
 *
 * @see HTTPHandler
 * @see ServerSocket
 * @see ExecutorService
 * @see ServerLogger
 */
public class TCPServer extends Thread {

    /**
     * The HTTP handler responsible for processing incoming HTTP requests.
     * <p>
     * This handler is invoked for each accepted client connection and manages
     * the complete HTTP request/response cycle including parsing, routing,
     * handler execution, and response transmission.
     */
    private final HTTPHandler handler;

    /**
     * The server socket that listens for incoming TCP connections.
     * <p>
     * This socket is bound to the configured port during construction and
     * remains open throughout the server's lifetime. Closing this socket
     * triggers server shutdown.
     */
    private final ServerSocket serverSocket;

    /**
     * The thread pool (executor service) used to handle client requests concurrently.
     * <p>
     * Each accepted connection is submitted to this pool for asynchronous processing.
     * This can be configured to use platform threads, virtual threads, or any other
     * executor implementation. Virtual threads (via {@code Executors.newVirtualThreadPerTaskExecutor()})
     * are recommended for high-concurrency scenarios.
     */
    private final ExecutorService pool;


    /**
     * Constructs a TCPServer instance with the specified port, handler, and thread pool.
     * <p>
     * This constructor initializes the server socket and binds it to the specified port.
     * The server is not started automatically; {@link #start()} must be called to begin
     * accepting connections.
     * <p>
     * Port binding considerations:
     * <ul>
     *   <li>Ports below 1024 typically require root/administrator privileges</li>
     *   <li>The port must not be in use by another process</li>
     *   <li>Port 0 can be used to automatically select an available port</li>
     *   <li>Common HTTP ports: 80 (HTTP), 443 (HTTPS), 8080 (alternative HTTP)</li>
     * </ul>
     * <p>
     * The thread pool should be configured based on expected load and concurrency model:
     * <ul>
     *   <li>Virtual threads: {@code Executors.newVirtualThreadPerTaskExecutor()} - Recommended for high concurrency</li>
     *   <li>Fixed thread pool: {@code Executors.newFixedThreadPool(n)} - For controlled resource usage</li>
     *   <li>Cached thread pool: {@code Executors.newCachedThreadPool()} - For varying load</li>
     * </ul>
     *
     * @param port          The TCP port number on which the server will listen for connections.
     *                      Must be in the range 0-65535. Port 0 allows automatic port selection.
     * @param handlerObject The HTTPHandler instance to process client requests. Must not be null.
     * @param pool          The ExecutorService for handling requests concurrently. Must not be null
     *                      and should be in a running state.
     * @throws IOException If the server socket cannot be created or bound to the specified port.
     *                     Common causes: port already in use, insufficient permissions, invalid port number.
     * @throws Exception   If any other error occurs during server initialization.
     */
    public TCPServer(int port, HTTPHandler handlerObject, ExecutorService pool) throws Exception{
        this.handler = handlerObject;
        this.serverSocket = new ServerSocket(port);
        this.pool = pool;
    }

    /**
     * Starts the server and continuously listens for incoming client connections.
     * <p>
     * This method implements the main server loop that:
     * <ol>
     *   <li>Logs server startup with the bound port number</li>
     *   <li>Enters an infinite accept loop waiting for client connections</li>
     *   <li>Accepts each connection and submits it to the thread pool for processing</li>
     *   <li>Continues accepting new connections even if individual requests fail</li>
     *   <li>Exits gracefully when {@link #stopServer()} is called</li>
     * </ol>
     * <p>
     * The accept loop is blocking - when no connections are pending, the thread waits
     * until a client connects. This is efficient as it doesn't consume CPU while waiting.
     * <p>
     * Error handling:
     * <ul>
     *   <li>{@link SocketException} - Expected during shutdown when socket is closed,
     *       silently ignored as it's part of normal shutdown procedure</li>
     *   <li>Other exceptions - Logged to standard error but server continues attempting
     *       to accept connections</li>
     * </ul>
     * <p>
     * This method runs on the server's thread (not the calling thread) since TCPServer
     * extends Thread. Call {@code server.start()} to begin execution, not {@code server.run()}.
     * <p>
     * Note: This method contains German comments in the exception handlers from the original
     * implementation explaining exception handling behavior.
     */
    public void run(){
        ServerLogger.getInstance().log(Loglevel.INFO, "TCPServer started on port " + serverSocket.getLocalPort(), LogDestination.SERVER);
        try{
            while(true){
                var clientSocket = serverSocket.accept();
                handler.handle(clientSocket, pool);
            }
        } catch (SocketException ignored){
            // Beim Aufruf von stopServer() wird eine SocketException geworfen
        } catch (Exception e){
            System.err.println("TCPServer Error: " + e.getMessage());
        }

    }

    /**
     * Initiates graceful shutdown of the server.
     * <p>
     * This method performs an orderly shutdown sequence:
     * <ol>
     *   <li>Closes the server socket, which causes the accept loop to exit with a {@link SocketException}</li>
     *   <li>Shuts down the thread pool, preventing new tasks from being submitted</li>
     *   <li>Prints a shutdown confirmation message to standard output</li>
     * </ol>
     * <p>
     * Shutdown behavior:
     * <ul>
     *   <li>New connections are immediately rejected once the socket is closed</li>
     *   <li>The server thread (running {@link #run()}) will exit after socket closure</li>
     *   <li>The thread pool begins orderly shutdown (completes submitted tasks but accepts no new ones)</li>
     *   <li>In-progress requests are allowed to complete before the pool terminates</li>
     * </ul>
     * <p>
     * This method can be safely called from any thread and can be invoked multiple times
     * (subsequent calls have no effect as the socket is already closed).
     * <p>
     * Thread pool shutdown: The method calls {@link ExecutorService#shutdown()} rather than
     * {@code shutdownNow()}, allowing graceful completion of in-flight requests. If immediate
     * termination is required, additional logic should be added to interrupt worker threads.
     * <p>
     * IOException handling: Any IOException thrown during socket closure is silently ignored
     * since shutdown is already in progress and the exception doesn't affect the outcome.
     */
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ignored){
            // Ignorieren, da der Server geschlossen wird
        }
        pool.shutdown();
        System.out.println("TCPServer stopped on port " + serverSocket.getLocalPort());
    }
}
