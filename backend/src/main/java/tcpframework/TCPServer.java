package tcpframework;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer extends Thread {

    private final AbstractHandler handler;
    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    public TCPServer(int port, AbstractHandler handlerObject) throws Exception{
        this.handler = handlerObject;
        this.serverSocket = new ServerSocket(port);
        this.pool = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void run(){
        System.out.println("TCPServer started on port " + serverSocket.getLocalPort());
        try{
            while(true){
                var clientSocket = serverSocket.accept();
                handler.handle(clientSocket, pool);
            }
        } catch (SocketException ignored){
            // Beim Aufruf von stopServer() wird eine SocketException geworfen
        } catch (Exception e){
            System.err.println("TCPServer Error: " + e.getMessage());
        }

    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ignored){
            // Ignorieren, da der Server geschlossen wird
        }
        pool.shutdown();
        System.out.println("TCPServer stopped on port " + serverSocket.getLocalPort());
    }
}
