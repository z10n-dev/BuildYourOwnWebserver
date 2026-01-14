// java
package com.ns.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class Server1 {
    private final int port;

    public Server1(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on " + serverSocket.getLocalSocketAddress() + ":" + port);
            process(serverSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(ServerSocket serverSocket) {
        while (true) {
            SocketAddress socketAddress = null;

            try (var socket = serverSocket.accept()) {
                var out = new BufferedOutputStream(socket.getOutputStream());
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                socketAddress = socket.getRemoteSocketAddress();
                System.out.println("Connection established with " + socketAddress);

                out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("Content-Type: text/html; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("Connection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();

                // Read and echo HTTP request
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
            } catch (EOFException ignored) {
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                System.out.println("Connection closed with " + socketAddress);
            }
        }
    }
}
