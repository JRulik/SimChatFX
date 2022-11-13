package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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

    private String username;

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
        textFlowRecieve.requestFocus();//aby se ztratil focus po odjeti z tlacitka po kliku
    }
    @FXML
    protected void buttonAddFriendAction(ActionEvent e) throws IOException {
        System.out.println(getClass());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AddFriend-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("SimChatFX - Add Friend");
        String css = this.getClass().getResource("styles.css").toExternalForm();
        //Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
        //stage.getIcons().add(icon);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.setResizable(false);
        stage.showAndWait();
        //stage.show();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
