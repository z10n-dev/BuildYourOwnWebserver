package com.ns.tcpframework.logger;

import org.json.JSONObject;

/**
 * Represents a log entry with a specific log level, message, timestamp, and destination.
 */
public class Log {
    private final Loglevel level;
    private final String message;
    private final long timestamp;
    private final LogDestination destination;

    /**
     * Constructs a new Log instance.
     *
     * @param level       The severity level of the log.
     * @param message     The message content of the log.
     * @param destination The destination where the log will be sent.
     */
    public Log(Loglevel level, String message, LogDestination destination) {
        this.level = level;
        this.message = message;
        this.destination = destination;
        this.timestamp = System.currentTimeMillis();
    }


    /**
     * Gets the severity level of the log.
     *
     * @return The log level.
     */
    public Loglevel getLevel() {
        return level;
    }

        /**
        * Gets the message content of the log.
        *
        * @return The log message.
        */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the timestamp of when the log was created.
     *
     * @return The log timestamp in milliseconds since epoch.
     */
    public LogDestination getDestination() {
        return destination;
    }

    /**
     * Converts the log entry to a JSON object for structured logging.
     *
     * @return A JSONObject representing the log entry.
     */
    public JSONObject toJson() {
        final JSONObject obj = new JSONObject();
        obj.put("level", level.name());
        obj.put("message", message);
        obj.put("timestamp", timestamp);
        return obj;
    }
}
