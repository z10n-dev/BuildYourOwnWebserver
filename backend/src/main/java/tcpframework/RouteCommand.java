package tcpframework;

import java.net.Socket;

@FunctionalInterface
public interface RouteCommand {
    void run(Socket socket, HTTPRequest request) throws Exception;
}
