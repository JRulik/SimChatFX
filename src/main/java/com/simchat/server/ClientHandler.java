package com.simchat.server;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

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
                        case RETURNFRIENDLIST: returnFriendList(message); break;
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

    //TODO save password in hash
    protected void signUp(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (!database.userExists(username)) {
            database.addUser(username,password);
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

        //TODO here would be better to have MSG type from server with more return possibilities
        //TODO manage that you can add yourself as friend and write yourself msg
        if (!friendUserName.equals(this.clientUsername) && database.userExists(friendUserName)) {
            database.addFriend(clientUsername,friendUserName);
            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();
        }
        else{
            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();
        }
                /*
        if (!friendUserName.equals(this.clientUsername) && database.userExists(friendUserName)
                && !database.userInUserFriendlist(this.clientUsername,friendUserName) ) {
            database.addFriend(clientUsername,friendUserName);
            message = new Message(MessageType.SERVER_OK);
            objectOutputStream.writeObject(message);
        }
        else{
            message = new Message(MessageType.SERVER_ERROR,"User already in friendlist");
            objectOutputStream.writeObject(message);
        }*/
    }

    protected void returnFriendList(Message message) throws IOException, ClassNotFoundException, SQLException {
        ArrayList<String> friends =  database.getFriends(clientUsername);
        Message returnMessage = new Message(MessageType.SERVER_OK);
        StringJoiner joinedFriends = new StringJoiner("\n");
        for (String friend: friends) {
            joinedFriends.add(friend);
        }
        returnMessage.setMessage(joinedFriends.toString());
        objectOutputStream.writeObject(returnMessage);
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

}
