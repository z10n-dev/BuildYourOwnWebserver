package tcpframework;

import tcpframework.exceptions.InternalServerErrorException;
import tcpframework.exceptions.NotFoundException;
import tcpframework.exceptions.NotImplementedException;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler extends RequestHandler {

    private final String rootPath;

    public StaticFileHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    protected void handleGetRequest(HTTPRequest request, Socket socket) throws Exception {
        String path = request.getPath();
        Path filePath = Paths.get(rootPath, path);

        if(Files.isDirectory(filePath)){
            filePath = filePath.resolve("index.html");
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new NotFoundException("File not found: " + filePath);
        }

        try {
            byte[] body = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);

            HTTPResponse response = new HTTPResponse(200, "OK");
            response.setBody(body, mimeType != null ? mimeType : "application/octet-stream");
            response.send(socket);

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read file: " + filePath);
        }


    }

    @Override
    protected void handlePostRequest(HTTPRequest request, Socket socket) throws Exception{
        throw new NotImplementedException("POST method not implemented for StaticFileHandler");
    }

    @Override
    protected void handlePutRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("PUT method not implemented for StaticFileHandler");
    }

    @Override
    protected void handleDeleteRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("DELETE method not implemented for StaticFileHandler");
    }
}
