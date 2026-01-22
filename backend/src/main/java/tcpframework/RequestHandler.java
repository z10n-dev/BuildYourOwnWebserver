package tcpframework;

import tcpframework.exceptions.NotImplementedException;

import java.net.Socket;

/**
 * Abstract base class for handling HTTP requests.
 * This class uses the Template Method design pattern to define the structure of request handling.
 * Subclasses can override specific HTTP method handlers (GET, POST, PUT, DELETE) as needed.
 */
public abstract class RequestHandler {

    /**
     * Handles an HTTP request by routing it to the appropriate method based on the HTTP method.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws Exception If an error occurs during request handling.
     */
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

    /**
     * Handles HTTP GET requests.
     * Subclasses should override this method to provide specific GET request handling logic.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected void handleGetRequest(HTTPRequest request, Socket socket) throws Exception{
        throw new NotImplementedException("GET method not implemented for this handler");
    };

    /**
     * Handles HTTP POST requests.
     * Subclasses should override this method to provide specific POST request handling logic.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected void handlePostRequest(HTTPRequest request, Socket socket) throws Exception{
        throw new NotImplementedException("POST method not implemented for this handler");
    };

    /**
     * Handles HTTP PUT requests.
     * Subclasses should override this method to provide specific PUT request handling logic.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected void handlePutRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("PUT method not implemented for this handler");
    };

    /**
     * Handles HTTP DELETE requests.
     * Subclasses should override this method to provide specific DELETE request handling logic.
     *
     * @param request The HTTP request to handle.
     * @param socket  The socket connection to the client.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected void handleDeleteRequest(HTTPRequest request, Socket socket) throws Exception {
        throw new NotImplementedException("DELETE method not implemented for this handler");
    };
}
