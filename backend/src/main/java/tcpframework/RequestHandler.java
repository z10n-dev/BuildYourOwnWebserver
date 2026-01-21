package tcpframework;

import tcpframework.exceptions.InternalServerErrorException;
import tcpframework.exceptions.NotImplementedException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public abstract class RequestHandler {
    public void handle(HTTPRequest request, Socket socket) throws Exception {
        switch (request.getMethod()){
            case GET:
                handleGetRequest(request, socket);
                break;
            case POST:
                handlePostRequest(request, socket);
                break;
            case PUT:
                handlePutRequest(request, socket);
                break;
            case DELETE:
                handleDeleteRequest(request, socket);
                break;
            default:
                HTTPErrorHandler.sendNotImplemented(socket);
                break;
        }
    }

    protected void handleGetRequest(HTTPRequest request, Socket socket) throws Exception{
        throw new NotImplementedException("GET method not implemented for this handler");
    };

    protected void handlePostRequest(HTTPRequest request, Socket socket) throws Exception{
        throw new NotImplementedException("POST method not implemented for this handler");
    };

    protected void handlePutRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("PUT method not implemented for this handler");
    };

    protected void handleDeleteRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("DELETE method not implemented for this handler");
    };



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
