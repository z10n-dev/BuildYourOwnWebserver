package tcpframework;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public abstract class AbstractHandler {
    public final void handle (Socket socket, ExecutorService pool) {
        pool.execute(() -> {
           try(socket) {
               var socketAddress = socket.getRemoteSocketAddress();
               System.out.println("Connection from " + socketAddress);
               runTask(socket);
           } catch (Exception e) {
                System.err.println("Handler Error: " + e.getMessage());
           }
        });
    }

    protected abstract void runTask(Socket socket) throws IOException;
}
