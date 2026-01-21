package com.ns.webserver;

import tcpframework.*;

import java.net.Socket;

public class HTTP1Handler extends AbstractHandler {
    private final RouterConfig router;

    public HTTP1Handler(RouterConfig router) {
        this.router = router;
    }

    @Override
    protected void runTask(Socket socket) {
        try {
            HTTPRequest request = HTTPRequestParser.parseHTTPRequest(socket);
//            System.out.println(request);

            RequestHandler handler = router.findHandler(request);
            handler.handle(request, socket);
        } catch (Exception e) {
            HTTPErrorHandler.handleException(socket,e);
        }
    }
}