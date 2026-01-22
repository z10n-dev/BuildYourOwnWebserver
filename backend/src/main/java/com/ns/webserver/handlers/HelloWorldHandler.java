package com.ns.webserver.handlers;

import org.json.HTTP;
import tcpframework.*;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloWorldHandler extends RequestHandler {

    @Override
    public void handle(HTTPRequest request, Socket socket) {
        try {
            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setBody("<h1>Hello, World!</h1>".getBytes(StandardCharsets.UTF_8), "text/html; charset=UTF-8");
            response.send(socket);

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
