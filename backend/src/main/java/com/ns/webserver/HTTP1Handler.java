package com.ns.webserver;

import tcpframework.AbstractHandler;
import tcpframework.HTTPRequest;
import tcpframework.HTTPRequestHandler;
import tcpframework.HTTPResponseHandler;

import java.net.Socket;

public class HTTP1Handler extends AbstractHandler {
    private final String rootPath;

    public HTTP1Handler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    protected void runTask(Socket socket) {
        try {
            HTTPRequest request = HTTPRequestHandler.parseHTTPRequest(socket);
            System.out.println(request);
            HTTPResponseHandler responseHandlerLocal = new HTTPResponseHandler(rootPath);
//            HTTPResponseHandler responseHandlerLocal = new HTTPResponseHandler("backend/src/main/resources/static");
            responseHandlerLocal.handleRequest(request, socket);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}