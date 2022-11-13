package com.simchat.server;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler extends AbstractNetworkHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int threadID;

    private String clientUsername;

    private Database database;

    private boolean logged;
    public ClientHandler(int threadID, Socket socket) {
        logged = false;

        this.threadID=threadID;
        try {
            initSocketAndStreams(socket);
            database = new Database();
            this.clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything(socket, objectInputStream, objectOutputStream);
            System.out.println("[Thread ID:"+threadID+"] finished");
        } catch (SQLException e) {
            e.printStackTrace();
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
                        case ADDFRIEND: addFriend(message); break;
                        default: //TODO logged client
                    }
                }
                catch (IOException e) {
                    closeEverything(socket, objectInputStream, objectOutputStream);
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    protected void logInClient(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (database.checkUsernameAndPassword(username,password)) {
            clientUsername=username;
            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();
        }
        else{
            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();
            System.out.println("tebe neznam");
        }
    }

    protected void signUp(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (!database.userExists(username)) {
            database.insertUser(username,password);
            database.createTableUserFriendList(username);
            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();
        }
        else{
            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();
        }
    }

    protected void addFriend(Message message) throws IOException, ClassNotFoundException, SQLException {
        String friendUserName = message.getMessage();

        if (!database.userExists(friendUserName)) {
            database.addFriend(clientUsername,friendUserName);
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
