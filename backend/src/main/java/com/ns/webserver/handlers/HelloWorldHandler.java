package com.ns.webserver.handlers;

import tcpframework.AbstractHandler;
import tcpframework.HTTPRequest;
import tcpframework.HTTPRequestParser;
import tcpframework.RequestHandler;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloWorldHandler extends RequestHandler {

    @Override
    public void handle(HTTPRequest request, Socket socket) {
        try {
            sendResponse(socket, "text/html", "<h1>Hello, World!</h1>".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e){
            System.err.println("HelloWorldHandler Error: " + e.getMessage());
        }
    }

    @Override
    protected void handleGetRequest(HTTPRequest request, Socket socket) throws Exception {

    }

    @Override
    protected void handlePostRequest(HTTPRequest request, Socket socket) throws Exception {

    }

    @Override
    protected void handlePutRequest(HTTPRequest request, Socket socket) throws Exception {

    }

    @Override
    protected void handleDeleteRequest(HTTPRequest request, Socket socket) throws Exception {

    }


}
