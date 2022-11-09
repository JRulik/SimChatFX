package com.simchat.server;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

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
                    switch(typeOfMessage){
                        case LOGINMESSAGE: logInClient(message); break;
                        case SIGNUPMESSAGE: signUp(message); break;
                        default: //TODO logged client
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

        if (database.hashMapUserLoginAndPassword.containsKey(username)
                && database.hashMapUserLoginAndPassword.get(username).equals(password)) {
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

        if (!database.hashMapUserLoginAndPassword.containsKey(username)) {
            database.hashMapUserLoginAndPassword.put(username,password);
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

}
