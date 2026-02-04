package com.ns.tcpframework;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Represents an HTTP response, encapsulating the status code, status message, headers, and body.
 */
public class HTTPResponse {
    private int statusCode;
    private String statusMessage;
    private final HashMap<String, String[]> headers = new HashMap<>();
    private byte[] body;
    private boolean sended = false;

    /**
     * Constructs an HTTPResponse object with the specified status code and status message.
     *
     * @param statusCode    The HTTP status code (e.g., 200, 404).
     * @param statusMessage The HTTP status message (e.g., "OK", "Not Found").
     */
    public HTTPResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * Sets a header for the HTTP response. If the header already exists, the new value is appended.
     *
     * @param key   The name of the header.
     * @param value The value of the header.
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
     * Sets the body of the HTTP response and updates the Content-Length and Content-Type headers.
     *
     * @param body        The body of the response as a byte array.
     * @param contentType The MIME type of the body content (e.g., "text/html", "application/json").
     */
    public void setBody(byte[] body, String contentType) {
        this.body = body;
        setHeader("Content-Length", String.valueOf(body.length));
        setHeader("Content-Type", contentType);
    }

    /**
     * Sends the HTTP response to the client through the provided socket.
     *
     * @param socket The client socket to send the response to.
     * @throws Exception If an error occurs while sending the response.
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

    public boolean isSended() {
        return sended;
    }


}
