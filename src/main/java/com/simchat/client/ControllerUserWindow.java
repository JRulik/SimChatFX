package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.simchat.client.ClientMain.serverHandler;

public class ControllerUserWindow extends AbstractNetworkHandler implements Initializable {
    @FXML
    private TextArea textAreaSend;
    @FXML
    private VBox vBoxRecieve;
    @FXML
    private ScrollPane scrollPaneRecieve;
    @FXML
    private ListView<String> listViewFriendList;
    @FXML
    private Label labelSelectedFriend;
    @FXML
    private Label labelUsername;
    private String username;
    private String selectedFriend;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverHandler.setGUIThread(this);
        serverHandler.setListViewFriendList(listViewFriendList);
        serverHandler.setvBoxRecieve(vBoxRecieve);
        setUsername(serverHandler.getClientUsername());

        vBoxRecieve.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPaneRecieve.layout();
                scrollPaneRecieve.setVvalue((Double) newValue);
            }
        });

        try {
            listViewRefresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listViewFriendList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
              @Override
              public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                  selectedFriend = listViewFriendList.getSelectionModel().getSelectedItem();
                  //textFlowRecieve.getChildren().clear();
                  //textFlowRecieve.getChildren().add(new Text(selectedFriend));
                  labelSelectedFriend.getStyleClass().add("labelSelectedFriend");
                  labelSelectedFriend.setText("  "+selectedFriend);
                  //textAreaSend.setFocusTraversable(true);
                  Platform.runLater(()-> {
                      textAreaSend.requestFocus();
                      vBoxRecieveRefresh();
                  });
              }
          }
        );
    }

    protected void vBoxRecieveRefresh(){
        vBoxRecieve.getChildren().clear();
        if(serverHandler.getLocalMessagesBetweenUsers(selectedFriend)== null) {
            Message message = new Message(MessageType.RETURN_MESSAGES_BETWEEN_USERS,this.username,selectedFriend);
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
            //TODO catch this exception -> dont knwo, mby turn off everything
            try {
                serverHandler.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            synchronized (this) {
                while (!serverHandler.isProcessedRequest()) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        if(serverHandler.getLocalMessagesBetweenUsers(selectedFriend)!= null) {
            for (Message msg : serverHandler.getLocalMessagesBetweenUsers(selectedFriend)) {
                if (msg.getFromUser().equals(this.username)) {
                    HBox hBox = new HBox();
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.getStyleClass().add("hbox_send");
                    Text text = new Text(msg.getMessage());
                    TextFlow textFlow = new TextFlow(text);
                    textFlow.getStyleClass().add("textflow_send");
                    hBox.getChildren().add(textFlow);
                    vBoxRecieve.getChildren().add(hBox);
                } else {
                    HBox hBox = new HBox();
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.getStyleClass().add("hbox_recieve");
                    Text text = new Text(msg.getMessage());
                    text.setId("text_font_color_white");
                    TextFlow textFlow = new TextFlow(text);
                    textFlow.getStyleClass().add("textflow_recieve");
                    hBox.getChildren().add(textFlow);
                    vBoxRecieve.getChildren().add(hBox);
                }
            }
        }



    }

    protected void listViewRefresh() throws IOException {
        listViewFriendList.getItems().clear();
        Message message = new Message(MessageType.RETURN_FRIENDLIST);
        serverHandler.setProcessedRequest(false);
        serverHandler.sendMessage(message);

        serverHandler.setGUIThread(this);
        synchronized (this) {
            while (!serverHandler.isProcessedRequest()) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        ArrayList<String> friends = serverHandler.getFriendList();
        if (friends!=null  && !friends.get(0).equals("")){//kontrola pokud v tabulce neni zadny radek
            listViewFriendList.getItems().addAll(friends);
        }
    }

    @FXML
    protected void butttonSendAction(ActionEvent e) throws IOException {

        if(selectedFriend==null){
            return;
        }

        String messageToSend =textAreaSend.getText();
        if (!messageToSend.isEmpty()) {

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.getStyleClass().add("hbox_send");
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.getStyleClass().add("textflow_send");
            hBox.getChildren().add(textFlow);
            vBoxRecieve.getChildren().add(hBox);

            //TODO for server communication
            Message message = new Message(MessageType.STANDART_MESSAGE, username,
                    selectedFriend, LocalDateTime.now(), messageToSend);
            serverHandler.sendMessage(message);

            //textFlowRecieve.getChildren().add(new Text(textAreaSend.getText()));
            textAreaSend.clear();
            textAreaSend.requestFocus();//aby se ztratil focus po odjeti z tlacitka po kliku
        }


    }
    @FXML
    protected void buttonAddFriendAction(ActionEvent e) throws IOException {
        System.out.println(getClass());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddFriend-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("SimChatFX - Add Friend");
        String css = this.getClass().getResource("styles.css").toExternalForm();
        Image icon = new Image(ClientMain.class.getResourceAsStream("icon.png"));
        stage.getIcons().add(icon);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.setResizable(false);
        ControllerAddFriend controller = fxmlLoader.getController();
        controller.setUsername(username);
        stage.showAndWait();
        //stage.show();

        serverHandler.setGUIThread(this);   //for listViewRefresh, to serverhanlder could notify right thread
        //TODO without using database, only serverHandler loocal ListView<String> listViewFriendList
        listViewRefresh();
        //textFlowRecieve.requestFocus();//aby se ztratil focus po odjeti z tlacitka po kliku
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        setLabelUsername(username);
    }

    public void setLabelUsername(String username) {
        labelUsername.setText(username+"  ");
    }
}
