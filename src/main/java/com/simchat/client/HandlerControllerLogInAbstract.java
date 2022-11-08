package com.simchat.client;

import com.simchat.shareddataclasses.Message;
import com.simchat.shareddataclasses.AbstractNetworkHandler;
import com.simchat.shareddataclasses.MessageType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class HandlerControllerLogInAbstract extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Label labelLogInfo;
    @FXML
    private Button buttonLogIn;
    @FXML
    private Button buttonSingUp;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private TextField textFieldPassword;
    @FXML
    protected void signUpButtonClick(ActionEvent e) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SignUp-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 260);
        Stage stage = new Stage();
        stage.setTitle("SimChatFX - Sign Up");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.showAndWait();
        //stage.show();
    }

    @FXML
    protected void loginButtonClick() throws IOException {
        String name, password;
        name = textFieldUserName.getText();
        password = textFieldPassword.getText();
        Message message = new Message(MessageType.LOGINMESSAGE,name+"\n"+password);
        objectOutputStream.writeObject(message);
        boolean logged = objectInputStream.readBoolean();
        if (logged){
            labelLogInfo.setText("");
            //TODO switch scenes
        }
        else{
            labelLogInfo.setText("Wrong User Name or Password!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initSocketAndStreams();
        } catch (IOException e) {
            buttonLogIn.setDisable(true);
            buttonSingUp.setDisable(true);
            closeEverything(socket, objectInputStream, objectOutputStream);
            labelLogInfo.setText("[Error] -Could not connect to server");
            e.printStackTrace();
        }
    }



}