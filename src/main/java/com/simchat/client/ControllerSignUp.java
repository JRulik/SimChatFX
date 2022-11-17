package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.simchat.client.ClientMain.serverHandler;


public class ControllerSignUp extends AbstractNetworkHandler implements Initializable {
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
        serverHandler.setGUIThread(this);
        labelLogInfo.setText("");
    }

    @FXML
    protected void signUp() throws IOException {
        if (isCorrectInput()) {
            Message message = new Message(MessageType.SIGNUP_MESSAGE, textFieldUserName.getText()
                    + "\n" + passwordFieldPassword.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.sendMessage(message);

            synchronized (this) {
                while (!serverHandler.isProcessedRequest()) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Thread interruption error!", ButtonType.OK);
                        alert.showAndWait();
                        serverHandler.closeEverything();
                        System.out.println("[Error] -Thread interruption error!");
                        ex.printStackTrace();
                        System.exit(0);
                    }
                }
            }
            if (serverHandler.isSignedUp()) {
                labelLogInfo.getStyleClass().add("labelLogInfoSuccess");
                labelLogInfo.setText("User: \"" + textFieldUserName.getText() + "\" was created");
                buttonSignUp.setDisable(true);
            } else {
                labelLogInfo.setText("Username already exists!");
                textFieldUserName.requestFocus();
            }
        }
    }

    private boolean isCorrectInput() {
        String regexPattern = ".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*";
        labelLogInfo.getStyleClass().add("labelLogInfoError");
        if (Pattern.matches(regexPattern, textFieldUserName.getText())
                || Pattern.matches(regexPattern, passwordFieldPassword.getText())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "This characters  \",/;'#=<> ?:@~{}+-\" can´t be used in name or password", ButtonType.OK);
            alert.showAndWait();
            textFieldUserName.requestFocus();
            return false;
        }
        if (textFieldUserName.getText().length() < 3) {
            labelLogInfo.setText("Username must have at least 3 characters!");
            textFieldUserName.requestFocus();
            return false;
        }
        if (passwordFieldPassword.getText().length() < 5) {
            labelLogInfo.setText("Password must have at least 5 characters!");
            passwordFieldPassword.requestFocus();
            return false;
        }
        if (!passwordFieldPassword.getText().equals(passwordFieldPassword2.getText())) {
            passwordFieldPassword.requestFocus();
            labelLogInfo.setText("Passwords don´t match!");
            return false;
        }
        return true;
    }
}