package com.ns.tcpframework.reqeustHandlers.sse;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.logger.Stats;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handler for Server-Sent Events (SSE) connections.
 * <p>
 * This class manages SSE connections between the server and multiple clients, enabling
 * real-time, one-way communication from server to clients. It maintains a pool of active
 * socket connections and broadcasts events such as logs, statistics, heartbeats, and
 * connection status updates to all connected clients.
 * <p>
 * The handler supports the following event types:
 * <ul>
 *   <li>{@link SSEEvent#CONNECTED} - Notifies clients of connection establishment</li>
 *   <li>{@link SSEEvent#HEARTBEAT} - Periodic keep-alive messages</li>
 *   <li>{@link SSEEvent#STATS} - Server statistics and performance metrics</li>
 *   <li>{@link SSEEvent#LOG} - Server log messages</li>
 * </ul>
 * <p>
 * Thread-safety: This class uses a {@link ConcurrentHashMap}-backed set to safely
 * manage socket connections across multiple threads.
 *
 * @see SSEEvent
 * @see RequestHandler
 */
public class SSEHandler extends RequestHandler {
    /**
     * Thread-safe set of active SSE client socket connections.
     * <p>
     * Uses a ConcurrentHashMap-backed set to allow safe concurrent access
     * from multiple threads broadcasting to or managing client connections.
     */
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();

    /**
     * Not supported - SSE requires socket access.
     * <p>
     * This method is inherited from {@link RequestHandler} but cannot be used for SSE
     * connections because they require direct socket access for streaming. Always throws
     * an IOException directing users to use {@link #handle(HTTPRequest, Socket)} instead.
     *
     * @param request The HTTP request (ignored).
     * @return Never returns normally.
     * @throws IOException Always thrown with message indicating socket is required.
     */
    @Override
    public HTTPResponse handle(HTTPRequest request) throws IOException {
        throw new IOException("The SSEHandler requires a socket to handle the request.");
    }

    /**
     * Handles an SSE connection request by establishing a persistent event stream.
     * <p>
     * This method:
     * <ol>
     *   <li>Sends appropriate SSE headers (Content-Type, Cache-Control, etc.)</li>
     *   <li>Registers the client socket in the active connections pool</li>
     *   <li>Broadcasts a CONNECTED event to all clients with updated connection count</li>
     *   <li>Enters a keep-alive loop that periodically sends:
     *     <ul>
     *       <li>HEARTBEAT events (every 1 second)</li>
     *       <li>STATS events with server metrics (every 1 second)</li>
     *     </ul>
     *   </li>
     *   <li>Cleans up the connection when the socket closes</li>
     * </ol>
     * <p>
     * This method blocks until the socket is closed or an error occurs.
     * It should be run on a dedicated thread per client connection.
     *
     * @param request The HTTP request that initiated the SSE connection.
     * @param socket  The client socket for the SSE stream.
     * @return The HTTP response with SSE headers (sent immediately).
     * @throws Exception if an error occurs during connection setup or maintenance.
     */
    public HTTPResponse handle(HTTPRequest request, Socket socket) throws Exception {
        // Create response with SSE-specific headers
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send(socket);

        sockets.add(socket);

        ServerLogger.getInstance().log(Loglevel.DEBUG, "SSE Client connected from " + socket.getRemoteSocketAddress(), LogDestination.EVERYWHERE);
        broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));
        Stats.getInstance().setActiveConnections(new AtomicLong(sockets.size()));

        try {
            // Keep-alive loop: continuously send heartbeat and stats while connection is open
            while (!socket.isClosed()) {
                Thread.sleep(1000); // Wait 1 second between broadcasts
                broadcast(SSEEvent.HEARTBEAT, String.valueOf(sockets.size()));
                broadcast(SSEEvent.STATS, Stats.getInstance().getStatsAsJson().toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ServerLogger.getInstance().log(Loglevel.ERROR, "SSE thread interrupted: " + e.getMessage(), LogDestination.EVERYWHERE);
        }

        return response;
    }

    /**
     * Broadcasts an SSE event to all connected clients.
     * <p>
     * This method formats the event according to the SSE protocol specification:
     * <pre>
     * event: {eventName}
     * data: {message}
     *
     * </pre>
     * The formatted message is then sent to every active client socket. If sending
     * to any client fails, that client is automatically disconnected and removed
     * from the active connections pool.
     * <p>
     * Thread-safety: This method is thread-safe and can be called concurrently
     * from multiple threads.
     *
     * @param event   The type of SSE event to broadcast (e.g., LOG, STATS, HEARTBEAT).
     * @param message The message payload to send with the event.
     * @see SSEEvent
     */
    public void broadcast(SSEEvent event, String message) {
        String formattedMessage = "event: " + event.name().toLowerCase() + "\n" + "data: " + message + "\n\n";

        sockets.forEach(socket -> {send(socket, formattedMessage);});
    }

    /**
     * Sends a message to a specific client socket.
     * <p>
     * This method attempts to write the message to the socket's output stream and flush it.
     * If an IOException occurs (e.g., client disconnected), the socket is automatically
     * closed and removed from the active connections pool.
     *
     * @param socket  The client socket to send the message to.
     * @param message The message string to send.
     */
    public void send(Socket socket, String message) {
        try{
            socket.getOutputStream().write(message.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e){
            ServerLogger.getInstance().log(Loglevel.WARN, "Error sending data to " + socket.getRemoteSocketAddress() + ": " + e.getMessage() + " -> close Connection", LogDestination.EVERYWHERE);
            removeSocket(socket);
        }
    }

    /**
     * Removes a socket from the active connections pool and closes it.
     * <p>
     * This method performs the following cleanup operations:
     * <ol>
     *   <li>Removes the socket from the active connections set (with duplicate check)</li>
     *   <li>Closes the socket connection</li>
     *   <li>Logs the disconnection event</li>
     *   <li>Broadcasts a CONNECTED event with the updated connection count</li>
     *   <li>Updates the Stats service with the new connection count</li>
     * </ol>
     * <p>
     * This method is idempotent - if the socket has already been removed, the method
     * returns early without performing any operations.
     *
     * @param socket The client socket to remove and close.
     */
    private void removeSocket(Socket socket) {
        if (!sockets.remove(socket)) {
            return; // Already removed, prevent duplicate processing
        }

        try {
            socket.close();
        } catch (IOException e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Error closing socket: " + e.getMessage(), LogDestination.EVERYWHERE);
        }

        ServerLogger.getInstance().log(Loglevel.DEBUG, "SSE Client disconnected from " + socket.getRemoteSocketAddress(), LogDestination.EVERYWHERE);

        broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));

        Stats.getInstance().setActiveConnections(new AtomicLong(sockets.size()));
    }

    /**
     * Checks if there are any active SSE client connections.
     * <p>
     * This is a convenience method for determining whether any clients are currently
     * connected to the SSE stream.
     *
     * @return {@code true} if at least one client is connected, {@code false} otherwise.
     */
    public boolean hasClients() {
        return !sockets.isEmpty();
    }

    /**
     * Returns the current number of active SSE client connections.
     * <p>
     * This method provides a snapshot of the connection count at the time of invocation.
     * The count may change immediately after the method returns due to concurrent
     * connections or disconnections.
     *
     * @return The number of currently connected clients.
     */
    public int clientCount() {
        return sockets.size();
    }
}
