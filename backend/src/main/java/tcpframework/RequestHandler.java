package tcpframework;

import tcpframework.exceptions.InternalServerErrorException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public abstract class RequestHandler {
    public abstract void handle(HTTPRequest request, Socket socket) throws Exception;

    protected void sendResponse(Socket socket, String mimeType, byte[] body) throws InternalServerErrorException {
        try {
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write(String.format("HTTP/1.1 %d %s\r\n", 200, "OK"));
            writer.write(String.format("Content-Type: %s; charset=utf-8\r\n", mimeType));
            writer.write(String.format("Content-Length: %d\r\n", body != null ? body.length : 0));
            writer.write("Connection: close\r\n\r\n");
            writer.flush();
            if (body != null && body.length > 0) {
                out.write(body);
                out.flush();
            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to send response" );
        }
    }
}
