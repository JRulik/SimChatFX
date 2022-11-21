package com.simchat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.*;

/**
 * Main class of Client side. Initiate server communication and start first JavaFX window "Log-In".
 */
public class ClientMain extends Application {

    /**
     * Object to handle server-client communication.
     */
    public static ServerHandler serverHandler;

    /**
     * Object to create individual stages for JavaFX GUI.
     */
    public static StageCreator stageCreator;

    /**
     * Start first JavaFX window "Log-In". This method is called automatically when
     * method launch() is called in main.
     */
    @Override
    public void start(Stage stage) {
        stage = stageCreator.createStage(stage, "LogIn-view.fxml","icon.png",
                "styles.css","SimChatFX-Login");
        stage.setOnCloseRequest(event -> {Platform.exit();System.exit(0);});
        stage.show();
    }

    /**
     * Run at start of the application. Initialize serverHandler in new thread.
     * @param args input arguments for java application.
     */
    public static void main(String[] args) {
        stageCreator = new StageCreator();
        try {
            serverHandler = new ServerHandler();
            Thread thread = new Thread(serverHandler);
            thread.start();
        } catch (IOException e) {
            System.out.println("[Error] - Can´t connect to server!");
            e.printStackTrace();
        }
        launch();

    }
}