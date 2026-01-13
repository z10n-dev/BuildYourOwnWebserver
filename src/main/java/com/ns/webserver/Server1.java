package com.ns.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class Server1 {
    private final int port;

    public Server1(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new java.net.ServerSocket(port)) {
            System.out.println("Server started on " + serverSocket.getLocalSocketAddress() + ":" + port);
            process(serverSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(ServerSocket serverSocket){
        while (true) {
            SocketAddress socketAddress = null;

            try (Socket socket = serverSocket.accept()) {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                socketAddress = socket.getRemoteSocketAddress();
                System.out.println("Connection established with " + socketAddress);

                while(true) {
                    Object obj = null;
                    try {
                        obj = in.readObject();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Received from " + socketAddress + ": " + obj);
                    out.writeObject("Echo: " + obj);
                }
            } catch (EOFException ignored) {

            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                System.out.println("Connection closed with " + socketAddress);
            }
        }
    }
}
