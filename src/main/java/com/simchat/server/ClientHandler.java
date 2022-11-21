package com.simchat.server;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 * class used to handle individual client connection. Manage users request to server and sending
 * messages between users. Run in own thread. Called from Server class.
 */
public class ClientHandler extends AbstractNetworkHandler implements Runnable {

    /**
     * List of all ClientHandlers threads. Used for searching users to send them message.
     */
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    /**
     * ID of this thread.
     */
    private int threadID;

    /**
     * Username of client, which this thread handle. Set after login of user.
     */
    private String clientUsername;

    /**
     * Class to work with database, requesting data or inserting new.
     */
    private Database database;

    //TODO probably not needed. Check it, test it, delete it if not neccessary
    private boolean logged;

    /**
     * Initialize this ClientHandler. Set thread id and initialize socket communication with client.
     * @param threadID ID of this thread, set from Server.java.
     * @param socket socket on which will be this thread listening and writing to.
     */
    public ClientHandler(int threadID, Socket socket) {
        logged = false;
        this.threadID=threadID;
        try {
            initSocketAndStreams(socket);
            database = new Database();
            this.clientHandlers.add(this);
        } catch (IOException e) {
            closeEverything();
            System.out.println("[Thread ID:"+threadID+"] [ERROR] - can´t inicialize socket communication with client");
            e.printStackTrace();
        } catch (SQLException e) {
            closeEverything();
            System.out.println("[Thread ID:"+threadID+"] [ERROR] - can´t inicialize SQL connection");
            e.printStackTrace();
        }
    }

    /**
     * Main method of thread. Listening on socket. Filtering coming messages according their MessageType.
     */
    @Override
    public void run() {
            while(socket.isConnected()){
                try{
                    Message message = (Message) objectInputStream.readObject();
                    MessageType typeOfMessage = message.getMessageType();
                    switch(typeOfMessage){
                        case LOGIN_MESSAGE: logInClient(message); break;
                        case SIGNUP_MESSAGE: signUp(message); break;
                        case ADD_FRIEND: addFriend(message); break;
                        case RETURN_FRIENDLIST: returnFriendList(message); break;
                        case STANDARD_MESSAGE: recieveAndSendMessage(message); break;
                        case RETURN_MESSAGES_BETWEEN_USERS: sendMessagesBetweenUsers(message);break;
                    }
                }
                catch (IOException|ClassNotFoundException e) {
                    closeEverything();
                    System.out.println("[Thread ID:"+threadID+"] [ERROR] - Can´t communicate with client");
                    e.printStackTrace();
                    break;
                } catch (SQLException e) {
                    closeEverything();
                    System.out.println("[Thread ID:"+threadID+"] [ERROR] - Can´t communicate with SQL database");
                    e.printStackTrace();
                    break;
                }
            }
        System.out.println("[Thread ID:"+threadID+"] [Finished]");
    }

    /**
     * (MessageType RETURN_MESSAGES_BETWEEN_USERS) Return messages between users in "message".
     * Receive this information from database and then send new message with payload of messages between users
     * to requesting client.
     * @param message message with MessageType RETURN_MESSAGES_BETWEEN_USERS, with information between
     *                which users should be messages searched (fromUser,toUser) in whose message list (fromUser).
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
    private void sendMessagesBetweenUsers(Message message) throws SQLException, IOException {
        String fromUser,toUser;
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        ArrayList<Message>messagesBetweenUsers = database.getMessagesBetweenUsers(fromUser,toUser);
        message = new Message(MessageType.RETURN_MESSAGES_BETWEEN_USERS,fromUser,toUser,messagesBetweenUsers);
        objectOutputStream.writeObject(message);
    }

    /**
     * (MessageType STANDARD_MESSAGE) Send message between users.
     * Store message in message list of both users in database. Send message to each client in clientHandlers
     * which name is same as toUser (received side).
     * @param message message with MessageType STANDARD_MESSAGE, which is going to be stored and send to other user.
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
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

        Iterator<ClientHandler> iterator = clientHandlers.iterator();
        while (iterator.hasNext()){
            ClientHandler client = iterator.next();
            if (client.getClientUsername()!=null) {
                if (client.getClientUsername().equals(toUser) || client.getClientUsername().equals(fromUser)
                        && !client.equals(this)) {
                    try {
                        client.objectOutputStream.writeObject(message);
                    } catch (Exception e) {
                        client.closeEverything();
                        iterator.remove();
                    }
                }
            }
            //break; //cant be break because "dead" threads with client, which needs to be filtered
        }
        database.insertMessage(fromUser,toUser,messageRecieved,createdTime);
    }

    /**
     * (MessageType LOGIN_MESSAGE) Log in client. Get client username and password from message and
     * check in database, if user with this password exist. Send validation information (true/false) to client back
     * @param message message with MessageType LOGIN_MESSAGE, with user and his password which will be checked.
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
    protected void logInClient(Message message) throws IOException, SQLException {
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
        }
        objectOutputStream.writeObject(message);
    }

    /**
     * (MessageType SIGNUP_MESSAGE) Sign up client. Get client username and password from message and
     * check in database, if user with this password exist. If doesn´t add this user to database and
     * inform client about it (send true if user was added).
     * @param message message with MessageType SIGNUP_MESSAGE, with user and his password which will be checked.
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
    protected void signUp(Message message) throws IOException, SQLException{
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

    /**
     * (MessageType ADD_FRIEND) Add user (stored in payload of message) to client friendlist in database.
     * Send client information if user was adde (true if user was added to friendlist in database).
     * @param message message with MessageType SIGNUP_MESSAGE, with user and his password which will be checked.
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
    protected void addFriend(Message message) throws IOException, SQLException {
        String friendUserName = message.getMessage();

        //TODO here would be better to have MSG type from server with more return possibilities
        //TODO manage that you can add yourself as friend and write yourself msg
        //TODO Standartize usage of message for this needs (using msg payload and local client name or to/from user of message)
        if (!friendUserName.equals(this.clientUsername) && database.userExists(friendUserName)) {
            database.addFriend(clientUsername,friendUserName);
            message = new Message(MessageType.ADD_FRIEND,true);
        }
        else{
            message = new Message(MessageType.ADD_FRIEND,false);
        }
        objectOutputStream.writeObject(message);
    }

    /**
     * (MessageType RETURN_FRIENDLIST) Return friendlist of client from database to client.
     * @param message message with MessageType RETURN_FRIENDLIST.
     * @throws SQLException if can´t communicate with database or insert.
     * @throws IOException if can´t write to output stream.
     */
    protected void returnFriendList(Message message) throws IOException, SQLException {
        //TODO Standartize usage of message for this needs (using msg payload and local client name or to/from user of message)
        ArrayList<String> friends =  database.getFriends(clientUsername);
        Message returnMessage = new Message(MessageType.RETURN_FRIENDLIST);
        StringJoiner joinedFriends = new StringJoiner("\n");
        for (String friend: friends) {
            joinedFriends.add(friend);
        }
        returnMessage.setMessage(joinedFriends.toString());
        objectOutputStream.writeObject(returnMessage);
    }

    /**
     * @return username of client on other side of socket communication.
     */
    public String getClientUsername() {
        return clientUsername;
    }

}
