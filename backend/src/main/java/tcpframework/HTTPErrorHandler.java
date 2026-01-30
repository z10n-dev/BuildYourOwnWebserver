package tcpframework;

import tcpframework.exceptions.*;
import tcpframework.logger.LogDestination;
import tcpframework.logger.Loglevel;
import tcpframework.logger.ServerLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for handling HTTP error responses. This class provides methods
 * to send predefined HTTP error responses to a client socket and handle exceptions
 * by mapping them to appropriate HTTP error codes.
 */
public class HTTPErrorHandler {

    /**
     * Sends an HTTP error response to the specified client socket.
     *
     * @param socket     The client socket to send the response to.
     * @param statusCode The HTTP status code to include in the response.
     * @param message    The HTTP status message to include in the response.
     */
    private static void sendError(Socket socket, int statusCode, String message) {
        try (OutputStream out = socket.getOutputStream()) {
            String response = String.format(
                    "HTTP/1.1 %d %s\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n\r\n<h1>%d %s</h1>",
                    statusCode, message, statusCode, message
            );
            out.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Failed to send error response: " + e.getMessage(), LogDestination.SERVER);
        }
    }

    /**
     * Sends a 400 Bad Request HTTP error response.
     *
     * @param socket The client socket to send the response to.
     */
    public static void sendBadRequest(Socket socket) {
        sendError(socket, 400, "Bad Request");
    }

    /**
     * Sends a 404 Not Found HTTP error response.
     *
     * @param socket The client socket to send the response to.
     */
    public static void sendNotFound(Socket socket) {
        sendError(socket, 404, "Not Found");
    }

    /**
     * Sends a 500 Internal Server Error HTTP error response.
     *
     * @param socket The client socket to send the response to.
     */
    public static void sendInternalError(Socket socket) {
        sendError(socket, 500, "Internal Server Error");
    }

    /**
     * Sends a 405 Method Not Allowed HTTP error response.
     *
     * @param socket The client socket to send the response to.
     */
    public static void sendMethodNotAllowed(Socket socket) {
        sendError(socket, 405, "Method Not Allowed");
    }

    /**
     * Sends a 501 Not Implemented HTTP error response.
     *
     * @param socket The client socket to send the response to.
     */
    public static void sendNotImplemented(Socket socket) {
        sendError(socket, 501, "Not Implemented");
    }

    /**
     * Handles an exception by mapping it to an appropriate HTTP error response
     * and sending the response to the specified client socket.
     *
     * @param socket The client socket to send the response to.
     * @param e      The exception to handle.
     */
    public static void handleException(Socket socket, Exception e) {
        if (e instanceof BadRequestException) {
            sendBadRequest(socket);
        } else if (e instanceof InternalServerErrorException) {
            sendInternalError(socket);
        } else if (e instanceof NotFoundException) {
            sendNotFound(socket);
        } else if (e instanceof NotImplementedException) {
            sendNotImplemented(socket);
        } else {
            sendInternalError(socket);
        }
        ServerLogger.getInstance().log(Loglevel.ERROR, "Exception handled: " + e.getMessage(), LogDestination.SERVER);
    }
}
