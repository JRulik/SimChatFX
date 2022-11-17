package com.simchat.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class StageCreator {

    public Stage createStage(Stage stage, String FxmlPath, String iconPath, String stylesPath,String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource(FxmlPath));
        Scene scene = new Scene(fxmlLoader.load());
        Image icon = new Image(ClientMain.class.getResourceAsStream(iconPath));
        stage.getIcons().add(icon);

        String css = this.getClass().getResource(stylesPath).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        return stage;
    }
    public Stage createStage(String FxmlPath, String iconPath, String stylesPath,String title) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource(FxmlPath));
        Scene scene = new Scene(fxmlLoader.load());
        Image icon = new Image(ClientMain.class.getResourceAsStream(iconPath));
        stage.getIcons().add(icon);

        String css = this.getClass().getResource(stylesPath).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        return stage;
    }
}
