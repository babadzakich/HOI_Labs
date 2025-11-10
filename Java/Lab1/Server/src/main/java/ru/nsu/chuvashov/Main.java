package ru.nsu.chuvashov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        if  (args.length != 2) {
            System.err.println("Usage: java -jar Main.jar <port> <threads_amount>");
        }
        Server server;
        try {
            server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NoSuchAlgorithmException e) {
            System.err.println("42");
            return;
        }
        server.start();
    }
}