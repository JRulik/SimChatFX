package com.simchat.server;

import com.simchat.shared.dataclasses.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

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

    public void insertUser(String username, String password) throws SQLException {
        PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO users VALUES (?,?)");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.executeUpdate();
    }

    public void createTableUserFriendList(String username){
        try {
            PreparedStatement preparedStatement =  connection.prepareStatement("CREATE TABLE IF NOT EXISTS ? (username VARCHAR(50))");
            preparedStatement.setString(1, username);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
