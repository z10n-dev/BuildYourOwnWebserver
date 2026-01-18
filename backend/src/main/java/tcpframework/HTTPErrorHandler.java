package tcpframework;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HTTPErrorHandler {
    private static void sendError(Socket socket, int statusCode, String message) {
        try {
            OutputStream out = socket.getOutputStream();
            String response = String.format("HTTP/1.1 %d %s\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n\r\n<h1>%d %s</h1>", statusCode, message, statusCode, message);
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }

    public static void sendBadRequest(Socket socket) {
        sendError(socket, 400, "Bad Request");
    }

    public static void sendNotFound(Socket socket) {
        sendError(socket, 404, "Not Found");
    }

    public static void sendInternalError(Socket socket) {
        sendError(socket, 500, "Internal Server Error");
    }

    public static void sendMethodNotAllowed(Socket socket) {
        sendError(socket, 405, "Method Not Allowed");
    }

    public static void sendNotImplemented(Socket socket) {
        sendError(socket, 501, "Not Implemented");
    }
}
