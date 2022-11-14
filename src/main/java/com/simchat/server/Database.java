package com.simchat.server;

import java.sql.*;
import java.time.LocalDate;
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

    public ArrayList<String> getFriends(String username) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("SELECT * FROM "+username+"_friendlist");
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> friends = new ArrayList<>();
        while(resultSet.next()){
            friends.add(String.valueOf(resultSet.getString(1)));
        }
        return friends;
    }
    public void createTableUserFriendList(String username){
        try {
            //TODO set username unique
           connection.prepareStatement("CREATE TABLE "+username+"_friendlist (username VARCHAR(50), PRIMARY KEY(username))").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public void createTableUserMessages(String username){
        try {
            connection.prepareStatement("CREATE TABLE "+username+"_messages (ID INTEGER NOT NULL AUTO_INCREMENT," +
                    "fromUser VARCHAR(50),toUser VARCHAR(50)," +
                    "datetime DATETIME,message TEXT, PRIMARY KEY(ID))").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void insertMessage(String fromUser, String toUser, String messageToSend, String createdTime) throws SQLException {

        PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO "+fromUser+"_messages " +
                "(fromUser, toUser,datetime,message) VALUES (?,?,?,?)");
        preparedStatement.setString(1, fromUser);
        preparedStatement.setString(2, toUser);
        preparedStatement.setString(3, createdTime);
        preparedStatement.setString(4, messageToSend);
        preparedStatement.executeUpdate();

        preparedStatement =  connection.prepareStatement("INSERT INTO "+toUser+"_messages " +
                "(fromUser, toUser,datetime,message) VALUES (?,?,?,?)");
        preparedStatement.setString(1, fromUser);
        preparedStatement.setString(2, toUser);
        preparedStatement.setString(3, createdTime);
        preparedStatement.setString(4, messageToSend);
        preparedStatement.executeUpdate();

    }
    /*
    public boolean userInUserFriendlist(String clientUsername, String friendUserName) {

    }*/

/*
@Converter(autoApply = true)
public class LocalDateAttributeConverter implements
        AttributeConverter<LocalDate, Date> {

    @Override
    public Date convertToDatabaseColumn(LocalDate locDate) {
        return (locDate == null ? null : Date.valueOf(locDate));
    }

    @Override
    public LocalDate convertToEntityAttribute(Date sqlDate) {
        return (sqlDate == null ? null : sqlDate.toLocalDate());
    }*/
}