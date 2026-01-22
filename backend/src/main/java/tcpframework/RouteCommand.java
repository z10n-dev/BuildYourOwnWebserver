package tcpframework;

import java.net.Socket;

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
     * @param socket  The socket connection to the client.
     * @param request The HTTP request to process.
     * @throws Exception If an error occurs during execution.
     */
    void run(Socket socket, HTTPRequest request) throws Exception;
}
