package com.simchat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientControllerLogIn implements Initializable {
    @FXML
    private Label labelLogInfo;

    @FXML
    protected void signUpButtonClick(ActionEvent e) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SignUp-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 260);
        Stage stage = new Stage();
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.showAndWait();
        //stage.show();
    }

    @FXML
    protected void loginButtonClick() {
        labelLogInfo.setText("hovno!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }



}