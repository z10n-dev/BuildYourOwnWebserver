package com.ns.webserver.handlers;

import tcpframework.reqeustHandlers.sse.SSEEvent;
import tcpframework.reqeustHandlers.sse.SSEHandler;

public class ServerLogger extends Thread {
    private static ServerLogger instance;
    private SSEHandler sseHandler;

    private ServerLogger(SSEHandler sseHandler) {
        this.sseHandler = sseHandler;
    }

    public static void initialize(SSEHandler sseHandler) {
        if (instance == null) {
            instance = new ServerLogger(sseHandler);
            instance.start();
        }
    }

    public static ServerLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServerLogger not initialized");
        }
        return instance;
    }

    @Override
    public void run() {
        System.out.println("ServerLogger started.");
    }

    public void newLog(String logMessage) {
        sseHandler.broadcast(SSEEvent.LOG ,logMessage);
    }
}
