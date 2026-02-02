package com.ns.webserver;

import com.ns.webserver.handlers.*;
import tcpframework.*;
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

        VirtualHostConfig defaultVHost = new VirtualHostConfig("default", "static");
        VirtualHostManager vhost = new VirtualHostManager(defaultVHost);

        VirtualHostConfig domain1 = new VirtualHostConfig("a.localhost", "static");
        domain1.getRouter().register("/hello", new HelloWorldHandler());
        vhost.registerVirtualHost(domain1);

        VirtualHostConfig domain2 = new VirtualHostConfig("localhost", args[0]);
        ToDoHandler toDoHandler = new ToDoHandler();
        SSEHandler sseHandler = new SSEHandler();
        vhost.registerVirtualHost(domain2);

        // TODO: Make sub-path registrations in the same way as other handlers
        domain2.getRouter().register("/api/todos", toDoHandler);
        domain2.getRouter().register("/api/todos/*", toDoHandler);
        domain2.getRouter().register("/api/sse", sseHandler);

        HTTPHandler serverHandler = new HTTPHandler(vhost);
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