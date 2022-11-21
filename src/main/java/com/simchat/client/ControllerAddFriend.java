package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.simchat.client.ClientMain.serverHandler;

/**
 * Controller of window "Add friend" with button for adding friend to friendlist. Controls are
 * defined in "AddFriend-view.fxml".
 */
public class ControllerAddFriend extends AbstractNetworkHandler implements Initializable {

    /**
     * JavaFX controls, defined in "AddFriend-view.fxml", which are shown on stage. Some of their
     * attributes, as listeners, are also deffined in "AddFriend-view.fxml".
     */
    @FXML
    private Button buttonAddFriend;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private Label labelLogInfo;

    /**
     * username of currently logged user. Be obtained from serverHandler.
     */
    private String username;

    /**
     * Initialize method called before stage is shown. Set serverHandler gui variable to this to
     * further manipulation with server-client communication (in synchronized part). Parameters are
     * defined in Initializable interface
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverHandler.setGUIThread(this);
        textFieldUserName.requestFocus();
        labelLogInfo.setText("");
        setUsername(serverHandler.getClientUsername());
    }

    /**
     * set username value to current logged user from serverHandler.
     * @param username of user currently logged.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Method bounded with control "Button buttonAddFriend" called when button is pressed.
     * Check user input from text fields, and if passed check, then via serverHandler sent message
     * to server about new added friend. Then refresh GUI and inform user.
     */
    @FXML
    protected void addFriendButtonClick()  {

        if(isCorrectInput() ){
            Message message = new Message(MessageType.ADD_FRIEND, textFieldUserName.getText());
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
            if(serverHandler.isAddedFriend()){
                serverHandler.getFriendList().put(textFieldUserName.getText(),0);
                serverHandler.putInMessageList(textFieldUserName.getText(),null);
                labelLogInfo.getStyleClass().add("labelLogInfoSuccess");
                labelLogInfo.setText("User: \"" +textFieldUserName.getText()+"\" added to friendlist");
                buttonAddFriend.setDisable(true);
            }else{
                labelLogInfo.getStyleClass().add("labelLogInfoError");
                labelLogInfo.setText("User: \"" +textFieldUserName.getText()+"\" doesn´t exists");
            }
        }
    }

    /**
     * Check user input from text field against required parameters. If user input matches parameters (is in correct form)
     * return true.
     * @return true if user input from text field matches all parameters.
     */
    private boolean isCorrectInput() {

        String regexPattern = ".*\\W.*";// match nonword character, only [a-zA-Z_0-9] can be used
        labelLogInfo.getStyleClass().add("labelLogInfoError");

        if (serverHandler.getFriendList().containsKey(textFieldUserName.getText())){//if already is in friendlist
            labelLogInfo.setText("Username already in friendlist!");
            textFieldUserName.requestFocus();
            return false;
        }
        if (Pattern.matches(regexPattern, textFieldUserName.getText())) {
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
        if(username.equals(textFieldUserName.getText())){
            labelLogInfo.setText("You cannot Add yourself man!");
            textFieldUserName.requestFocus();
            return false;
        }
        return true;
    }
}
