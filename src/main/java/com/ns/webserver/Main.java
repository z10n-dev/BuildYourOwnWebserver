package com.ns.webserver;

import tcpframework.TCPServer;

public class Main {
    public static void main(String[] args) throws Exception {
        HelloWorldServer helloWorldServer = new HelloWorldServer();
        TCPServer server = new TCPServer(8080, helloWorldServer);
        server.start();
    }
}