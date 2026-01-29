package tcpframework.reqeustHandlers;

import tcpframework.HTTPRequest;
import tcpframework.HTTPResponse;
import tcpframework.exceptions.NotImplementedException;

/**
 * Abstract base class for handling HTTP requests.
 * This class uses the Template Method design pattern to define the structure of request handling.
 * Subclasses can override specific HTTP method handlers (GET, POST, PUT, DELETE) as needed.
 */
public class MethodeBasedHandler extends RequestHandler {

    /**
     * Handles an HTTP request by routing it to the appropriate method based on the HTTP method.
     *
     * @param request The HTTP request to handle.
     * @throws Exception If an error occurs during request handling.
     */
    public HTTPResponse handle(HTTPRequest request) throws Exception {

        return switch (request.getMethod()) {
            case GET -> handleGetRequest(request);
            case POST -> handlePostRequest(request);
            case PUT -> handlePutRequest(request);
            case HEAD -> handleHeadRequest(request);
            case DELETE -> handleDeleteRequest(request);
            default -> throw new NotImplementedException("HTTP Method not supported: " + request.getMethod());
        };

    }

    /**
     * Handles HTTP GET requests.
     * Subclasses should override this method to provide specific GET request handling logic.
     *
     * @param request The HTTP request to handle.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected HTTPResponse handleGetRequest(HTTPRequest request) throws Exception{
        throw new NotImplementedException("GET method not implemented for this handler");
    };

    /**
     * Handles HTTP POST requests.
     * Subclasses should override this method to provide specific POST request handling logic.
     *
     * @param request The HTTP request to handle.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected HTTPResponse handlePostRequest(HTTPRequest request) throws Exception{
        throw new NotImplementedException("POST method not implemented for this handler");
    };

    /**
     * Handles HTTP PUT requests.
     * Subclasses should override this method to provide specific PUT request handling logic.
     *
     * @param request The HTTP request to handle.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected HTTPResponse handlePutRequest(HTTPRequest request) throws Exception {
        throw new NotImplementedException("PUT method not implemented for this handler");
    };

    /**
     * Handles HTTP DELETE requests.
     * Subclasses should override this method to provide specific DELETE request handling logic.
     *
     * @param request The HTTP request to handle.
     * @throws Exception If the method is not implemented or an error occurs.
     */
    protected HTTPResponse handleDeleteRequest(HTTPRequest request) throws Exception {
        throw new NotImplementedException("DELETE method not implemented for this handler");
    };

    protected HTTPResponse handleHeadRequest(HTTPRequest request) throws Exception {
        throw new NotImplementedException("HEAD method not implemented for this handler");
    }
}
