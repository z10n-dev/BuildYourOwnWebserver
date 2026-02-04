package com.ns.tcpframework;

import com.ns.tcpframework.logger.LogDestination;
import com.ns.tcpframework.logger.Loglevel;
import com.ns.tcpframework.logger.ServerLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * A multithreaded TCP server that listens for incoming client connections
 * and delegates request handling to an HTTPHandler.
 * This server uses virtual threads for efficient concurrency.
 */
public class TCPServer extends Thread {

    private final HTTPHandler handler;
    private final ServerSocket serverSocket;
    private final ExecutorService pool;


    /**
     * Constructs a TCPServer instance with the specified port and HTTPHandler.
     *
     * @param port          The port number on which the server will listen for connections.
     * @param handlerObject The HTTPHandler instance to handle client requests.
     * @throws Exception If an error occurs while initializing the server socket.
     */
    public TCPServer(int port, HTTPHandler handlerObject, ExecutorService pool) throws Exception{
        this.handler = handlerObject;
        this.serverSocket = new ServerSocket(port);
        this.pool = pool;
    }

    /**
     * Starts the server and listens for incoming client connections.
     * Each connection is handled in a separate virtual thread.
     */
    public void run(){
        ServerLogger.getInstance().log(Loglevel.INFO, "TCPServer started on port " + serverSocket.getLocalPort(), LogDestination.SERVER);
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

    /**
     * Stops the server by closing the server socket and shutting down the thread pool.
     */
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
