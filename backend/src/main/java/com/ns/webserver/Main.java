package com.ns.webserver;

import com.ns.webserver.handlers.*;
import tcpframework.HTTPHandler;
import tcpframework.RouterConfig;
import tcpframework.TCPServer;
import tcpframework.logger.LogDestination;
import tcpframework.logger.Loglevel;
import tcpframework.logger.ServerLogger;
import tcpframework.reqeustHandlers.sse.SSEHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // args[0] = root path
        // args[1] = port number
        // args[2] = (optional) Log Level

        RouterConfig router = new RouterConfig(args[0]);
        router.register("/hello", new HelloWorldHandler());
        ToDoHandler toDoHandler = new ToDoHandler();
        SSEHandler sseHandler = new SSEHandler();

        // TODO: Make sub-path registrations in the same way as other handlers
        router.register("/api/todos", toDoHandler);
        router.register("/api/todos/*", toDoHandler);
        router.register("/api/sse", sseHandler);

        HTTPHandler serverHandler = new HTTPHandler(router);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ServerLogger.initialize(sseHandler, args.length > 2 ? Loglevel.valueOf(args[2].toUpperCase()) : Loglevel.INFO);

        try {
            TCPServer server = new TCPServer(Integer.parseInt(args[1]), serverHandler, executor);
            server.start();
        } catch (Exception e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Server failed to start: " + e.getMessage(), LogDestination.SERVER);
        }



    }
}