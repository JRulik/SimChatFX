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
import java.util.ResourceBundle;

import static com.simchat.client.ClientMain.serverHandler;

public class ControllerUserWindow extends AbstractNetworkHandler implements Initializable {
    @FXML
    private Button buttonAddFriend;
    @FXML
    private Button buttonSend;
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

        vBoxRecieve.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPaneRecieve.layout();
                scrollPaneRecieve.setVvalue((Double) newValue);
            }
        });

        //textFlowRecieve.setBackground(Background.fill(Color.WHITE));// funguje


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
                  Platform.runLater(()->textAreaSend.requestFocus());
              }
          }
        );
    }

    protected void listViewRefresh() throws IOException {
        listViewFriendList.getItems().clear();
        Message message = new Message(MessageType.RETURNFRIENDLIST);
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
        String[] friends = serverHandler.getFriends();
        if (friends!=null  && !friends[0].equals("")){//kontrola pokud v tabulce neni zadny radek
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
            Message message = new Message(MessageType.STANDARTMESSAGE, username,
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
        stage.showAndWait();
        //stage.show();

        serverHandler.setGUIThread(this);   //for listViewRefresh, to serverhanlder could notify right thread
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
