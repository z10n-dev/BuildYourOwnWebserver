package com.ns.webserver.handlers;

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
//        while (true) {
//            try {
//                Thread.sleep(1000);
//                String logMessage = "Active SSE connections: " + sseHandler.clientCount();
//                System.out.println(logMessage);
//                sseHandler.broadcast(logMessage);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
    }

    public void newLog(String logMessage) {
        sseHandler.broadcast(logMessage);
    }
}
