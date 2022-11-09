package com.simchat.server;

import com.simchat.shareddataclasses.AbstractNetworkHandler;
import com.simchat.shareddataclasses.Message;
import com.simchat.shareddataclasses.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static com.simchat.server.ServerMain.database;


public class ClientHandler extends AbstractNetworkHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int threadID;

    private String clientUsername;

    private boolean logged;
    public ClientHandler(int threadID, Socket socket) {
        logged = false;

        this.threadID=threadID;
        try {
            initSocketAndStreams(socket);
            //Read line
            this.clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, objectInputStream, objectOutputStream);
            System.out.println("[Thread ID:"+threadID+"] finished");
        }
    }


    @Override
    public void run() {
            String messageFromClient;
            while(socket.isConnected()){
                try{
                    Message message = (Message) objectInputStream.readObject();
                    MessageType typeOfMessage = message.getMessageType();

                    if (typeOfMessage.equals(MessageType.LOGINMESSAGE)){
                        logInClient(message);
                    }
                    else if (typeOfMessage.equals(MessageType.SIGNUPMESSAGE)){
                       signUp(message);
                    }else{
                        //TODO logged client
                    }
                }
                catch (IOException e) {
                    closeEverything(socket, objectInputStream, objectOutputStream);
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    protected void logInClient(Message message) throws IOException, ClassNotFoundException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (database.userLoginAndPasswordMap.containsKey(username)
                && database.userLoginAndPasswordMap.get(username).equals(password)) {
            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();
        }
        else{
            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();
            System.out.println("tebe neznam");

        }
    }
    protected void signUp(Message message) throws IOException, ClassNotFoundException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (!database.userLoginAndPasswordMap.containsKey(username)) {
            database.userLoginAndPasswordMap.put(username,password);
            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();
        }
        else{
            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }



    /* // pred kontrolou vstupu
    @Override
    public void run() {
        DataOutputStream dataOutputStream= null;
        DataInputStream dataInputStream= null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream  = new DataInputStream(socket.getInputStream());

            String line = dataInputStream.readUTF();
            System.out.println(line);
            while (!line.equals("quit"))
            {
                try
                {
                    line = dataInputStream.readUTF();
                    System.out.println("[CLIENT #"+threadID+"] - write: "+line);
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            System.out.println("[CLIENT #"+threadID+"] - Disconnected");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

}
