package com.simchat.server;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.simchat.server.ServerMain.*;

public class DatabaseMaster extends Database{

    public DatabaseMaster() throws SQLException {
        connection = DriverManager.getConnection(url,user,password);
    }

    public void databaseInit() throws SQLException {
        connection.prepareStatement("CREATE DATABASE IF NOT EXISTS "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase,user,password);
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) not null" +
                ", password VARCHAR(50) not null" +
                ", PRIMARY KEY(username))").execute(); //ID must be set as a kay, when AUTO_INCREMENT (dont know why ->otherwise exception)

    }

    public void resetDatabase() throws SQLException {
        connection.prepareStatement("DROP DATABASE IF EXISTS "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url,user,password);
        connection.prepareStatement("CREATE DATABASE "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase,user,password);
        resetTableUsers();
    }

    public void resetTableUsers() throws SQLException {
        connection.prepareStatement("DROP TABLE IF EXISTS users").execute();
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) not null" +
                ", password VARCHAR(50) not null" +
                ", PRIMARY KEY(username))").execute(); //ID must be set as a kay, when AUTO_INCREMENT (dont know why ->otherwise exception)
    }

    public void fillTestUsers() throws SQLException {
        //TODO After hashing passwords this will not work!
        addUser("asdasd","asdasd");
        addUser("qweqwe","qweqwe");
        addUser("werwer","werwer");
        addUser("sdfsdf","sdfsdf");

        addFriend("asdasd","qweqwe");
        addFriend("asdasd","werwer");
        addFriend("qweqwe","asdasd");
        addFriend("qweqwe","werwer");
        insertMessage("asdasd","qweqwe", "Hello there", LocalDateTime.parse("2022-02-16T10:22:15.1"));
        insertMessage("qweqwe","asdasd", "General Kenobi...", LocalDateTime.parse("2022-02-16T10:22:15.2"));

    }

}
