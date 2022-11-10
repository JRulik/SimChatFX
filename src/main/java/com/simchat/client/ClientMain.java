package com.simchat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource("LogIn-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        System.out.println(ClientMain.class);
        Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
        stage.getIcons().add(icon);

        String css = this.getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("SimChatFX");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) throws InterruptedException {
        launch();
    }
}