package com.ns.webserver;

import com.ns.webserver.handlers.HelloWorldHandler;
import tcpframework.RouterConfig;
import tcpframework.TCPServer;

public class Main {
    public static void main(String[] args) {
        // args[0] = root path
        // args[1] = port number

        RouterConfig router = new RouterConfig(args[0]);
        router.register("/hello", new HelloWorldHandler());

        HTTP1Handler serverHandler = new HTTP1Handler(router);

        try {
            TCPServer server = new TCPServer(Integer.parseInt(args[1]), serverHandler);
            server.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}