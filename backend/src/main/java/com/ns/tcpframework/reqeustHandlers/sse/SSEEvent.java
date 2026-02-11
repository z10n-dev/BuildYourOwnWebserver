package com.ns.tcpframework.reqeustHandlers.sse;

/**
 * Enumeration representing different types of Server-Sent Events (SSE) that can be broadcast to clients.
 * <p>
 * This enum defines the event types used in the SSE communication protocol between the server
 * and connected clients. Each event type serves a specific purpose in the real-time
 * communication system.
 */
public enum SSEEvent {
    /**
     * Log event - Used to send server log messages to connected clients.
     * <p>
     * This event type is triggered when the server wants to broadcast log information
     * to client dashboards or monitoring interfaces.
     */
    LOG,

    /**
     * Stats event - Used to send server statistics and performance metrics to clients.
     * <p>
     * This event type carries server metrics such as CPU usage, memory usage, uptime,
     * total requests, and active connections.
     */
    STATS,

    /**
     * Connected event - Used to notify a client that the SSE connection has been established.
     * <p>
     * This event type is typically sent immediately after a client connects to confirm
     * the successful establishment of the SSE stream.
     */
    CONNECTED,

    /**
     * Heartbeat event - Used to keep the SSE connection alive.
     * <p>
     * This event type is sent periodically to prevent connection timeouts and to verify
     * that the connection is still active. Clients can use this to detect disconnections.
     */
    HEARTBEAT
}
