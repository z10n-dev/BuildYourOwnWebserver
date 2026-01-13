package com.ns.webserver;

public class Main {
    public static void main(String[] args) {
        while(true){
            System.out.println("Hello, Noex09!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}