package com.ns.tcpframework.logger;

import com.ns.tcpframework.reqeustHandlers.sse.SSEEvent;
import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerLogger implements Runnable {
    private static ServerLogger instance;
    private final SSEHandler sseHandler;
    private final Loglevel loglevel;
    private BlockingQueue<Log> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    private ServerLogger(SSEHandler sseHandler, Loglevel loglevel) {
        this.loglevel = loglevel;
        this.sseHandler = sseHandler;
    }

    public static void initialize(SSEHandler sseHandler, Loglevel loglevel) {
        if (instance == null) {
            instance = new ServerLogger(sseHandler, loglevel);
            Thread.ofVirtual().name("ServerLogger-Thread").start(instance);
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
        log(Loglevel.INFO, "ServerLogger started: " + Thread.currentThread().getName(), LogDestination.EVERYWHERE);

        while (running) {
            try {
                Log log = logQueue.take();
                processLog(log);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void log(Loglevel logLevel, String message, LogDestination destination) {
        logQueue.offer(new Log(logLevel, message, destination));
    }

    private void processLog(Log log) {
        if (log.getDestination() == LogDestination.SERVER || log.getDestination() == LogDestination.EVERYWHERE) {
            if (log.getLevel().isHigherOrEqual(loglevel)) {
                System.out.println("[" + log.getLevel().name() + "] " + log.getMessage());
            }
        }
        if ((log.getDestination() == LogDestination.CLIENT || log.getDestination() == LogDestination.EVERYWHERE) && sseHandler != null) {
            sseHandler.broadcast(SSEEvent.LOG, log.toJson().toString());
        }
    }

    public void shutdown() {
        running = false;
    }
}
