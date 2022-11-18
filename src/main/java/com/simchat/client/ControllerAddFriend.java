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

public class ControllerAddFriend extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Button buttonAddFriend;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private Label labelLogInfo;

    private String username;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverHandler.setGUIThread(this);
        textFieldUserName.requestFocus();
        labelLogInfo.setText("");
        setUsername(serverHandler.getClientUsername());
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @FXML
    protected void addFriendButtonClick()  {

        if(isCorrectInput() ){
            Message message = new Message(MessageType.ADD_FRIEND, textFieldUserName.getText());
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
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
            if(serverHandler.isAddedFriend()){
                serverHandler.getFriendList().add(textFieldUserName.getText());
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

    private boolean isCorrectInput() {

        String regexPattern = ".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*";
        labelLogInfo.getStyleClass().add("labelLogInfoError");

        if (serverHandler.getFriendList().contains(textFieldUserName.getText())){
            labelLogInfo.setText("Username already in friendlist!");
            textFieldUserName.requestFocus();
            return false;
        }
        if (Pattern.matches(regexPattern, textFieldUserName.getText())) {
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
        if(username.equals(textFieldUserName.getText())){
            labelLogInfo.setText("You cannot Add yourself man!");
            textFieldUserName.requestFocus();
            return false;
        }
        return true;
    }
}
