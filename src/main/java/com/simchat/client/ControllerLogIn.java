package com.simchat.client;

import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.simchat.client.ClientMain.serverHandler;
import static com.simchat.client.ClientMain.stageCreator;

/**
 * Controller of window "Login". Have buttons for Sign up user and Log in user. Controls are
 * defined in "LogIn-view.fxml".
 */
public class ControllerLogIn implements Initializable {

    /**
     * JavaFX controls, defined in "LogIn-view.fxml", which are shown on stage. Some of their
     * attributes, as listeners, are also defined in "LogIn-view.fxml".
     */
    @FXML
    private Label labelLogInfo;
    @FXML
    private Button buttonLogIn;
    @FXML
    private Button buttonSingUp;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private PasswordField passwordFieldPassword;

    /**
     * Initialize method called before stage is shown. Set serverHandler gui variable to this to
     * further manipulation with server-client communication (in synchronized part). Parameters are
     * defined in Initializable interface.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (serverHandler!=null){
            serverHandler.setGUIThread(this);
        }
        else{
            buttonLogIn.setDisable(true);
            buttonSingUp.setDisable(true);
            labelLogInfo.setText("[Error] - Can´t connect to server!");
        }
    }

    /**
     * Method bounded with control "Button buttonSingUp" called when button is pressed. Opens new GUI window
     * for signuping user and forwards control to ControllerSignUp. Waits for this new window to close.
     * @param e ActionEvent which invoke this method.
     */
    @FXML
    protected void signUpButtonClick(ActionEvent e){
        Stage stage = stageCreator.createStage("SignUp-view.fxml","icon.png",
                "styles.css","SimChatFX - Sign Up");
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.showAndWait();
        textFieldUserName.requestFocus();
    }


    /**
     * Method bounded with control "Button loginButton" called when button is pressed. Check correctness of input from textfields
     * and if correct send message with username and password via serverHandler to server.
     * If response from server is possitive (true), user is logged, new window "UserWindow" is shown and control is
     * forwarded to "ControllerUserWindow".
     *@param e ActionEvent which invoke this method
     */
    @FXML
    protected void loginButtonClick(ActionEvent e) {
        String regexPattern = ".*\\W.*";// match nonword character, only [a-zA-Z_0-9] can be used
        if( Pattern.matches(regexPattern, textFieldUserName.getText())
                || Pattern.matches(regexPattern, passwordFieldPassword.getText())){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Username or passqword can be only from this character set: [a-zA-Z_0-9]", ButtonType.OK);
            alert.showAndWait();
            textFieldUserName.requestFocus();
            return;
        }
        if (textFieldUserName.getText().equals("") || passwordFieldPassword.getText().equals("")){
            labelLogInfo.setText("Fill User Name or Password!");
        }
        else {
            Message message = new Message(MessageType.LOGIN_MESSAGE,
                    textFieldUserName.getText() + "\n" + passwordFieldPassword.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
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

            if (serverHandler.isLogged()) {
                labelLogInfo.setText("");
                serverHandler.setClientUsername(textFieldUserName.getText());
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage= stageCreator.createStage(stage,"Main-view.fxml","icon.png","styles.css"
                        ,"SimChatFX - "+ textFieldUserName.getText());
                stage.setOnCloseRequest(event -> {Platform.exit();System.exit(0);});
                stage.show();
            } else {
                labelLogInfo.setText("Wrong User Name or Password!");
            }

        }
        textFieldUserName.requestFocus(); //TODO check this if its needed
    }

}