package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Main class of server side. Stores global variables of server and database information
 * Init serversocket and run new thread with server.
 */
public class ServerMain {

    /**
     * Port of server on which is listening. Used also on client side.
     */
    public final static int PORT = 6612;

    /**
     * Address of server. Default is localhost (server is host on same computer as this application.
     * is running)
     */
    public final static String serverHost="localhost";

    /**
     * Admin user of database. Default value root set for usage with default configuration of
     * XAMPP.
     */
    public final static String databaseUser ="root";

    /**
     * Password of admin user of database. Default value "" (empty string) set for usage with default configuration of
     * XAMPP.
     */
    public final static String databasePassword = "";

    /**
     * Address of database.Default value "jdbc:mysql://localhost:3306" set for usage with default configuration of
     * XAMPP.
     */
    public final static String url = "jdbc:mysql://localhost:3306";

    /**
     * Name of database, used to create new database on database server.
     */
    public final static String nameOfDatabase = "simchatfx_database";

    /**
     * Main methos od server side. Initialize socket and run new thread with server listener.
     * @param args arguments used when starting up this application.
     */
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
