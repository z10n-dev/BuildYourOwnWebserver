package tcpframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTTPResponseHandler {

    private final String rootPath;

    public HTTPResponseHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    public void handleRequest(HTTPRequest request, Socket socket) throws IOException {

        switch (request.getMethod()){
            case GET:
                handleGetRequest(request, socket);
                break;
            case POST:
                handlePostRequest(request, socket);
                break;
            default:
                HTTPErrorHandler.sendNotImplemented(socket);
                break;
        }
    }

    private void handleGetRequest(HTTPRequest request, Socket socket) {
        String path = request.getPath();
        Path filePath = Paths.get(rootPath, path);

        if(Files.isDirectory(filePath)){
            filePath = filePath.resolve("index.html");
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            HTTPErrorHandler.sendNotFound(socket);
            return;
        }

        try {
            byte[] body = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            sendResponse(socket, mimeType, body);
        } catch (IOException e) {
            HTTPErrorHandler.sendInternalError(socket);
        }


    }

    private void handlePostRequest(HTTPRequest request, Socket socket) {
        HTTPErrorHandler.sendNotImplemented(socket);
    }

    private void sendResponse(Socket socket, String mimeType, byte[] body) {
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
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }

}
