package com.simchat.server;

import java.sql.*;
import java.util.ArrayList;

import static com.simchat.server.ServerMain.*;

public class Database {

    protected Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase,user,password);
    }


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

    /*
    public boolean userInUserFriendlist(String clientUsername, String friendUserName) {

    }*/
}
