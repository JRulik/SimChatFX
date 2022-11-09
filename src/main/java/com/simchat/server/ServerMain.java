package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerMain {
    public final static int PORT = 6612;
    public static Database database;

    public static void main(String[] args) throws IOException {
        Database database = new Database();
        System.out.println("TEST");
        database.hashMapUserLoginAndPassword.put("honza","aznoh");

        ServerSocket serverSocket = null;
        serverSocket= new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.start();
    }

}
