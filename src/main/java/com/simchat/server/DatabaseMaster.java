package com.simchat.server;

import java.sql.DriverManager;
import java.sql.SQLException;

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

    public void fillTableUsers() throws SQLException {
        addUser("honza","aznoh");
        addUser("asdasd","asdasd");
        addUser("qweqwe","qweqwe");

    }


}
