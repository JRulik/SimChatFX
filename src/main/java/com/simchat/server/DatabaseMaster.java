package com.simchat.server;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.simchat.server.ServerMain.*;

/**
 * Extension of Database where creating and droping database can be done. Used in Server. Not used in
 * ClientHandler.
 */
public class DatabaseMaster extends Database{

    /**
     * Create a database connection with global parameters from ServerMain.
     * @throws SQLException if connection to database cannot be done (e.g. database server not running).
     */
    public DatabaseMaster() throws SQLException {
        connection = DriverManager.getConnection(url, databaseUser, databasePassword);
    }

    /**
     * Init database on database server. Table with users (and their password + salt to hash) is created
     * with name users.
     * @throws SQLException if connection to database cannot be done or table cannot be created.
     */
    public void databaseInit() throws SQLException {
        connection.prepareStatement("CREATE DATABASE IF NOT EXISTS "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase, databaseUser, databasePassword);
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) not null UNIQUE" +
                ", password VARBINARY(512) not null" +
                ", salt VARBINARY(16) not null"+
                ", PRIMARY KEY(username))").execute(); //ID must be set as a kay, when AUTO_INCREMENT (dont know why ->otherwise exception)

    }

    /**
     * Drop existing database and create it again (lost all stored data!). Used for testing purposes.
     * @throws SQLException if can´t communicate with database.
     */
    public void resetDatabase() throws SQLException {
        connection.prepareStatement("DROP DATABASE IF EXISTS "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url, databaseUser, databasePassword);
        connection.prepareStatement("CREATE DATABASE "+nameOfDatabase).execute();
        connection = DriverManager.getConnection(url+"/"+nameOfDatabase, databaseUser, databasePassword);
        resetTableUsers();
    }

    /**
     * Drop and create table users (lost previous data). Used for testing purposes.
     * @throws SQLException if can´t communicate with database.
     */
    public void resetTableUsers() throws SQLException {
        connection.prepareStatement("DROP TABLE IF EXISTS users").execute();
        databaseInit();
    }

    /**
     * Create some default users (with fast typed names and passwords) and some default messages.
     * Used for testing purposes.
     * @throws SQLException if can´t communicate with database.
     */
    public void fillTestUsers() throws SQLException {
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
