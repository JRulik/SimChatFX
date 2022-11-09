package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;



public class ClientControllerSignUp extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Button buttonSignUp;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private TextField passwordFieldPassword;
    @FXML
    private TextField passwordFieldPassword2;
    @FXML
    private Label labelLogInfo;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labelLogInfo.setText("");
    }

    @FXML
    protected void signUp() throws IOException {
        if(passwordFieldPassword.getText().length()<5){
            labelLogInfo.setText("Password must have at least 5 characters!");
        }else if (!passwordFieldPassword.getText().equals(passwordFieldPassword2.getText())){
            labelLogInfo.setText("Passwords don´t match!");
        }else{
            Message message = new Message(MessageType.SIGNUPMESSAGE, textFieldUserName.getText()
                    +"\n"+ passwordFieldPassword.getText());
            objectOutputStream.writeObject(message);
            boolean signedUp = objectInputStream.readBoolean();
            if(signedUp){
                labelLogInfo.getStyleClass().add("labelLogInfoSuccess");
                labelLogInfo.setText("User: \"" +textFieldUserName.getText()+"\" was created");
                buttonSignUp.setDisable(true);
            }
            else{
                labelLogInfo.getStyleClass().add("labelLogInfoError");
                labelLogInfo.setText("Username already exist. Use other username!");
            }
        }
    }

}
