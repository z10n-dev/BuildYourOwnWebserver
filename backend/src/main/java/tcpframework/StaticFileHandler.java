package tcpframework;

import tcpframework.exceptions.InternalServerErrorException;
import tcpframework.exceptions.NotFoundException;
import tcpframework.exceptions.NotImplementedException;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A request handler for serving static files from a specified root directory.
 * This class extends the RequestHandler and provides an implementation for handling
 * HTTP GET requests to serve files. Other HTTP methods are not supported.
 */
public class StaticFileHandler extends RequestHandler {

    private final String rootPath;

    /**
     * Constructs a StaticFileHandler with the specified root directory.
     *
     * @param rootPath The root directory from which files will be served.
     */
    public StaticFileHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Handles HTTP GET requests by serving static files from the root directory.
     * If the requested path is a directory, it attempts to serve an "index.html" file.
     * If the file does not exist or is a directory, a NotFoundException is thrown.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws NotFoundException If the file is not found or is a directory.
     * @throws InternalServerErrorException If an error occurs while reading the file.
     * @throws Exception If any other error occurs during request handling.
     */
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
}
