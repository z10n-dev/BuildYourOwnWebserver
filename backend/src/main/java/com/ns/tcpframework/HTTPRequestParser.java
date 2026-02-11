package com.ns.tcpframework;

import com.ns.tcpframework.exceptions.BadRequestException;
import com.ns.tcpframework.exceptions.NotImplementedException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/**
 * Utility class for parsing raw HTTP requests from socket input streams.
 * <p>
 * This parser implements HTTP/1.1 request parsing according to RFC 7230 and RFC 7231.
 * It reads raw bytes from a socket, separates headers from the body, and constructs
 * a structured {@link HTTPRequest} object containing all request components.
 * <p>
 * The parser handles:
 * <ul>
 *   <li>Request line parsing (method, path, HTTP version)</li>
 *   <li>Header parsing with support for multi-value headers</li>
 *   <li>Body stream creation with Content-Length based limitation</li>
 *   <li>Host header extraction for virtual host resolution</li>
 *   <li>Header name normalization (conversion to lowercase)</li>
 *   <li>Multi-value header merging (comma-separated values)</li>
 * </ul>
 * <p>
 * Parsing process:
 * <ol>
 *   <li>Read HTTP headers until the double CRLF (\\r\\n\\r\\n) sequence is encountered</li>
 *   <li>Parse the request line to extract method, path, and HTTP version</li>
 *   <li>Parse individual header lines into a map structure</li>
 *   <li>Create a body stream limited by the Content-Length header</li>
 *   <li>Construct and return the complete HTTPRequest object</li>
 * </ol>
 * <p>
 * Error handling: The parser throws specific exceptions for various error conditions:
 * <ul>
 *   <li>{@link BadRequestException} - Malformed requests, missing required components</li>
 *   <li>{@link NotImplementedException} - Unsupported HTTP methods</li>
 *   <li>{@link IOException} - Network I/O errors</li>
 * </ul>
 * <p>
 * Thread-safety: This class is stateless with only static methods, making it inherently
 * thread-safe for concurrent use across multiple request processing threads.
 *
 * @see HTTPRequest
 * @see HTTPMethode
 * @see FixedLengthInputStream
 */
public class HTTPRequestParser {

    /**
     * Parses a complete HTTP request from the provided socket connection.
     * <p>
     * This is the main entry point for HTTP request parsing. It orchestrates the complete
     * parsing process by reading the raw request from the socket, extracting all components,
     * and constructing a fully populated {@link HTTPRequest} object.
     * <p>
     * The method performs the following operations in sequence:
     * <ol>
     *   <li>Obtains the raw input stream from the socket</li>
     *   <li>Reads and extracts the HTTP header section (up to \\r\\n\\r\\n)</li>
     *   <li>Parses the request line (method, path, version)</li>
     *   <li>Parses all HTTP headers into a map structure</li>
     *   <li>Creates a body stream limited by Content-Length (0 if not specified)</li>
     *   <li>Extracts the Host header for virtual host resolution</li>
     *   <li>Constructs and returns the HTTPRequest object</li>
     * </ol>
     * <p>
     * Host header handling: If the Host header is missing, "404" is used as a placeholder
     * value, which typically results in a 404 response during virtual host resolution.
     *
     * @param socket The socket connection from which to read the HTTP request.
     *               Must not be null and should have an open input stream.
     * @return A fully constructed {@link HTTPRequest} object containing all parsed components.
     * @throws BadRequestException If the request is empty, malformed, or missing required components.
     * @throws NotImplementedException If the HTTP method is not supported by the server.
     * @throws IOException If an I/O error occurs while reading from the socket.
     * @throws Exception If any other error occurs during parsing (e.g., number format for Content-Length).
     */
    public static HTTPRequest parseHTTPRequest(Socket socket) throws Exception {
        InputStream rawIn = socket.getInputStream();
        String header = getHTTPHeader(rawIn);
        BufferedReader in = new BufferedReader(new StringReader(header));
        String line = in.readLine();
        if (line == null || line.isEmpty()) {
            throw new BadRequestException("Empty HTTP Request");
        }
        String[] requestLineParts = line.split(" ");

        HTTPMethode methode = extractMethode(requestLineParts);
        String path = extractPath(requestLineParts);
        String httpVersion = extractHttpVersion(requestLineParts);

        LinkedHashMap<String, String[]> headers = extractHttpHeaders(in);

        return new HTTPRequest(methode, path, httpVersion, headers, bodyStream(rawIn, Integer.parseInt(headers.getOrDefault("content-length", new String[]{"0"})[0])), headers.containsKey("host") ? headers.get("host")[0] : "404");
    }

    /**
     * Reads the HTTP header section from the input stream up to the double CRLF sequence.
     * <p>
     * This method implements a state machine to detect the end of HTTP headers, which is
     * indicated by the sequence \\r\\n\\r\\n (CRLF CRLF). It reads bytes one at a time,
     * accumulating them into a buffer until this terminating sequence is found.
     * <p>
     * State machine transitions:
     * <ul>
     *   <li>State 0: Initial state, waiting for \\r</li>
     *   <li>State 1: Found \\r, expecting \\n</li>
     *   <li>State 2: Found \\r\\n, expecting another \\r</li>
     *   <li>State 3: Found \\r\\n\\r, expecting final \\n</li>
     *   <li>Complete: Found \\r\\n\\r\\n, headers are complete</li>
     * </ul>
     * <p>
     * The method continues reading until either the double CRLF is found or the stream
     * ends (returns -1). The accumulated bytes are converted to a UTF-8 string representing
     * the complete HTTP header section including the request line.
     *
     * @param in The input stream from which to read the HTTP headers. Must not be null.
     * @return A UTF-8 string containing the complete HTTP header section including the
     *         request line and all headers, up to and including the final \\r\\n\\r\\n.
     * @throws IOException If an I/O error occurs while reading from the input stream.
     */
    private static String getHTTPHeader(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        int lastState = 0;

        while ((b = in.read()) != -1) {
            buffer.write(b);

            if (lastState == 0 && b == '\r') {
                lastState = 1;
            } else if (lastState == 1 && b == '\n') {
                lastState = 2;
            } else if (lastState == 2 && b == '\r') {
                lastState = 3;
            } else if (lastState == 3 && b == '\n') {
                break;
            } else {
                lastState = 0;
            }
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Creates a body input stream limited to the specified content length.
     * <p>
     * This method wraps the raw input stream in a {@link FixedLengthInputStream} that
     * restricts reading to exactly the number of bytes specified by the Content-Length
     * header. This prevents reading beyond the current request's body into subsequent
     * requests on persistent connections.
     * <p>
     * For requests without a body (e.g., GET, HEAD), the content length should be 0,
     * resulting in a stream that immediately returns EOF.
     *
     * @param in The raw input stream from the socket. Must not be null.
     * @param contentLength The number of bytes to allow reading from the stream, typically
     *                      obtained from the Content-Length header. Should be non-negative.
     * @return A {@link FixedLengthInputStream} that limits reading to the specified number of bytes.
     */
    private static InputStream bodyStream(InputStream in, int contentLength) {
        return new FixedLengthInputStream(in, contentLength);
    }

    /**
     * Extracts and validates the HTTP method from the request line.
     * <p>
     * This method parses the first component of the request line (the HTTP method) and
     * performs the following validations:
     * <ol>
     *   <li>Verifies the method field is present and not empty</li>
     *   <li>Verifies the method is uppercase (per HTTP specification)</li>
     *   <li>Verifies the method is a recognized HTTP method defined in {@link HTTPMethode}</li>
     * </ol>
     * <p>
     * HTTP methods are case-sensitive and must be uppercase according to RFC 7231.
     * Recognized methods are defined in the {@link HTTPMethode} enum.
     *
     * @param line The tokenized request line, where the first element should be the HTTP method.
     * @return The parsed {@link HTTPMethode} enum value.
     * @throws BadRequestException If the method is empty or not uppercase.
     * @throws NotImplementedException If the method is not recognized (not in {@link HTTPMethode} enum).
     * @throws Exception If any other error occurs during method extraction.
     */
    private static HTTPMethode extractMethode(String[] line) throws Exception {
        HTTPMethode methode;

        if (line.length == 0 || line[0].isEmpty()) {
            throw new BadRequestException("Empty HTTP Methode");
        } else if (!line[0].equals(line[0].toUpperCase())) {
            throw new BadRequestException("Invalid HTTP Methode: " + line[0]);
        }
        try {
            methode = HTTPMethode.valueOf(line[0]);
        } catch (Exception e) {
            throw new NotImplementedException("Unsupported HTTP Methode: " + line[0]);
        }

        return methode;
    }

    /**
     * Extracts and validates the request path from the request line.
     * <p>
     * This method parses the second component of the request line (the request path/URI)
     * and verifies it is present and not empty. The path may include:
     * <ul>
     *   <li>The resource path (e.g., "/api/users")</li>
     *   <li>Query string parameters (e.g., "/search?q=hello")</li>
     *   <li>Fragment identifiers (though typically not sent by clients)</li>
     * </ul>
     * <p>
     * The path is returned as-is without any URL decoding or normalization, allowing
     * handlers to process it according to their specific requirements.
     *
     * @param line The tokenized request line, where the second element should be the request path.
     * @return The request path string, which may include query parameters.
     * @throws BadRequestException If the path is missing or empty.
     * @throws Exception If any other error occurs during path extraction.
     */
    private static String extractPath(String[] line) throws Exception {
        String path;

        if (line.length <= 1 || line[1].isEmpty()) {
            throw new BadRequestException("Empty HTTP Path");
        }

        path = line[1];

        return path;
    }

    /**
     * Extracts and validates the HTTP version from the request line.
     * <p>
     * This method parses the third component of the request line (the HTTP version)
     * and verifies it is present and not empty. Common HTTP versions include:
     * <ul>
     *   <li>HTTP/1.0 - Original HTTP version</li>
     *   <li>HTTP/1.1 - Most common version with persistent connections</li>
     *   <li>HTTP/2.0 - Binary protocol with multiplexing</li>
     * </ul>
     * <p>
     * The version string is returned as-is without validation of the specific version,
     * allowing the server to handle version compatibility at a higher level if needed.
     *
     * @param line The tokenized request line, where the third element should be the HTTP version.
     * @return The HTTP version string (e.g., "HTTP/1.1").
     * @throws BadRequestException If the version is missing or empty.
     * @throws Exception If any other error occurs during version extraction.
     */
    private static String extractHttpVersion(String[] line) throws Exception {
        String httpVersion;

        if (line.length <= 2 || line[2].isEmpty()) {
            throw new BadRequestException("Empty HTTP Version");
        }

        httpVersion = line[2];

        return httpVersion;
    }

    /**
     * Extracts and parses all HTTP headers from the header section.
     * <p>
     * This method processes each header line and builds a map of header names to their values.
     * It handles several important aspects of HTTP header processing:
     * <p>
     * Header processing features:
     * <ul>
     *   <li><b>Name normalization</b>: Header names are converted to lowercase for case-insensitive lookups</li>
     *   <li><b>Multi-value support</b>: Headers with comma-separated values are split into arrays</li>
     *   <li><b>Duplicate header merging</b>: Multiple occurrences of the same header are merged into a single array</li>
     *   <li><b>Empty value handling</b>: Headers without values are stored with an empty array</li>
     * </ul>
     * <p>
     * Header format: Each header line follows the format "Name: Value", where the colon and
     * space separate the name from the value. Multiple values can be comma-separated on a
     * single line or the same header can appear multiple times.
     * <p>
     * Examples:
     * <pre>
     * Accept: text/html, application/json     → ["text/html", "application/json"]
     * Cookie: session=abc                      → ["session=abc"]
     * Cookie: user=john                        → Merged with previous: ["session=abc", "user=john"]
     * Host:                                    → []
     * </pre>
     *
     * @param in A BufferedReader positioned at the first header line (after the request line).
     *           Must not be null.
     * @return A LinkedHashMap where keys are lowercase header names and values are arrays
     *         of header values. Returns an empty map if no headers are present.
     * @throws BadRequestException If a header line is malformed (missing header name).
     * @throws IOException If an I/O error occurs while reading header lines.
     * @throws Exception If any other error occurs during header parsing.
     */
    private static LinkedHashMap<String, String[]> extractHttpHeaders(BufferedReader in) throws Exception {
        LinkedHashMap<String, String[]> headers = new LinkedHashMap<>();

        String line = in.readLine();
        while (line != null && !line.isEmpty()) {
            String[] parts = line.split(": ");

            if (parts.length == 0 || parts[0].isEmpty()) {
                throw new BadRequestException("Invalid HTTP Header: " + line);
            }

            String key = parts[0].toLowerCase();
            String[] values;

            if (parts.length > 1 && !parts[1].isEmpty()) {
                String value = parts[1];
                values = value.split(", ");

                if (headers.containsKey(key)) {
                    String[] existingValues = headers.get(key);
                    String[] newValues = new String[existingValues.length + values.length];
                    System.arraycopy(existingValues, 0, newValues, 0, existingValues.length);
                    System.arraycopy(values, 0, newValues, existingValues.length, values.length);
                    values = newValues;
                }
            } else {
                values = new String[]{};
            }

            headers.put(key, values);

            line = in.readLine();
        }

        return headers;
    }
}
