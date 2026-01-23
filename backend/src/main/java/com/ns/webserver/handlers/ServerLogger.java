package com.ns.webserver.handlers;

public class ServerLogger extends Thread{
    SSEHandler sseHandler;

    public ServerLogger(SSEHandler sseHandler) {
        this.sseHandler = sseHandler;
    }

    @Override
    public void run() {
        System.out.println("ServerLogger started.");
        while (true) {
            try {
                Thread.sleep(10000);
                String logMessage = "Active SSE connections: " + sseHandler.clientCount();
                System.out.println(logMessage);
                sseHandler.broadcast(logMessage);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
