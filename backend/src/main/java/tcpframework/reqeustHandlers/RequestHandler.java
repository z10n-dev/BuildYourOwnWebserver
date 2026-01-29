package tcpframework.reqeustHandlers;

import tcpframework.HTTPRequest;
import tcpframework.HTTPResponse;

import java.net.Socket;

public abstract class RequestHandler {

    public abstract HTTPResponse handle(HTTPRequest request) throws Exception;

    public HTTPResponse handle(HTTPRequest request, Socket socket) throws Exception {
        return handle(request);
    }
}