package tcpframework.reqeustHandlers.sse;

import tcpframework.HTTPRequest;
import tcpframework.HTTPResponse;
import tcpframework.reqeustHandlers.RequestHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        response.send(request.getSocket());

        sockets.add(request.getSocket());

        System.out.println("Client connected. Active connections: " + sockets.size());
        broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));

            try {
                while (!request.getSocket().isClosed()) {
                    Thread.sleep(1000);
                    broadcast(SSEEvent.HEARTBEAT, String.valueOf(sockets.size()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("SSE thread interrupted: " + e.getMessage());
            } finally {
                removeSocket(request.getSocket());
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
            broadcast(SSEEvent.CONNECTED, String.valueOf(sockets.size()));
        }
    }

    public boolean hasClients() {
        return !sockets.isEmpty();
    }

    public int clientCount() {
        return sockets.size();
    }
}
