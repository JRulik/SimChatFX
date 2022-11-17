package com.simchat.client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.*;

public class ClientMain extends Application {
    public static ServerHandler serverHandler;
    public static StageCreator stageCreator;
    
    @Override
    public void start(Stage stage) throws IOException {
        stage = stageCreator.createStage(stage, "LogIn-view.fxml","icon.png",
                "styles.css","SimChatFX");
        stage.show();
    }

    public static void main(String[] args) throws InterruptedException {
        stageCreator = new StageCreator();
        try {
            serverHandler = new ServerHandler();
            Thread thread = new Thread(serverHandler);
            thread.start();
        } catch (IOException e) {
            serverHandler.closeEverything();
            System.out.println("[Error] - Could not connect to server");
            e.printStackTrace();
        }
        launch();
    }
}