package com.ns.tcpframework;

import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;
import com.ns.tcpframework.logger.Stats;
import com.ns.tcpframework.reqeustHandlers.RequestHandler;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Handles incoming HTTP connections and delegates tasks to appropriate handlers.
 */
public class HTTPHandler {
    private final VirtualHostManager vManager;

    /**
     * Constructs an HTTPHandler with the specified router configuration.
     *
     * @param vManager The VirtualHostManager managing virtual hosts.
     */
    public HTTPHandler(VirtualHostManager vManager) {
        this.vManager = vManager;
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
                ServerLogger.getInstance().log(Loglevel.ERROR, e.getMessage(), LogDestination.EVERYWHERE);
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
            Stats.getInstance().incrementRequests();
            if (!request.getPath().contains("_next") && !request.getMethod().equals(HTTPMethode.HEAD) && !request.getPath().startsWith("/api/active-clients")) {
                ServerLogger.getInstance().log(Loglevel.DEBUG, request.getRequestHead() + " from " + socket.getRemoteSocketAddress() + "\n" + request.getHost(), LogDestination.EVERYWHERE);
            }

            VirtualHostConfig vhost = vManager.getVirtualHost(request.getHost());

            RouterConfig router = vhost.getRouter();
            RequestHandler handler = router.findHandler(request);
            HTTPResponse response = handler.handle(request, socket);
            if(!response.isSended()){
                response.send(socket);
            }
        } catch (Exception e) {
            HTTPErrorHandler.handleException(socket, e);
        }
    }
}
