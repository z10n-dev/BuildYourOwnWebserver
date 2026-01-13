package com.ns.webserver;

public class Main {
    public static void main(String[] args) {
        Server1 server = new Server1(8080);
        server.startServer();
    }
}