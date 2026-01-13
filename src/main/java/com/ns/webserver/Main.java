package com.ns.webserver;

public class Main {
    public static void main(String[] args) {
        while(true){
            System.out.println("Hello, World!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}