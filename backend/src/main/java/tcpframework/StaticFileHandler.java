package tcpframework;

import tcpframework.exceptions.InternalServerErrorException;
import tcpframework.exceptions.NotFoundException;

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

    public void handle(HTTPRequest request, Socket socket) throws Exception {

        switch (request.getMethod()){
            case GET:
                handleGetRequest(request, socket);
                break;
            case POST:
                handlePostRequest(request, socket);
                break;
            case PUT:
//                handlePutRequest(request, socket);
                break;
            case DELETE:
//                handleDeleteRequest(request, socket);
                break;
            default:
                HTTPErrorHandler.sendNotImplemented(socket);
                break;
        }
    }

    private void handleGetRequest(HTTPRequest request, Socket socket) throws Exception {
        String path = request.getPath();
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }
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
            sendResponse(socket, mimeType, body);
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read file: " + filePath);
        }


    }

    private void handlePostRequest(HTTPRequest request, Socket socket) {
        HTTPErrorHandler.sendNotImplemented(socket);
    }
}
