package com.simchat.shared.dataclasses;

import java.io.*;
import java.net.Socket;

import static com.simchat.server.ServerMain.PORT;
import static com.simchat.server.ServerMain.serverHost;

public abstract class AbstractNetworkHandler {
    protected Socket socket;
    protected ObjectInputStream objectInputStream;
    protected ObjectOutputStream objectOutputStream;


    protected void initSocketAndStreams(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }
    protected void initSocketAndStreams() throws IOException {
        socket = new Socket(serverHost, PORT);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void closeEverything(){
        try{
            if(objectInputStream != null){
                objectInputStream.close();
            }
            if(objectOutputStream != null){
                objectOutputStream.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            System.out.println("[Error] -Cannot close communication!");
            e.printStackTrace();
        }
    }
}
