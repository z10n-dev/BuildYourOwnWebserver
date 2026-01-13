package com.ns.webserver;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ClientTool {
   public static void main(String[] args) {
       String host = "192.168.0.25";
       int port = 80;

       try (var socket = new Socket(host, port)) {
           var in = new ObjectInputStream(socket.getInputStream());
           var out = new ObjectOutputStream(socket.getOutputStream());

           var scanner = new Scanner(System.in);
           while (true) {
               System.out.print("> ");
               var id = 0;
                try {
                    id = Integer.parseInt(scanner.next());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid integer ID.");
                    continue;
                }
                if (id == 0) break;
                out.writeInt(id);
                out.flush();
                String name = (String) in.readObject();
                System.out.println("Name: " + name);
           }
       } catch (UnknownHostException e) {
           throw new RuntimeException(e);
       } catch (IOException e) {
           throw new RuntimeException(e);
       } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
       }
   }
}
