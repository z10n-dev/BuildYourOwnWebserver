package com.ns.webserver;

import com.ns.tcpframework.*;
import com.ns.webserver.handlers.*;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.reqeustHandlers.sse.SSEHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        // args[0] = environment

        ServerConfig config = ConfigLoader.load(args.length > 0 ? args[0] : "prod");
        System.out.println(config);

        VirtualHostManager vhost = new VirtualHostManager(config.getDefaultHost());

        config.getHosts().forEach((hostname, vHostConfig) -> {
            vhost.registerVirtualHost(vHostConfig);
        });

        HTTPHandler serverHandler = new HTTPHandler(vhost);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        SSEHandler sseHandler = vhost.getSSEHandler();
        ServerLogger.initialize(sseHandler, config.getLoglevel());

        try {
            TCPServer server = new TCPServer(config.getPort(), serverHandler, executor);
            server.start();
        } catch (Exception e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Server failed to start: " + e.getMessage(), LogDestination.SERVER);
        }



    }
}