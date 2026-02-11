package com.ns.tcpframework.reqeustHandlers;

import com.ns.tcpframework.HTTPRequest;
import com.ns.tcpframework.HTTPResponse;

import java.net.Socket;

/**
 * Abstract base class for handling HTTP requests.
 * <p>
 * This class provides a framework for implementing custom request handlers that process
 * HTTP requests and generate appropriate responses. Subclasses must implement the
 * {@link #handle(HTTPRequest)} method to define their specific request handling logic.
 * <p>
 * The class provides two variants of the handle method:
 * <ul>
 *   <li>{@link #handle(HTTPRequest)} - Standard request handling without socket access</li>
 *   <li>{@link #handle(HTTPRequest, Socket)} - Extended handling with direct socket access
 *       for cases requiring persistent connections (e.g., WebSockets, SSE)</li>
 * </ul>
 * <p>
 * By default, the socket-aware method delegates to the standard method. Handlers that
 * require socket access (such as streaming or bidirectional communication) should
 * override the socket-aware variant.
 *
 * @see HTTPRequest
 * @see HTTPResponse
 */
public abstract class RequestHandler {

    /**
     * Processes an HTTP request and generates a response.
     * <p>
     * This is the primary method that subclasses must implement to define their
     * request handling behavior. Implementations should:
     * <ul>
     *   <li>Parse and validate the request</li>
     *   <li>Execute the necessary business logic</li>
     *   <li>Construct and return an appropriate HTTPResponse</li>
     * </ul>
     *
     * @param request The HTTP request to process.
     * @return An HTTPResponse object containing the response to be sent to the client.
     * @throws Exception if an error occurs during request processing. The exception
     *                   will be handled by the calling framework.
     */
    public abstract HTTPResponse handle(HTTPRequest request) throws Exception;

    /**
     * Processes an HTTP request with access to the underlying socket connection.
     * <p>
     * This method provides an extension point for handlers that need direct access
     * to the client socket, such as:
     * <ul>
     *   <li>Server-Sent Events (SSE) requiring persistent connections</li>
     *   <li>WebSocket upgrades</li>
     *   <li>Custom streaming protocols</li>
     *   <li>Long-polling implementations</li>
     * </ul>
     * <p>
     * The default implementation delegates to {@link #handle(HTTPRequest)}, making
     * socket access optional. Subclasses requiring socket access should override this
     * method instead of (or in addition to) the standard handle method.
     *
     * @param request The HTTP request to process.
     * @param socket  The client socket connection, providing direct I/O access.
     * @return An HTTPResponse object containing the response to be sent to the client.
     * @throws Exception if an error occurs during request processing. The exception
     *                   will be handled by the calling framework.
     */
    public HTTPResponse handle(HTTPRequest request, Socket socket) throws Exception {
        return handle(request);
    }
}