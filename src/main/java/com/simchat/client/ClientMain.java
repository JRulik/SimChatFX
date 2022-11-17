package com.simchat.client;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    public static void main(String[] args) {
        stageCreator = new StageCreator();
        try {
            serverHandler = new ServerHandler();
            Thread thread = new Thread(serverHandler);
            thread.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection with server lost!", ButtonType.OK);
            alert.showAndWait();
            serverHandler.closeEverything();
            System.out.println("[Error] - Can´t connect to server!");
            e.printStackTrace();
            System.exit(0);
        }
        launch();

    }
}