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

/**
 * Controller of window "SignUp". Have buttons for Sign up user and Log in user. Controls are
 * defined in "SignUp-view.fxml".
 */
public class ControllerSignUp extends AbstractNetworkHandler implements Initializable {

    /**
     * JavaFX controls, defined in "SignUp-view.fxml", which are shown on stage. Some of their
     * attributes, as listeners, are also deffined in "SignUp-view.fxml".
     */
    @FXML
    private Button buttonSignUp;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private TextField passwordFieldPassword;
    @FXML
    private TextField passwordFieldPasswordAgain;
    @FXML
    private Label labelLogInfo;

    /**
     * Initialize method called before stage is shown. Set serverHandler gui variable to this to
     * further manipulation with server-client communication (in synchronized part). Parameters are
     * defined in Initializable interface
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverHandler.setGUIThread(this);
        labelLogInfo.setText("");
    }

    /**
     * Method bounded with control "Button buttonSingUp" called when button is pressed.  Check correctness of input from
     * textfields and if correct send message with username and password via serverHandler to server. Then inform user
     * (trough GUI), if user was created (depends on answer from server).
     */
    @FXML
    protected void signUp() throws IOException {
        if (isCorrectInput()) {
            Message message = new Message(MessageType.SIGNUP_MESSAGE, textFieldUserName.getText()
                    + "\n" + passwordFieldPassword.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.sendMessage(message);

            synchronized (this) {//to be able to wake up from other thread
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

    /**
     * Check user input from text field against required parameters. If user input matches parameters (is in correct form)
     * return true.
     * @return true if user input from text field matches all parameters
     */
    private boolean isCorrectInput() {
        //TODO check this regex
       // String regexPattern = ".*\s*[\u0020,./;'#=<>?:@{}_+-\\[\\]].*\s*"; // Some error with upper cases letters (also filtered)
        String regexPattern = ".*\\W.*";// match nonword character
        labelLogInfo.getStyleClass().add("labelLogInfoError");
        if (Pattern.matches(regexPattern, textFieldUserName.getText())
                || Pattern.matches(regexPattern, passwordFieldPassword.getText())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Username or password can be only from this character set: [a-zA-Z_0-9]", ButtonType.OK);
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
        if (!passwordFieldPassword.getText().equals(passwordFieldPasswordAgain.getText())) {
            passwordFieldPassword.requestFocus();
            labelLogInfo.setText("Passwords don´t match!");
            return false;
        }
        return true;
    }
}