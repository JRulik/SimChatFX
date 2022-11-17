package com.simchat.server;

import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.simchat.server.ServerMain.*;

public class Database {

    protected Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase,user,password);
    }

    //TODO hashovani hesla
    public boolean checkUsernameAndPassword(String username, String password) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password= ?");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        ResultSet resultSet =  preparedStatement.executeQuery();
        if(resultSet.next()){
            return true;
        }
        return false;
    }

    public boolean userExists(String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet =  preparedStatement.executeQuery();
        if(resultSet.next()){
            return true;
        }
        return false;
    }

    public void addUser(String username, String password) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO users VALUES (?,?)");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.executeUpdate();

        createTableUserFriendList(username);
        createTableUserMessages(username);
    }

    public void addFriend(String username, String friendUserName) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO "+username+"_friendlist VALUES (?)");
        preparedStatement.setString(1, friendUserName);
        preparedStatement.executeUpdate();
    }

    public boolean isFriendOfSender(String senderUsername, String recieverUsername) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("SELECT * FROM "+recieverUsername+"_friendlist WHERE username=?");
        preparedStatement.setString(1, senderUsername);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> friends = new ArrayList<>();
        while(resultSet.next()){
            return true;
        }
        return false;
    }

    public ArrayList<String> getFriends(String username) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("SELECT * FROM "+username+"_friendlist");
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> friends = new ArrayList<>();
        while(resultSet.next()){
            friends.add(String.valueOf(resultSet.getString(1)));
        }
        return friends;
    }
    public void createTableUserFriendList(String username) throws SQLException {
            //TODO set username unique
           connection.prepareStatement("CREATE TABLE "+username+"_friendlist (username VARCHAR(50), PRIMARY KEY(username))").execute();
    }
    public void createTableUserMessages(String username) throws SQLException {
            connection.prepareStatement("CREATE TABLE "+username+"_messages (ID INTEGER NOT NULL AUTO_INCREMENT," +
                    "fromUser VARCHAR(50),toUser VARCHAR(50)," +
                    "datetime DATETIME,message TEXT, PRIMARY KEY(ID))").execute();
    }
    private String dateTimeToDatabaseDate(LocalDateTime createdTime) {
        return String.valueOf(createdTime).replace('T',' ');
    }
    private LocalDateTime databaseDateToDateTime(String date) {
        LocalDateTime dateTime =  LocalDateTime.parse(date.replace(' ','T'));
        return dateTime;
    }

    public void insertMessage(String fromUser, String toUser, String messageToSend, LocalDateTime createdTime) throws SQLException {
        String databaseDateFormat = dateTimeToDatabaseDate(createdTime);
        PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO "+fromUser+"_messages " +
                "(fromUser, toUser,datetime,message) VALUES (?,?,?,?)");
        preparedStatement.setString(1, fromUser);
        preparedStatement.setString(2, toUser);
        preparedStatement.setString(3, databaseDateFormat);
        preparedStatement.setString(4, messageToSend);
        preparedStatement.executeUpdate();

        preparedStatement =  connection.prepareStatement("INSERT INTO "+toUser+"_messages " +
                "(fromUser, toUser,datetime,message) VALUES (?,?,?,?)");
        preparedStatement.setString(1, fromUser);
        preparedStatement.setString(2, toUser);
        preparedStatement.setString(3, databaseDateFormat);
        preparedStatement.setString(4, messageToSend);
        preparedStatement.executeUpdate();

    }

    public ArrayList<Message> getMessagesBetweenUsers(String fromUser, String toUser) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("SELECT * FROM "+fromUser+"_messages WHERE (fromUser=? AND toUser=?) OR (fromUser=? AND toUser=?)");
        preparedStatement.setString(1, fromUser);
        preparedStatement.setString(2, toUser);
        preparedStatement.setString(3, toUser);
        preparedStatement.setString(4, fromUser);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Message> messages = new ArrayList<>();
        while(resultSet.next()){
            LocalDateTime dateTime= databaseDateToDateTime(String.valueOf(resultSet.getString(4)));
            String from,to,message;
            from = String.valueOf(resultSet.getString(2));
            to=String.valueOf(resultSet.getString(3));
            message = String.valueOf(resultSet.getString(5));
            messages.add(new Message(MessageType.STANDART_MESSAGE, from, to,dateTime, message));
        }
        return messages;
    }

}