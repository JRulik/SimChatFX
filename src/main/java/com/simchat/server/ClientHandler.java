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
                        case LOGINMESSAGE: logInClient(message); break;
                        case SIGNUPMESSAGE: signUp(message); break;
                        case ADDFRIEND: addFriend(message); break;
                        case RETURNFRIENDLIST: returnFriendList(message); break;
                        case STANDARTMESSAGE: recieveAndSendMessage(message); break;
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

    private void recieveAndSendMessage(Message message) throws SQLException, IOException {
        String fromUser,toUser,messageRecieved;
        LocalDateTime createdTime = message.getCreatedTime();
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        messageRecieved=message.getMessage();

        //TODO sort clientHandlers and find by binarysearch
        for (ClientHandler client: clientHandlers) {
            if(client.getClientUsername().equals(toUser)){
                try {
                    client.objectOutputStream.writeObject(message);
                }
                catch(Exception e){
                    client.closeEverything();
                    client.removeClientHandler();
                }
            }
            //break; //cant be break because "dead" threads with client, which needs to bi filtered
        }

        String databaseDateFormat = dateTimeToDatabaseDate(createdTime);
        database.insertMessage(fromUser,toUser,messageRecieved,databaseDateFormat);
    }


    private String dateTimeToDatabaseDate(LocalDateTime createdTime) {
        return String.valueOf(createdTime).replace('T',' ');
    }

    protected void logInClient(Message message) throws IOException, ClassNotFoundException, SQLException {
        String[] usernameAndPassword = message.getMessage().split("\\r?\\n|\\r");//also only \\n
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        if (database.checkUsernameAndPassword(username,password)) {
            logged=true;
            clientUsername=username;
            message = new Message(MessageType.LOGINMESSAGE,true);
        }
        else{
            message = new Message(MessageType.LOGINMESSAGE,false);
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
            message = new Message(MessageType.SIGNUPMESSAGE,true);
        }
        else{
            message = new Message(MessageType.SIGNUPMESSAGE,false);
        }
        objectOutputStream.writeObject(message);
    }

    protected void addFriend(Message message) throws IOException, ClassNotFoundException, SQLException {
        String friendUserName = message.getMessage();

        //TODO here would be better to have MSG type from server with more return possibilities
        //TODO manage that you can add yourself as friend and write yourself msg
        if (!friendUserName.equals(this.clientUsername) && database.userExists(friendUserName)) {
            database.addFriend(clientUsername,friendUserName);
            message = new Message(MessageType.ADDFRIEND,true);
        }
        else{
            message = new Message(MessageType.ADDFRIEND,false);
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
        Message returnMessage = new Message(MessageType.RETURNFRIENDLIST);
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
