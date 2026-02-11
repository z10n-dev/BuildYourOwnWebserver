package com.ns.tcpframework.logger;

import com.ns.tcpframework.reqeustHandlers.sse.SSEEvent;
import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A singleton logger implementation that processes log messages asynchronously.
 * <p>
 * The ServerLogger runs on a separate virtual thread and processes log entries from a queue.
 * It can send logs to the server console, to connected clients via SSE (Server-Sent Events),
 * or to both destinations based on the log's configuration.
 * <p>
 * This class implements the Runnable interface and processes logs in a non-blocking manner
 * using a {@link BlockingQueue}.
 */
public class ServerLogger implements Runnable {
    /** The singleton instance of the ServerLogger. */
    private static ServerLogger instance;

    /** Handler for sending log messages to clients via Server-Sent Events. */
    private SSEHandler sseHandler;

    /** The minimum log level that will be printed to the server console. */
    private Loglevel loglevel;

    /** Thread-safe queue for storing log entries to be processed. */
    private BlockingQueue<Log> logQueue = new LinkedBlockingQueue<>();

    /** Flag indicating whether the logger thread should continue running. */
    private volatile boolean running = true;

    /**
     * Private constructor to enforce singleton pattern.
     * <p>
     * Initializes the ServerLogger with the specified SSE handler and log level.
     *
     * @param sseHandler The SSE handler for broadcasting logs to connected clients.
     * @param loglevel   The minimum log level for server console output.
     */
    private ServerLogger(SSEHandler sseHandler, Loglevel loglevel) {
        this.loglevel = loglevel;
        this.sseHandler = sseHandler;
    }

    /**
     * Initializes the singleton ServerLogger instance and starts its processing thread.
     * <p>
     * This method should be called once during application startup. If called multiple times,
     * subsequent calls will be ignored.
     *
     * @param sseHandler The SSE handler for broadcasting logs to connected clients.
     * @param loglevel   The minimum log level for server console output.
     */
    public static void initialize(SSEHandler sseHandler, Loglevel loglevel) {
        if (instance == null) {
            instance = new ServerLogger(sseHandler, loglevel);
            // Start the logger on a virtual thread for efficient concurrent processing
            Thread.ofVirtual().name("ServerLogger-Thread").start(instance);
        }
    }

    /**
     * Returns the singleton instance of the ServerLogger.
     *
     * @return The ServerLogger instance.
     * @throws IllegalStateException if the logger has not been initialized via {@link #initialize(SSEHandler, Loglevel)}.
     */
    public static ServerLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServerLogger not initialized");
        }
        return instance;
    }

    /**
     * Sets or updates the SSE handler for broadcasting logs to clients.
     * <p>
     * This method can be used to update the SSE handler after initialization if needed,
     * for example, when the handler is created or becomes available later in the application lifecycle.
     *
     * @param sseHandler The new SSE handler instance.
     */
    public void setSseHandler(SSEHandler sseHandler) {
        // This method can be used to set the SSEHandler after initialization if needed
        this.sseHandler = sseHandler;
    }

    /**
     * Updates the minimum log level for server console output.
     * <p>
     * Only log messages with a level equal to or higher than this level will be printed
     * to the server console. This does not affect logs sent to clients via SSE.
     *
     * @param loglevel The new minimum log level.
     */
    public void setLogLevel(Loglevel loglevel) {
        this.loglevel = loglevel;
    }

    /**
     * Main processing loop for the logger thread.
     * <p>
     * This method runs continuously in a virtual thread, taking log entries from the queue
     * and processing them. It blocks when the queue is empty and waits for new log entries.
     * The loop continues until {@link #shutdown()} is called or the thread is interrupted.
     */
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

    /**
     * Adds a log entry to the processing queue.
     * <p>
     * This method is non-blocking and adds the log to the queue for asynchronous processing.
     * The log will be processed by the logger thread and sent to the appropriate destination(s).
     *
     * @param logLevel    The severity level of the log message.
     * @param message     The content of the log message.
     * @param destination The destination(s) where the log should be sent (SERVER, CLIENT, or EVERYWHERE).
     */
    public void log(Loglevel logLevel, String message, LogDestination destination) {
        logQueue.offer(new Log(logLevel, message, destination));
    }

    /**
     * Processes a single log entry by sending it to the appropriate destination(s).
     * <p>
     * This method handles the routing logic for log messages based on their destination:
     * <ul>
     *   <li>SERVER or EVERYWHERE: Prints to server console if log level is sufficient</li>
     *   <li>CLIENT or EVERYWHERE: Broadcasts to connected clients via SSE</li>
     * </ul>
     *
     * @param log The log entry to process.
     */
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

    /**
     * Gracefully shuts down the logger by stopping the processing loop.
     * <p>
     * This method sets the running flag to false, which causes the main processing loop
     * in {@link #run()} to exit after processing the current log entry. Any remaining
     * logs in the queue will not be processed after shutdown.
     */
    public void shutdown() {
        running = false;
    }
}
