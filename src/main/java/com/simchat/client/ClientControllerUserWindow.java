package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientControllerUserWindow extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Button buttonAddFriend;
    @FXML
    private Button buttonSend;
    @FXML
    private TextFlow textArea;
    @FXML
    private TextFlow textFlowRecieve;
    @FXML
    private ScrollPane scrollPaneRecieve;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
            textFlowRecieve.getChildren().addListener((ListChangeListener<Node>) ((change) -> {
                textFlowRecieve.layout();
                scrollPaneRecieve.layout();
                scrollPaneRecieve.setVvalue(1.0f);
            }));
        //textFlowRecieve.setBackground(Background.fill(Color.WHITE));      //funguje
    }

    @FXML
    protected void butttonSendAction(ActionEvent e){

    }
    @FXML
    protected void buttonAddFriendAction(ActionEvent e){

    }
}
