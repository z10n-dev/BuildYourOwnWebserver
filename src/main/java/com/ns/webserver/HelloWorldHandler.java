package com.ns.webserver;

import tcpframework.AbstractHandler;
import tcpframework.HTTPRequest;
import tcpframework.HTTPRequestHandler;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloWorldHandler extends AbstractHandler {
    @Override
    protected void runTask(Socket socket) {
        try {
            var out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            InputStream in = socket.getInputStream();
            HTTPRequest request = HTTPRequestHandler.parseHTTPRequest(socket);
            System.out.println(request);


            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: text/html; charset=utf-8\r\n");
            out.write("Connection: close\r\n\r\n");
            out.write('\t');
            out.write("<h1>Hello World!</h1>");
            out.close();
        } catch (Exception e){
            System.err.println("HelloWorldHandler Error: " + e.getMessage());
        }
    }
}
