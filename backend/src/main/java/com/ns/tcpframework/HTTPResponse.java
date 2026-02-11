package com.ns.tcpframework;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Represents an HTTP response, encapsulating the status code, status message, headers, and body.
 * <p>
 * This class provides a structured representation of an HTTP response that can be sent to clients.
 * It manages all components of an HTTP response including the status line, headers, and message body,
 * and provides methods to construct and transmit the response according to HTTP/1.1 specifications.
 * <p>
 * Key features:
 * <ul>
 *   <li>Flexible header management with support for multi-value headers</li>
 *   <li>Automatic Content-Length and Content-Type header setting when body is provided</li>
 *   <li>UTF-8 encoding for text headers and arbitrary binary body support</li>
 *   <li>Track whether response has been sent to prevent duplicate transmission</li>
 *   <li>Thread-safe transmission to socket output streams</li>
 * </ul>
 * <p>
 * Response structure (HTTP/1.1 format):
 * <pre>
 * HTTP/1.1 {statusCode} {statusMessage}
 * {Header-Name}: {Header-Value}
 * ...
 *
 * {Body}
 * </pre>
 * <p>
 * Usage example:
 * <pre>
 * HTTPResponse response = new HTTPResponse(200, "OK");
 * response.setHeader("Content-Type", "application/json");
 * response.setBody("{\"status\":\"success\"}".getBytes(), "application/json");
 * response.send(clientSocket);
 * </pre>
 * <p>
 * Thread-safety: This class is not thread-safe. A single HTTPResponse instance should not be
 * modified concurrently by multiple threads. However, different threads can safely create and
 * send their own HTTPResponse instances.
 *
 * @see HTTPRequest
 * @see HTTPHandler
 */
public class HTTPResponse {
    /**
     * The HTTP status code indicating the result of the request.
     * <p>
     * Common status codes include:
     * <ul>
     *   <li>200 - OK (successful request)</li>
     *   <li>201 - Created (resource successfully created)</li>
     *   <li>204 - No Content (successful with no body)</li>
     *   <li>301 - Moved Permanently (redirect)</li>
     *   <li>302 - Found (temporary redirect)</li>
     *   <li>400 - Bad Request (client error)</li>
     *   <li>404 - Not Found (resource doesn't exist)</li>
     *   <li>500 - Internal Server Error (server error)</li>
     * </ul>
     */
    private int statusCode;

    /**
     * The HTTP status message providing a human-readable description of the status code.
     * <p>
     * This message corresponds to the status code and follows HTTP conventions.
     * Examples: "OK", "Not Found", "Internal Server Error"
     */
    private String statusMessage;

    /**
     * Map of HTTP response headers where keys are header names and values are arrays of header values.
     * <p>
     * Using arrays allows for headers that can have multiple values (e.g., Set-Cookie, Cache-Control).
     * Headers are stored with their original case but are typically case-insensitive in HTTP.
     */
    private final HashMap<String, String[]> headers = new HashMap<>();

    /**
     * The response body as a byte array.
     * <p>
     * This field holds the complete message body to be sent to the client. It can contain
     * any type of content (text, JSON, HTML, binary data, etc.). May be null if the response
     * has no body (e.g., 204 No Content, HEAD responses).
     */
    private byte[] body;

    /**
     * Flag indicating whether this response has been sent to a client.
     * <p>
     * This flag is used to track transmission status and prevent accidental duplicate sends.
     * It is set to true after {@link #send(Socket)} completes successfully.
     */
    private boolean sended = false;

    /**
     * Constructs an HTTPResponse object with the specified status code and status message.
     * <p>
     * This constructor initializes the response with the given status information.
     * Headers and body can be added after construction using {@link #setHeader(String, String)}
     * and {@link #setBody(byte[], String)} methods.
     * <p>
     * The status code and message should correspond to standard HTTP status codes and their
     * conventional messages, though any values are technically allowed.
     *
     * @param statusCode    The HTTP status code (e.g., 200, 404, 500). Should be a valid
     *                      HTTP status code in the range 100-599.
     * @param statusMessage The HTTP status message (e.g., "OK", "Not Found", "Internal Server Error").
     *                      Should be a brief, human-readable description. Must not be null.
     */
    public HTTPResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * Sets a header for the HTTP response.
     * <p>
     * If the header already exists, the new value is appended to the existing values,
     * allowing for multi-value headers (e.g., multiple Set-Cookie headers). If the header
     * does not exist, it is created with the provided value.
     * <p>
     * Header names are case-insensitive according to HTTP specification, but this implementation
     * preserves the case as provided. Common headers include:
     * <ul>
     *   <li>Content-Type - MIME type of the response body</li>
     *   <li>Content-Length - Size of the response body in bytes</li>
     *   <li>Cache-Control - Caching directives</li>
     *   <li>Set-Cookie - Set cookies in the client browser</li>
     *   <li>Location - Redirect target URL</li>
     * </ul>
     * <p>
     * Note: When using {@link #setBody(byte[], String)}, Content-Length and Content-Type
     * headers are set automatically.
     *
     * @param key   The name of the header (e.g., "Content-Type", "Cache-Control").
     *              Must not be null.
     * @param value The value of the header (e.g., "application/json", "no-cache").
     *              Must not be null.
     */
    public void setHeader(String key, String value) {
        if(headers.containsKey(key)){
            String[] values = headers.get(key);
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            headers.put(key, newValues);
        } else {
            headers.put(key, new String[]{value});
        }
    }

    /**
     * Sets the body of the HTTP response and automatically updates the Content-Length and Content-Type headers.
     * <p>
     * This method performs three operations:
     * <ol>
     *   <li>Stores the provided byte array as the response body</li>
     *   <li>Sets the Content-Length header to the exact size of the body</li>
     *   <li>Sets the Content-Type header to the specified MIME type</li>
     * </ol>
     * <p>
     * The Content-Length header is critical for HTTP/1.1 as it allows clients to know when
     * the response is complete. The Content-Type header informs clients how to interpret
     * the body content.
     * <p>
     * Common content types:
     * <ul>
     *   <li>text/html - HTML documents</li>
     *   <li>text/plain - Plain text</li>
     *   <li>application/json - JSON data</li>
     *   <li>application/xml - XML data</li>
     *   <li>image/jpeg, image/png - Image files</li>
     *   <li>application/octet-stream - Binary data</li>
     * </ul>
     *
     * @param body        The body of the response as a byte array. Must not be null.
     *                    Use an empty array for responses with no content.
     * @param contentType The MIME type of the body content (e.g., "text/html", "application/json").
     *                    Should include charset if applicable (e.g., "text/html; charset=utf-8").
     *                    Must not be null.
     */
    public void setBody(byte[] body, String contentType) {
        this.body = body;
        setHeader("Content-Length", String.valueOf(body.length));
        setHeader("Content-Type", contentType);
    }

    /**
     * Sends the HTTP response to the client through the provided socket.
     * <p>
     * This method constructs and transmits the complete HTTP response according to the HTTP/1.1
     * specification. It performs the following operations in sequence:
     * <ol>
     *   <li>Writes the status line: "HTTP/1.1 {statusCode} {statusMessage}\\r\\n"</li>
     *   <li>Writes all headers in the format: "{Header-Name}: {Header-Value}\\r\\n"</li>
     *   <li>Writes a blank line (\\r\\n) to separate headers from body</li>
     *   <li>Writes the body bytes if present</li>
     *   <li>Flushes all output to ensure delivery</li>
     *   <li>Sets the {@link #sended} flag to true</li>
     * </ol>
     * <p>
     * Headers are written using UTF-8 encoding, which is the standard for HTTP headers.
     * The body is written as raw bytes, preserving any encoding specified in the Content-Type header.
     * <p>
     * After calling this method, the response is considered sent and the {@link #isSended()}
     * method will return true. This can be used to prevent duplicate transmission.
     * <p>
     * Note: This method does not close the socket, allowing for persistent connections
     * (HTTP keep-alive). The socket should be closed by the caller when appropriate.
     *
     * @param socket The client socket to send the response to. Must not be null and should
     *               have an open output stream.
     * @throws Exception If an I/O error occurs while writing to the socket output stream.
     *                   This could indicate network issues, client disconnection, or socket closure.
     */
    public void send(Socket socket) throws Exception{
        OutputStream out = socket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(String.format("HTTP/1.1 %d %s\r\n", statusCode, statusMessage));
        for (String key : headers.keySet()) {
            String[] values = headers.get(key);
            for (String value : values) {
                writer.write(String.format("%s: %s\r\n", key, value));
            }
        }
        writer.write("\r\n");
        writer.flush();
        if (body != null && body.length > 0) {
            out.write(body);
            out.flush();
        }
        sended = true;
    }

    /**
     * Checks whether this response has been sent to a client.
     * <p>
     * This method returns the status of the {@link #sended} flag, which is set to true
     * after {@link #send(Socket)} completes successfully. This can be used to:
     * <ul>
     *   <li>Prevent accidental duplicate transmission of the same response</li>
     *   <li>Determine if a handler has already sent the response directly to the socket</li>
     *   <li>Track response lifecycle for logging or debugging purposes</li>
     * </ul>
     * <p>
     * Some handlers (e.g., SSE handlers) may send responses directly to the socket and
     * set this flag to indicate that the framework should not attempt to send the response again.
     *
     * @return {@code true} if the response has been sent, {@code false} otherwise.
     */
    public boolean isSended() {
        return sended;
    }

}
