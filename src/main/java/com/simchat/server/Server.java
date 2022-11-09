package com.simchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    protected ServerSocket serverSocket;
    public Server(ServerSocket serverSocket) {
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