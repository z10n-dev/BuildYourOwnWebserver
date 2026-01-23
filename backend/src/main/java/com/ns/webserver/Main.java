package com.ns.webserver;

import com.ns.webserver.handlers.HelloWorldHandler;
import com.ns.webserver.handlers.SSEHandler;
import com.ns.webserver.handlers.ServerLogger;
import com.ns.webserver.handlers.ToDoHandler;
import tcpframework.HTTPHandler;
import tcpframework.RouterConfig;
import tcpframework.TCPServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // args[0] = root path
        // args[1] = port number

        RouterConfig router = new RouterConfig(args[0]);
        router.register("/hello", new HelloWorldHandler());
        ToDoHandler toDoHandler = new ToDoHandler();
        SSEHandler sseHandler = new SSEHandler();

        // TODO: Make sub-path registrations in the same way as other handlers
        router.register("/api/todos", toDoHandler);
        router.register("/api/todos/*", toDoHandler);
        router.register("/api/sse", sseHandler);

        HTTPHandler serverHandler = new HTTPHandler(router);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new ServerLogger(sseHandler));

        try {
            TCPServer server = new TCPServer(Integer.parseInt(args[1]), serverHandler, executor);
            server.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }



    }
}