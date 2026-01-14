package com.ns.webserver;

import tcpframework.AbstractHandler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HelloWorldServer extends AbstractHandler {
    @Override
    public void runTask(Socket socket) {
        try {
            var out = new BufferedOutputStream(socket.getOutputStream());
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("Content-Type: text/html; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("Connection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            // Read and echo HTTP request in Server Console
            var n = 0;
            var length = 0;
            String line;

            while (true) {
                line = in.readLine();
                System.out.println(line);

                if (line == null || line.isEmpty()) break;

                if (line.startsWith("Content-Length: ")) {
                    length = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                }
            }

            out.write('\t');
            for (int i = 0; i < length; i++) {
                int c = in.read();
                if (c == -1) break;
                out.write(c);
            }
            out.flush();

            out.write('\t');
            out.write("<h1>Hello World!</h1>".getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e){}
    }
}
