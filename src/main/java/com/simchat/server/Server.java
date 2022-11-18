package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Server {

    protected ServerSocket serverSocket;

    private DatabaseMaster databaseMaster;
    public Server(ServerSocket serverSocket) {

        try {
            databaseMaster = new DatabaseMaster();
            databaseMaster.databaseInit();
            //-------------------------------------------this code is for testing purposes
            databaseMaster.resetDatabase();
            databaseMaster.fillTestUsers();
            //-------------------------------------------end of code for testing purposes
        } catch (SQLException e) {
            System.out.println("[Server Error] - Cannot open Database!");
            e.printStackTrace();
            closeServerSocket();
            System.exit(0);
        }

        this.serverSocket = serverSocket;
    }

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