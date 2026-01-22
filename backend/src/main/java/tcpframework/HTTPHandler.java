package tcpframework;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Handles incoming HTTP connections and delegates tasks to appropriate handlers.
 */
public class HTTPHandler {
    private final RouterConfig router;

    /**
     * Constructs an HTTPHandler with the specified router configuration.
     *
     * @param router The router configuration used to find request handlers.
     */
    public HTTPHandler(RouterConfig router) {
        this.router = router;
    }

    /**
     * Handles an incoming socket connection by submitting the task to a thread pool.
     *
     * @param socket The client socket representing the connection.
     * @param pool   The thread pool used to execute the handling task.
     */
    public final void handle(Socket socket, ExecutorService pool) {
        pool.execute(() -> {
            try (socket) {
                var socketAddress = socket.getRemoteSocketAddress();
                System.out.println("Connection from " + socketAddress);
                runTask(socket);
            } catch (Exception e) {
                System.err.println("Handler Error: " + e.getMessage());
            }
        });
    }

    /**
     * Processes the HTTP request and delegates it to the appropriate request handler.
     *
     * @param socket The client socket representing the connection.
     * @throws Exception If an error occurs while processing the request.
     */
    protected void runTask(Socket socket) throws Exception {
        try {
            HTTPRequest request = HTTPRequestParser.parseHTTPRequest(socket);

            RequestHandler handler = router.findHandler(request);
            handler.handle(request, socket);
        } catch (Exception e) {
            HTTPErrorHandler.handleException(socket, e);
        }
    }

    ;
}
