package com.simchat.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * Create stage (from JavaFX) according to given parameters. Used for creating new GUI window.
 */
public class StageCreator {

    /**
     * Modify stage according to given parameters
     * @param stage stage which will be modified and returned
     * @param FxmlPath path (and name) to fxml file  from where JavaFX elements would be loaded
     * @param iconPath path (and name) to image icon shown when stage (window) is running
     * @param stylesPath path (and name) of styling classes for JavaFX elements
     * @param title title of stage (window)
     * @return modified stage given as parameter
     */
    public Stage createStage(Stage stage, String FxmlPath, String iconPath, String stylesPath,String title)  {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource(FxmlPath));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Thread interruption error!", ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
            //serverHandler.closeEverything();
            System.out.println("[Error] - Could not load scene from Fxml: " + FxmlPath);
            System.exit(0);
        }
        Image icon = new Image(ClientMain.class.getResourceAsStream(iconPath));
        stage.getIcons().add(icon);

        String css = this.getClass().getResource(stylesPath).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        return stage;
    }

    /**
     * Create new stage according to given parameters.
     * @param FxmlPath path (and name) to fxml file  from where JavaFX elements would be loaded
     * @param iconPath path (and name) to image icon shown when stage (window) is running
     * @param stylesPath path (and name) of styling classes for JavaFX elements
     * @param title title of stage (window)
     * @return modified stage given as parameter
     */
    public Stage createStage(String FxmlPath, String iconPath, String stylesPath,String title) {
        Stage stage = new Stage();
        return createStage( stage, FxmlPath, iconPath, stylesPath, title);
    }


}
