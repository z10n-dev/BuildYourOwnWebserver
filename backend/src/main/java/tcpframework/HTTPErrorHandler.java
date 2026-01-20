package tcpframework;

import tcpframework.exceptions.BadRequestException;
import tcpframework.exceptions.InternalServerErrorException;
import tcpframework.exceptions.NotFoundException;
import tcpframework.exceptions.NotImplementedException;

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

    public static void handleException(Socket socket, Exception e) {
        switch (e) {
            case BadRequestException ignored:
                sendBadRequest(socket);
                System.err.println("Bad Request Exception: " + e.getMessage());
                break;
            case InternalServerErrorException ignored:
                sendInternalError(socket);
                System.err.println("Internal Server Error: " + e.getMessage());
                break;
            case NotFoundException ignored:
                sendNotFound(socket);
                System.err.println("Not Found: " + e.getMessage());
                break;
            case NotImplementedException ignored:
                sendNotImplemented(socket);
                System.err.println("Not Implemented: " + e.getMessage());
                break;
            default:
                sendInternalError(socket);
                System.err.println("Unhandled Exception: " + e.getMessage());
                break;
        }
        e.printStackTrace();
    }
}
