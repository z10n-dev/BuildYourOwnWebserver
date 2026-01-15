package com.ns.webserver;

import tcpframework.AbstractHandler;
import tcpframework.HTTPRequest;
import tcpframework.HTTPRequestHandler;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloWorldServer extends AbstractHandler {
    @Override
    public void runTask(Socket socket) {
        try {
            var out = new BufferedOutputStream(socket.getOutputStream());
            InputStream in = socket.getInputStream();
            HTTPRequestHandler parser = new HTTPRequestHandler();
            HTTPRequest request = parser.parseHTTPRequest(in);
            System.out.println(request);


            out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("Content-Type: text/html; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("Connection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.write('\t');
            out.write("<h1>Hello World!</h1>".getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e){
            System.err.println("HelloWorldServer Error: " + e.getMessage());
        }
    }
}
