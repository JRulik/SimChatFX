package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class ServerMain {
    public final static int PORT = 6612;
    public final static String serverHost="localhost";
    public final static String user ="root";
    public final static String password = "";
    public final static String url = "jdbc:mysql://localhost:3306";
    public final static String nameOfDatabase = "simchatfx_database";

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket= new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("[Server Error] - Cannot open Server socket!");
            e.printStackTrace();
            System.exit(0);
        }
        Server server = new Server(serverSocket);
        server.start();
    }

}
