package tcpframework;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Map;

/**
 * Represents an HTTP request, encapsulating the HTTP method, path, version, headers, and body.
 */
public class HTTPRequest {
    private final HTTPMethode method;
    private final String path;
    private final String httpVersion;
    private final Map<String, String[]> headers;
    private final byte[] bodyBytes;
    private final String host;

    /**
     * Constructs an HTTPRequest object with the specified parameters.
     *
     * @param method      The HTTP method (e.g., GET, POST).
     * @param path        The request path, including the query string if present.
     * @param httpVersion The HTTP version (e.g., HTTP/1.1).
     * @param headers     A map of HTTP headers, where the key is the header name and the value is an array of header values.
     * @param body        The InputStream representing the body of the request.
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
     *
     * @param body The InputStream representing the body of the request.
     * @return A byte array containing the body data, or an empty array if the body is null.
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
     * @return The HTTP method.
     */
    public HTTPMethode getMethod() {
        return method;
    }

    /**
     * Gets the path of the request, excluding the query string if present.
     *
     * @return The path of the request.
     */
    public String getPath() {
        int queryIndex = path.indexOf('?');
        return (queryIndex != -1) ? path.substring(0, queryIndex) : path;
    }

    /**
     * Gets the body of the request as a string.
     *
     * @return The body of the request.
     */
    public String getBody() {
        return new String(bodyBytes);
    }

    /**
     * Gets the request head string, which includes the HTTP method and path.
     *
     * @return The request head string.
     */
    public String getRequestHead(){
        return method + " " + path;
    }


    public String getHost() {
        int colonIndex = host.indexOf(':');
        if (colonIndex != -1){
            return host.substring(0, colonIndex);
        }
        return host;
    }

    /*
     * Returns a string representation of the HTTP request, including the method, path, version, headers, and body.
     *
     * @return A string representation of the HTTP request.
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
