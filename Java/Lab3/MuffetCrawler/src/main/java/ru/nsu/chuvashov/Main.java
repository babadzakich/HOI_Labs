package ru.nsu.chuvashov;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        if  (args.length != 1) {
            System.err.println("Usage: java Main <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        Crawler crawler = new Crawler(port);
        System.out.println("Starting Crawler");
        crawler.run();
    }
}