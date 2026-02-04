package com.ns.tcpframework.reqeustHandlers.sse;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.logger.Stats;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SSEHandler extends RequestHandler {
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();

    @Override
    public HTTPResponse handle(HTTPRequest request) throws IOException {
        throw new IOException("The SSEHandler requires a socket to handle the request.");
    }


    public HTTPResponse handle(HTTPRequest request, Socket socket) throws Exception {
        HTTPResponse response = new HTTPResponse(200, "OK");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send(socket);

        sockets.add(socket);

        ServerLogger.getInstance().log(Loglevel.DEBUG, "SSE Client connected from " + socket.getRemoteSocketAddress(), LogDestination.EVERYWHERE);
        broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));
        Stats.getInstance().setActiveConnections(new AtomicLong(sockets.size()));

            try {
                while (!socket.isClosed()) {
                    Thread.sleep(1000);
                    broadcast(SSEEvent.HEARTBEAT, String.valueOf(sockets.size()));
                    broadcast(SSEEvent.STATS, Stats.getInstance().getStatsAsJson().toString());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ServerLogger.getInstance().log(Loglevel.ERROR, "SSE thread interrupted: " + e.getMessage(), LogDestination.EVERYWHERE);
            }

        return response;

    }

    public void broadcast(SSEEvent event, String message) {
        String formattedMessage = "event: " + event.name().toLowerCase() + "\n" + "data: " + message + "\n\n";
//        System.out.println(formattedMessage);
        sockets.forEach(socket -> {send(socket, formattedMessage);});
    }

    public void send(Socket socket, String message) {
        try{
            socket.getOutputStream().write(message.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e){
            ServerLogger.getInstance().log(Loglevel.WARN, "Error sending data to " + socket.getRemoteSocketAddress() + ": " + e.getMessage() + " -> close Connection", LogDestination.EVERYWHERE);
            removeSocket(socket);
        }
    }

    private void removeSocket(Socket socket) {
        if (!sockets.remove(socket)) {
            return; // Already removed, prevent duplicate processing
        }

        try {
            socket.close();
        } catch (IOException e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Error closing socket: " + e.getMessage(), LogDestination.EVERYWHERE);
        }

        ServerLogger.getInstance().log(Loglevel.DEBUG, "SSE Client disconnected from " + socket.getRemoteSocketAddress(), LogDestination.EVERYWHERE);
        broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));
        Stats.getInstance().setActiveConnections(new AtomicLong(sockets.size()));
    }


    public boolean hasClients() {
        return !sockets.isEmpty();
    }

    public int clientCount() {
        return sockets.size();
    }
}
