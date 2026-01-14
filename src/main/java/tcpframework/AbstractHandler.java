package tcpframework;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public abstract class AbstractHandler {
    public void handle (final Socket socket, ExecutorService pool) {
        pool.execute(() -> {
            var socketAdress = socket.getRemoteSocketAddress();
            System.out.println("Verbindung zu " + socketAdress + " hergestellt.");
            runTask(socket);

            try {
                socket.close();
            } catch (IOException ignored){
            }
        });
    }

    public abstract void runTask(Socket socket);
}
