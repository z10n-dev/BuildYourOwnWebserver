package com.ns.webserver;

import tcpframework.AbstractHandler;
import tcpframework.HTTPRequest;
import tcpframework.HTTPRequestHandler;
import tcpframework.HTTPResponseHandler;

import java.net.Socket;

public class HTTP1Handler extends AbstractHandler {
    @Override
    protected void runTask(Socket socket) {
        try {
            HTTPRequest request = HTTPRequestHandler.parseHTTPRequest(socket);
            System.out.println(request);
            HTTPResponseHandler responseHandler = new HTTPResponseHandler("src/main/resources/static");
            responseHandler.handleRequest(request, socket);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
