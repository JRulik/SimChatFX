package com.simchat.shared.dataclasses;

import java.io.*;
import java.net.Socket;

import static com.simchat.server.ServerMain.PORT;

public abstract class AbstractNetworkHandler {
    protected Socket socket;
    protected static ObjectInputStream objectInputStream;
    protected static ObjectOutputStream objectOutputStream;

    protected void closeEverything(Socket socket, ObjectInputStream bufferReader, ObjectOutputStream bufferedWriter){
        try{
            if(bufferReader != null){
                bufferReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    protected void initSocketAndStreams(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }
    protected void initSocketAndStreams() throws IOException {
        socket = new Socket("localhost", PORT);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }
}
