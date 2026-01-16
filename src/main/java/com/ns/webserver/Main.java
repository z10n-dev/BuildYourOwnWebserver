package com.ns.webserver;

import tcpframework.TCPServer;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTP1Handler serverHandler = new HTTP1Handler();
        TCPServer server = new TCPServer(8080, serverHandler);
        server.start();
    }
}