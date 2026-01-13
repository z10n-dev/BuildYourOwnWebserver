package com.ns.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server1 {
    private final int port;
    private Map<Integer, String> map;

    public Server1(int port) {
        this.port = port;
        load();
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
                    var id = in.readInt();
                    var name = map.get(id);
                    out.writeObject(name);
                    out.flush();
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

    private void load() {
        map = new HashMap<>();
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");
    }
}
