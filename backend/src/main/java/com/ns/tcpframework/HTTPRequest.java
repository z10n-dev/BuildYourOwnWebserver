package com.ns.tcpframework;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Represents an HTTP request, encapsulating the HTTP method, path, version, headers, and body.
 * <p>
 * This class provides an immutable representation of an HTTP request received by the server.
 * It parses and stores all components of the HTTP request including the request line
 * (method, path, version), headers, and message body.
 * </p>
 * <p>
 * The request body is eagerly read from the input stream during construction and stored
 * as a byte array, making it available for multiple reads without stream concerns.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Immutable design - all fields are final and cannot be modified after construction</li>
 *   <li>Query string parsing - provides path without query parameters via {@link #getPath()}</li>
 *   <li>Host header extraction - handles both hostname and hostname:port formats</li>
 *   <li>Body caching - reads body once and caches for repeated access</li>
 *   <li>Header storage - maintains all headers with support for multi-value headers</li>
 * </ul>
 * <p>
 * Thread-safety: This class is immutable and therefore thread-safe. Multiple threads can
 * safely read from the same HTTPRequest instance concurrently.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * HTTPRequest request = new HTTPRequest(
 *     HTTPMethode.GET,
 *     "/api/users?limit=10",
 *     "HTTP/1.1",
 *     headers,
 *     bodyStream,
 *     "example.com:8080"
 * );
 * String path = request.getPath(); // Returns "/api/users" (without query string)
 * String host = request.getHost(); // Returns "example.com" (without port)
 * </pre>
 *
 * @see HTTPMethode
 * @see HTTPRequestParser
 * @see HTTPResponse
 */
public class HTTPRequest {
    /**
     * The HTTP method of the request (e.g., GET, POST, PUT, DELETE).
     */
    private final HTTPMethode method;

    /**
     * The full request path including query string if present.
     * <p>
     * Example: "/api/users?limit=10&page=2"
     * </p>
     */
    private final String path;

    /**
     * The HTTP protocol version used in the request.
     * <p>
     * Typical values: "HTTP/1.0", "HTTP/1.1", "HTTP/2.0"
     * </p>
     */
    private final String httpVersion;

    /**
     * Map of HTTP headers where keys are header names and values are arrays of header values.
     * <p>
     * Header names are typically case-insensitive according to HTTP specification.
     * Array values support headers that can appear multiple times (e.g., Cookie, Accept).
     * </p>
     */
    private final Map<String, String[]> headers;

    /**
     * The request body as a byte array.
     * <p>
     * This field contains the complete request body read from the input stream.
     * For requests without a body (e.g., GET, HEAD), this will be an empty array.
     * </p>
     */
    private final byte[] bodyBytes;

    /**
     * The Host header value, potentially including port number.
     * <p>
     * Format: "hostname" or "hostname:port"
     * Example: "example.com" or "example.com:8080"
     * </p>
     */
    private final String host;

    /**
     * Constructs an HTTPRequest object with the specified parameters.
     * <p>
     * This constructor eagerly reads the entire request body from the provided InputStream
     * and stores it as a byte array. This ensures the body is available for multiple reads
     * and that the input stream can be closed immediately after construction.
     * </p>
     * <p>
     * The constructor performs the following operations:
     * <ol>
     *   <li>Stores all request line components (method, path, version)</li>
     *   <li>Stores the headers map (reference, not copied)</li>
     *   <li>Reads the complete body from the input stream via {@link #readBody(InputStream)}</li>
     *   <li>Stores the host header value for virtual host resolution</li>
     * </ol>
     *
     * @param method      The HTTP method (e.g., GET, POST, PUT, DELETE). Must not be null.
     * @param path        The request path, including the query string if present. Must not be null.
     * @param httpVersion The HTTP version (e.g., "HTTP/1.1"). Must not be null.
     * @param headers     A map of HTTP headers, where the key is the header name and the value
     *                    is an array of header values. Must not be null, but may be empty.
     * @param body        The InputStream representing the body of the request. May be null for
     *                    requests without a body.
     * @param host        The Host header value, potentially including port (e.g., "example.com:8080").
     *                    Must not be null.
     * @throws UncheckedIOException if an IOException occurs while reading the request body.
     */
    public HTTPRequest(HTTPMethode method, String path, String httpVersion, Map<String, String[]> headers, InputStream body, String host) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.bodyBytes = readBody(body);
        this.host = host;
    }

    /**
     * Reads the body of the request from the provided InputStream.
     * <p>
     * This method reads all available bytes from the input stream and returns them as
     * a byte array. If the input stream is null (indicating no body), an empty byte
     * array is returned.
     * </p>
     * <p>
     * This is a helper method used internally by the constructor to eagerly load the
     * request body into memory, allowing the stream to be closed immediately after
     * the request object is created.
     * </p>
     *
     * @param body The InputStream representing the body of the request. May be null.
     * @return A byte array containing the body data, or an empty array if the body is null.
     * @throws UncheckedIOException if an IOException occurs while reading from the stream.
     *                              This is a runtime exception wrapper around IOException.
     */
    private byte[] readBody(InputStream body) {
        try {
            return (body != null) ? body.readAllBytes() : new byte[0];
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the HTTP method of the request.
     *
     * @return The HTTP method (e.g., GET, POST, PUT, DELETE). Never null.
     */
    public HTTPMethode getMethod() {
        return method;
    }

    /**
     * Gets the path of the request, excluding the query string if present.
     * <p>
     * This method extracts the path portion of the request URI, removing any query
     * string parameters. The query string is identified by the first occurrence of
     * the '?' character.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>"/api/users?limit=10" returns "/api/users"</li>
     *   <li>"/api/users" returns "/api/users"</li>
     *   <li>"/search?q=hello&amp;page=2" returns "/search"</li>
     * </ul>
     *
     * @return The path of the request without query parameters. Never null.
     */
    public String getPath() {
        int queryIndex = path.indexOf('?');
        return (queryIndex != -1) ? path.substring(0, queryIndex) : path;
    }

    /**
     * Gets the body of the request as a string using the default character encoding.
     * <p>
     * This method converts the stored byte array body to a String using the platform's
     * default character encoding. For requests without a body, this returns an empty string.
     * </p>
     * <p>
     * Note: For better control over character encoding, consider using the Content-Type
     * header's charset parameter to determine the appropriate encoding, especially for
     * non-ASCII text.
     * </p>
     *
     * @return The body of the request as a String. Never null, but may be empty.
     */
    public String getBody() {
        return new String(bodyBytes);
    }

    /**
     * Gets the request head string, which includes the HTTP method and full path.
     * <p>
     * This method returns a string representation of the request line's method and path
     * components, formatted as "{METHOD} {PATH}". The path includes any query string
     * if present.
     * </p>
     * <p>
     * This is commonly used for logging and debugging purposes to get a concise
     * representation of what resource is being requested and how.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>"GET /api/users"</li>
     *   <li>"POST /api/login"</li>
     *   <li>"GET /search?q=hello"</li>
     * </ul>
     *
     * @return The request head string in the format "{METHOD} {PATH}". Never null.
     */
    public String getRequestHead(){
        return method + " " + path;
    }

    /**
     * Gets the hostname from the Host header, excluding the port number if present.
     * <p>
     * This method extracts the hostname portion from the Host header value, removing
     * any port number specification. The port is identified by the first colon (':')
     * character in the host string.
     * </p>
     * <p>
     * This is particularly useful for virtual host resolution, where the hostname alone
     * is needed to select the appropriate virtual host configuration, regardless of
     * the port on which the request was received.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>"example.com:8080" → "example.com"</li>
     *   <li>"example.com" → "example.com"</li>
     *   <li>"localhost:3000" → "localhost"</li>
     *   <li>"192.168.1.1:8080" → "192.168.1.1"</li>
     * </ul>
     *
     * @return The hostname without the port number. Never null.
     */
    public String getHost() {
        int colonIndex = host.indexOf(':');
        if (colonIndex != -1){
            return host.substring(0, colonIndex);
        }
        return host;
    }

    /**
     * Returns a string representation of the HTTP request, including the method, path, version, headers, and body.
     * <p>
     * This method constructs a detailed string representation of the complete HTTP request
     * in a format similar to the actual HTTP protocol format. This is useful for debugging,
     * logging, and understanding the exact contents of a request.
     * </p>
     * <p>
     * The format follows the HTTP message structure:
     * <pre>
     * {METHOD} {PATH} {VERSION}
     * {Header-Name}: {Header-Value(s)}
     * ...
     *
     * {Body}
     * </pre>
     * <p>
     * For headers with multiple values, the values are joined with ", " (comma-space).
     * <p>
     * Example output:
     * <pre>
     * GET /api/users HTTP/1.1
     * Host: example.com
     * Accept: application/json
     * Authorization: Bearer token123
     *
     * </pre>
     *
     * @return A string representation of the HTTP request. Never null.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" ").append(httpVersion).append("\n");
        headers.forEach((key, values) -> sb.append(key).append(": ").append(String.join(", ", values)).append("\n"));
        sb.append("\n").append(new String(bodyBytes));
        return sb.toString();
    }
}
