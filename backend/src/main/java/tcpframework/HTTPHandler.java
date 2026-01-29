package tcpframework;

import com.ns.webserver.handlers.ServerLogger;
import tcpframework.reqeustHandlers.RequestHandler;

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
//                System.out.println("Connection from " + socketAddress);
//                ServerLogger.getInstance().newLog(String.format("Connection from " + socketAddress));
                runTask(socket);
            } catch (Exception e) {
                System.err.println("Handler Error: " + e.getMessage());
                ServerLogger.getInstance().newLog(e.getMessage());
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
            if (!request.getPath().contains("_next") && !request.getMethod().equals(HTTPMethode.HEAD) && !request.getPath().startsWith("/api/active-clients")) {
                ServerLogger.getInstance().newLog("REQUEST: " + request.getMethod() + " " + request.getPath());
            }

            RequestHandler handler = router.findHandler(request);
            HTTPResponse response = handler.handle(request, socket);
            response.send(socket);
        } catch (Exception e) {
            HTTPErrorHandler.handleException(socket, e);
        }
    }

    ;
}
