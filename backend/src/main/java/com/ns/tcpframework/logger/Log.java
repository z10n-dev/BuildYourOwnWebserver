package com.ns.tcpframework.logger;

import org.json.JSONObject;

public class Log {
    private final Loglevel level;
    private final String message;
    private final long timestamp;
    private final LogDestination destination;

    public Log(Loglevel level, String message, LogDestination destination) {
        this.level = level;
        this.message = message;
        this.destination = destination;
        this.timestamp = System.currentTimeMillis();
    }

    public Loglevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LogDestination getDestination() {
        return destination;
    }

    public JSONObject toJson() {
        final JSONObject obj = new JSONObject();
        obj.put("level", level.name());
        obj.put("message", message);
        obj.put("timestamp", timestamp);
        return obj;
    }
}
