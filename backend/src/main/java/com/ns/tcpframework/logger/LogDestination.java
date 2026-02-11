package com.ns.tcpframework.logger;

/**
 * Enumeration representing the possible destinations for log messages.
 * <p>
 * This enum defines where log entries should be sent or displayed within the system.
 */
public enum LogDestination {
    /**
     * Indicates that the log message should be sent to the server-side logging system.
     */
    SERVER,
    /**
     * Indicates that the log message should be sent to the client-side logging system.
     */
    CLIENT,
    /**
     * Indicates that the log message should be sent to both the server and client logging systems.
     */
    EVERYWHERE
}
