package com.ns.tcpframework;

import com.ns.tcpframework.exceptions.BadRequestException;
import com.ns.tcpframework.exceptions.InternalServerErrorException;
import com.ns.tcpframework.exceptions.NotFoundException;
import com.ns.tcpframework.exceptions.NotImplementedException;
import com.ns.tcpframework.exceptions.*;
import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling and sending HTTP error responses.
 * <p>
 * This class provides a centralized mechanism for handling errors in HTTP request
 * processing by generating and sending appropriate HTTP error responses to clients.
 * It supports standard HTTP error codes and provides exception-to-error-code mapping
 * for common error scenarios.
 * <p>
 * Key features:
 * <ul>
 *   <li>Standardized HTTP error response generation with proper headers</li>
 *   <li>Automatic exception-to-HTTP-status-code mapping</li>
 *   <li>Integration with {@link ServerLogger} for error logging</li>
 *   <li>HTML-formatted error messages for browser-friendly display</li>
 * </ul>
 * <p>
 * Supported HTTP error codes:
 * <ul>
 *   <li>400 Bad Request - Malformed or invalid client requests</li>
 *   <li>404 Not Found - Requested resource does not exist</li>
 *   <li>405 Method Not Allowed - HTTP method not supported for the resource</li>
 *   <li>500 Internal Server Error - Unexpected server-side errors</li>
 *   <li>501 Not Implemented - Requested functionality not yet implemented</li>
 * </ul>
 * <p>
 * All error responses follow the HTTP/1.1 specification with:
 * <ul>
 *   <li>Proper status line format</li>
 *   <li>Content-Type header set to text/html with UTF-8 encoding</li>
 *   <li>Connection: close header to indicate non-persistent connections</li>
 *   <li>Simple HTML body with error status code and message</li>
 * </ul>
 * <p>
 * Thread-safety: This class is stateless and all methods are static, making it
 * inherently thread-safe for concurrent use across multiple request handlers.
 * <p>
 * Example usage:
 * <pre>
 * try {
 *     // Process request
 * } catch (NotFoundException e) {
 *     HTTPErrorHandler.handleException(socket, e);
 * }
 * </pre>
 *
 * @see HTTPResponse
 * @see ServerLogger
 */
public class HTTPErrorHandler {

    /**
     * Sends an HTTP error response to the specified client socket.
     * <p>
     * This method constructs a complete HTTP/1.1 error response including status line,
     * headers, and a simple HTML body displaying the error code and message. The response
     * is written directly to the socket's output stream and the stream is closed after
     * sending.
     * <p>
     * Response format:
     * <pre>
     * HTTP/1.1 {statusCode} {message}
     * Content-Type: text/html; charset=utf-8
     * Connection: close
     *
     * &lt;h1&gt;{statusCode} {message}&lt;/h1&gt;
     * </pre>
     * <p>
     * If an IOException occurs while sending the response (e.g., client disconnected),
     * the error is logged but not propagated, ensuring graceful handling of broken
     * connections.
     *
     * @param socket     The client socket to which the error response will be sent.
     *                   Must not be null and should be open.
     * @param statusCode The HTTP status code to include in the response (e.g., 404, 500).
     * @param message    The HTTP status message to include in the response (e.g., "Not Found").
     *                   Must not be null.
     */
    private static void sendError(Socket socket, int statusCode, String message) {
        try (OutputStream out = socket.getOutputStream()) {
            String response = String.format(
                    "HTTP/1.1 %d %s\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\n\r\n<h1>%d %s</h1>",
                    statusCode, message, statusCode, message
            );
            out.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            ServerLogger.getInstance().log(Loglevel.ERROR, "Failed to send error response: " + e.getMessage(), LogDestination.EVERYWHERE);
        }
    }

    /**
     * Sends a 400 Bad Request HTTP error response.
     * <p>
     * This error indicates that the server cannot process the request due to client error,
     * such as malformed request syntax, invalid request message framing, or deceptive
     * request routing.
     * <p>
     * Common scenarios for 400 errors:
     * <ul>
     *   <li>Malformed HTTP headers</li>
     *   <li>Invalid request line format</li>
     *   <li>Missing required headers</li>
     *   <li>Invalid request body format</li>
     * </ul>
     *
     * @param socket The client socket to which the error response will be sent.
     */
    public static void sendBadRequest(Socket socket) {
        sendError(socket, 400, "Bad Request");
    }

    /**
     * Sends a 404 Not Found HTTP error response.
     * <p>
     * This error indicates that the server cannot find the requested resource.
     * The resource may have been deleted, moved, or never existed. This is one of
     * the most common HTTP error codes.
     * <p>
     * Common scenarios for 404 errors:
     * <ul>
     *   <li>Requested file does not exist in the document root</li>
     *   <li>Invalid URL path specified by the client</li>
     *   <li>Resource has been deleted or moved</li>
     *   <li>Route is not registered in the server configuration</li>
     * </ul>
     *
     * @param socket The client socket to which the error response will be sent.
     */
    public static void sendNotFound(Socket socket) {
        sendError(socket, 404, "Not Found");
    }

    /**
     * Sends a 500 Internal Server Error HTTP error response.
     * <p>
     * This error indicates that the server encountered an unexpected condition that
     * prevented it from fulfilling the request. This is a generic error used when
     * no more specific error code is appropriate.
     * <p>
     * Common scenarios for 500 errors:
     * <ul>
     *   <li>Unhandled exceptions in request handlers</li>
     *   <li>Database connection failures</li>
     *   <li>File I/O errors (permission denied, disk full)</li>
     *   <li>Configuration errors</li>
     *   <li>Programming errors (NullPointerException, etc.)</li>
     * </ul>
     *
     * @param socket The client socket to which the error response will be sent.
     */
    public static void sendInternalError(Socket socket) {
        sendError(socket, 500, "Internal Server Error");
    }

    /**
     * Sends a 405 Method Not Allowed HTTP error response.
     * <p>
     * This error indicates that the HTTP method used in the request is not supported
     * for the requested resource. For example, attempting a POST request on a resource
     * that only supports GET.
     * <p>
     * Common scenarios for 405 errors:
     * <ul>
     *   <li>Using POST on a read-only resource</li>
     *   <li>Using DELETE on a resource that doesn't support deletion</li>
     *   <li>Request handler not implementing the requested HTTP method</li>
     * </ul>
     *
     * @param socket The client socket to which the error response will be sent.
     */
    public static void sendMethodNotAllowed(Socket socket) {
        sendError(socket, 405, "Method Not Allowed");
    }

    /**
     * Sends a 501 Not Implemented HTTP error response.
     * <p>
     * This error indicates that the server does not support the functionality required
     * to fulfill the request. Unlike 405 Method Not Allowed, which indicates the method
     * is recognized but not allowed for the resource, 501 indicates the server does not
     * recognize or support the method at all.
     * <p>
     * Common scenarios for 501 errors:
     * <ul>
     *   <li>HTTP methods not yet implemented (e.g., TRACE, CONNECT)</li>
     *   <li>Features under development</li>
     *   <li>Unsupported protocol versions</li>
     * </ul>
     *
     * @param socket The client socket to which the error response will be sent.
     */
    public static void sendNotImplemented(Socket socket) {
        sendError(socket, 501, "Not Implemented");
    }

    /**
     * Handles an exception by mapping it to the appropriate HTTP error response.
     * <p>
     * This method provides centralized exception handling for HTTP request processing.
     * It examines the type of exception and dispatches to the appropriate error-sending
     * method. All exceptions are logged via {@link ServerLogger} for debugging and
     * monitoring purposes.
     * <p>
     * Exception-to-status-code mapping:
     * <ul>
     *   <li>{@link BadRequestException} → 400 Bad Request</li>
     *   <li>{@link NotFoundException} → 404 Not Found</li>
     *   <li>{@link NotImplementedException} → 501 Not Implemented</li>
     *   <li>{@link InternalServerErrorException} → 500 Internal Server Error</li>
     *   <li>All other exceptions → 500 Internal Server Error (fallback)</li>
     * </ul>
     * <p>
     * After sending the error response, the exception message is logged at ERROR level
     * to both server and client destinations for comprehensive error tracking.
     *
     * @param socket The client socket to which the error response will be sent.
     * @param e      The exception to handle. Must not be null.
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
        ServerLogger.getInstance().log(Loglevel.ERROR, "Exception handled: " + e.getMessage(), LogDestination.EVERYWHERE);
    }
}
