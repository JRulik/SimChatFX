package com.simchat.shared.dataclasses;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static com.simchat.server.ServerMain.PORT;
import static com.simchat.server.ServerMain.serverHost;

/**
 * Class used to initialize and close network communication. Extended in ClientHandler and ServerHandler for client-server
 * communication.
 */
public abstract class AbstractNetworkHandler {

    /**
     * Communication socket from where streams are get.
     */
    protected Socket socket;

    /**
     * Input stream to read object (Message) from socket.
     */
    protected ObjectInputStream objectInputStream;

    /**
     * Output stream to write object (Message) to socket.
     */
    protected ObjectOutputStream objectOutputStream;


    /**
     * Initialize class socket to param socket. Initialize output/input stream.
     * @param socket socket to store as class variable.
     * @throws IOException when stream could not be initialized from socket.
     */
    protected void initSocketAndStreams(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Initialize new socket and store it to class variable. Used to initialize server socket (from Server.java).
     * Initialize output/input stream.
     * @throws IOException when stream could not be initialized from socket.
     */
    protected void initSocketAndStreams() throws IOException {
        socket = new Socket(serverHost, PORT);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Close open stream or socket.
     */
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
        }catch(SocketException e){
            System.out.println("[Error] -Cannot close communication! - client socked already closed");
            e.printStackTrace();
        }catch(IOException e){
        System.out.println("[Error] -Cannot close communication!");
        e.printStackTrace();
    }
    }
}
