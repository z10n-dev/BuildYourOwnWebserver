package com.ns.webserver.handlers;

import com.sun.net.httpserver.Request;
import org.json.HTTP;
import tcpframework.HTTPRequest;
import tcpframework.HTTPResponse;
import tcpframework.RequestHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SSEHandler extends RequestHandler {
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();

    @Override
    public void handle(HTTPRequest request, Socket socket) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send(socket);

        sockets.add(socket);

        System.out.println("Client connected. Active connections: " + sockets.size());

        try {
            while (!socket.isClosed()) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            removeSocket(socket);
        }

    }

    public void broadcast(String message) {
        String formattedMessage = "data: " + message + "\n\n";
        sockets.forEach(socket -> {send(socket, formattedMessage);});
    }

    public void send(Socket socket, String message) {
        try{
            socket.getOutputStream().write(message.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e){
            System.err.println("Error sending data: " + e.getMessage());
            removeSocket(socket);
        }
    }

    private void removeSocket(Socket socket) {
        sockets.remove(socket);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        } finally {
            System.out.println("Client disconnected. Active connections: " + sockets.size());
        }
    }

    public boolean hasClients() {
        return !sockets.isEmpty();
    }

    public int clientCount() {
        return sockets.size();
    }
}
