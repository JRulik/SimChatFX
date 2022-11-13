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

public class ClientControllerAddFriend extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Button buttonAddFriend;
    @FXML
    private TextField textFieldUserName;
    @FXML
    private Label labelLogInfo;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textFieldUserName.requestFocus();
        labelLogInfo.setText("");
    }

    @FXML
    protected void addFriendButtonClick() throws IOException {
        if( Pattern.matches(".*\s*[\u0020,./;'#=<>?:@~{}_+-].*\s*", textFieldUserName.getText())){
            Alert alert = new Alert(Alert.AlertType.WARNING, "This characters  \",/;'#=<> ?:@~{}+-\" can´t be used in name or password", ButtonType.OK);
            alert.showAndWait();
            textFieldUserName.requestFocus();
            return;
        }
        if(textFieldUserName.getText().length()<3){
            labelLogInfo.setText("Username must have at least 3 characters!");
            textFieldUserName.requestFocus();
            return;
        }

        //TODO check if user is in friendlist already

        Message message = new Message(MessageType.ADDFRIEND, textFieldUserName.getText());
        objectOutputStream.writeObject(message);
        boolean addedFriend = objectInputStream.readBoolean();
        if(addedFriend){
            labelLogInfo.getStyleClass().add("labelLogInfoSuccess");
            labelLogInfo.setText("User: \"" +textFieldUserName.getText()+"\" added to friendlist");
        }else{
            labelLogInfo.getStyleClass().add("labelLogInfoError");
            labelLogInfo.setText("User: \"" +textFieldUserName.getText()+"\" doesn´t exists");
        }
    }
}
