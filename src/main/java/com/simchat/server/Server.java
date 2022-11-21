package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Listening part of server. Listen on server socket and when new incomming connection is there
 * runs ClientHandler in separate thread to handle new client.
 */
public class Server {

    /**
     * Server socket on which is this server listening.
     */
    protected ServerSocket serverSocket;

    /**
     * Used to manipulate with database on "higher" level (possibility to create/drop database).
     */
    private DatabaseMaster databaseMaster;

    /**
     * Initialise database and set socket.
     * @param serverSocket server socket to be set in this class to listen on.
     */
    public Server(ServerSocket serverSocket) {
        try {
            databaseMaster = new DatabaseMaster();
            databaseMaster.databaseInit();
            //-------------------------------------------this code is for testing purposes
            //databaseMaster.resetDatabase();
            //databaseMaster.fillTestUsers();
            //-------------------------------------------end of code for testing purposes
        } catch (SQLException e) {
            System.out.println("[Server Error] - Cannot open Database!");
            e.printStackTrace();
            closeServerSocket();
            System.exit(0);
        }

        this.serverSocket = serverSocket;
    }

    /**
     * Main method of thread, started in Server.java. Listening on server socket, when new connection
     * is established, pass it to new thread ClientHandler to handle this client and then listen on
     * socket again.
     */
    public void start() {
        int threadID =1;
        try{
            while (!serverSocket.isClosed()){
                System.out.println("[SERVER] - Waiting for connection [Thread ID:"+threadID+"]");
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(threadID, socket));
                thread.start();
                System.out.println("[SERVER] - Connected [Thread ID:"+threadID+"]");
                threadID++;
            }
        }
        catch(IOException e){
            System.out.println("[Server Error] - Cannot open communication with client!");
            e.printStackTrace();
        }
    }

    /**
     * close server socket
     */
    public void closeServerSocket(){
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}