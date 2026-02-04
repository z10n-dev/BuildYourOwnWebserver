package com.ns.tcpframework;

/**
 * Functional interface representing a command to handle a specific route.
 * This interface is designed to encapsulate the logic for processing an HTTP request
 * on a given route, using a socket connection.
 */
@FunctionalInterface
public interface RouteCommand {

    /**
     * Executes the command for the specified route.
     *
     * @param request The HTTP request to process.
     * @throws Exception If an error occurs during execution.
     */
    HTTPResponse run(HTTPRequest request) throws Exception;
}
