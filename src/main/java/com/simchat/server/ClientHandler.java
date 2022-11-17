package com.simchat.server;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

public class ClientHandler extends AbstractNetworkHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int threadID;

    private String clientUsername;

    private Database database;

    private boolean logged;


    public int getThreadID() {
        return threadID;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public boolean isLogged() {
        return logged;
    }
    public ClientHandler(int threadID, Socket socket) {
        logged = false;

        this.threadID=threadID;
        try {
            initSocketAndStreams(socket);
            database = new Database();
            this.clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything();
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
                        case LOGIN_MESSAGE: logInClient(message); break;
                        case SIGNUP_MESSAGE: signUp(message); break;
                        case ADD_FRIEND: addFriend(message); break;
                        case RETURN_FRIENDLIST: returnFriendList(message); break;
                        case STANDART_MESSAGE: recieveAndSendMessage(message); break;
                        case RETURN_MESSAGES_BETWEEN_USERS: sendMessagesBetweenUsers(message);
                        //TODO case on returnRecievedMessages
                        default: //TODO logged client
                    }
                }
                catch (IOException e) {
                    closeEverything();
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    private void sendMessagesBetweenUsers(Message message) throws SQLException, IOException {
        String fromUser,toUser;
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        ArrayList<Message>messagesBetweenUsers = database.getMessagesBetweenUsers(fromUser,toUser);
        message = new Message(MessageType.RETURN_MESSAGES_BETWEEN_USERS,fromUser,toUser,messagesBetweenUsers);
        objectOutputStream.writeObject(message);
    }

    private void recieveAndSendMessage(Message message) throws SQLException, IOException {
        String fromUser,toUser,messageRecieved;
        LocalDateTime createdTime = message.getCreatedTime();
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        messageRecieved=message.getMessage();

        if(!database.isFriendOfSender(fromUser,toUser)){
                database.addFriend(toUser,fromUser);
        }
        if(!database.isFriendOfSender(toUser,fromUser)){
            database.addFriend(fromUser,toUser);
        }
        //TODO sort clientHandlers and find by binarysearch
        //TODO fix that there are null clients in clientHandlers -> clean them
        for (ClientHandler client: clientHandlers) {
            if (client.getClientUsername()!=null) {
                if (client.getClientUsername().equals(toUser) || client.getClientUsername().equals(fromUser)
                        && !client.equals(this)) {
                    try {
                        client.objectOutputStream.writeObject(message);
                    } catch (Exception e) {
                        client.closeEverything();
                        client.removeClientHandler();
                    }
                }
            }
            //break; //cant be break because "dead" threads with client, which needs to be filtered
        }
        database.insertMessage(fromUser,toUser,messageRecieved,createdTime);
    }




    protected void logInClient(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (database.checkUsernameAndPassword(username,password)) {
            logged=true;
            clientUsername=username;
            message = new Message(MessageType.LOGIN_MESSAGE,true);
        }
        else{
            message = new Message(MessageType.LOGIN_MESSAGE,false);
            System.out.println("tebe neznam");
        }
        objectOutputStream.writeObject(message);
    }

    //TODO save password in hash
    protected void signUp(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (!database.userExists(username)) {
            database.addUser(username,password);
            message = new Message(MessageType.SIGNUP_MESSAGE,true);
        }
        else{
            message = new Message(MessageType.SIGNUP_MESSAGE,false);
        }
        objectOutputStream.writeObject(message);
    }

    protected void addFriend(Message message) throws IOException, ClassNotFoundException, SQLException {
        String friendUserName = message.getMessage();

        //TODO here would be better to have MSG type from server with more return possibilities
        //TODO manage that you can add yourself as friend and write yourself msg
        if (!friendUserName.equals(this.clientUsername) && database.userExists(friendUserName)) {
            database.addFriend(clientUsername,friendUserName);
            message = new Message(MessageType.ADD_FRIEND,true);
        }
        else{
            message = new Message(MessageType.ADD_FRIEND,false);
        }
        objectOutputStream.writeObject(message);

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
        Message returnMessage = new Message(MessageType.RETURN_FRIENDLIST);
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
