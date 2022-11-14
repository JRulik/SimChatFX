package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ClientControllerUserWindow extends AbstractNetworkHandler implements Initializable {
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

        vBoxRecieve.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPaneRecieve.layout();
                scrollPaneRecieve.setVvalue((Double) newValue);
            }
        });

        //textFlowRecieve.setBackground(Background.fill(Color.WHITE));// funguje

        listViewRefresh();
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

    protected void listViewRefresh(){
        listViewFriendList.getItems().clear();
        Message message = new Message(MessageType.RETURNFRIENDLIST);
        try {
            objectOutputStream.writeObject(message);
            message = (Message) objectInputStream.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String[] friends = message.getMessage().split("\\n");
        if (!friends[0].equals("")){
            listViewFriendList.getItems().addAll(friends);
        }
    }

    @FXML
    protected void butttonSendAction(ActionEvent e){
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
                    labelSelectedFriend.getText(), LocalDateTime.now(), messageToSend);
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
