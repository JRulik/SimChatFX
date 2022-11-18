package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.simchat.client.ClientMain.serverHandler;
import static com.simchat.client.ClientMain.stageCreator;

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
    private LocalDateTime lastRecieveMessageTimeStamp;
    private LocalDateTime lastSendMessageTimeStamp;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverHandler.setGUIThread(this);
        serverHandler.setListViewFriendList(listViewFriendList);
        serverHandler.setvBoxRecieve(vBoxRecieve);
        setUsername(serverHandler.getClientUsername());

        vBoxRecieve.heightProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) ->{
                scrollPaneRecieve.layout();
                scrollPaneRecieve.setVvalue((Double) newValue);
        });

        friendlistRefresh();
        listViewFriendList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observableValue, String s, String t1)-> {
                    if ((s == null  || !s.equals(t1))&& t1!=null) {
                        selectedFriend = listViewFriendList.getSelectionModel().getSelectedItem();
                        labelSelectedFriend.getStyleClass().add("labelSelectedFriend");
                        labelSelectedFriend.setText("  " + selectedFriend);
                        Platform.runLater(() -> {
                            textAreaSend.requestFocus();
                            messageWindowRefresh();
                        });
                    }
              });
        textAreaSend.setOnKeyPressed(keyEvent->{
            if (keyEvent.getCode()== KeyCode.ENTER){
                //this doesn´t work because even bubbling, "\n" is catched faster then this handler gets called
                //keyEvent.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
                if (keyEvent.isShiftDown()) {
                    textAreaSend.appendText(System.getProperty("line.separator"));
                } else {
                    textAreaSend.setText(textAreaSend.getText().substring(0, textAreaSend.getText().length() - 2));//delete \n from string
                    butttonSendAction(new ActionEvent());
                }
            }
        });

          }

    @FXML
    protected void butttonSendAction(ActionEvent e) {
        if(selectedFriend==null){
            return;
        }
        String messageToSend =textAreaSend.getText();
        if (!messageToSend.isEmpty()) {
            LocalDateTime timeStamp = LocalDateTime.now();
            Message message = new Message(MessageType.STANDART_MESSAGE, username,
                    selectedFriend, timeStamp, messageToSend);
            showSendMessage(messageToSend,timeStamp);
            serverHandler.sendMessage(message);
            textAreaSend.clear();
            textAreaSend.requestFocus();//to lost focus from button, back to textArea
        }
    }

    @FXML
    protected void buttonAddFriendAction(ActionEvent e)  {
        Stage stage = stageCreator.createStage("AddFriend-view.fxml","icon.png","styles.css"
                ,"SimChatFX - Add Friend");
        stage.initModality(Modality.APPLICATION_MODAL);;
        stage.showAndWait();
        serverHandler.setGUIThread(this);   //put back this as GUI thread in for server (for next lisrefresh)
        friendlistRefresh();
    }

    protected void friendlistRefresh() {
        String selectedFreindLocal =  listViewFriendList.getSelectionModel().getSelectedItem();
        listViewFriendList.getItems().clear();
        //if frienlist is not inicialized or inicialized with empty string (user with no friends) ask server (again)
        if (serverHandler.getFriendList()==null || serverHandler.getFriendList().get(0).equals("")) {
            Message message = new Message(MessageType.RETURN_FRIENDLIST);
            serverHandler.setProcessedRequest(false);
            serverHandler.sendMessage(message);
            serverHandler.setGUIThread(this);
            //wait till process is not serviced by serverHandler
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
        }
        ArrayList<String> friends = serverHandler.getFriendList();
        if (friends!=null  && !friends.get(0).equals("")){//check if there is only empty row in returned data from database
            listViewFriendList.getItems().addAll(friends);
        }
        if(selectedFreindLocal!=null){//select friend who was selected before clearing listView
            selectedFriend=selectedFreindLocal;
            listViewFriendList.getSelectionModel().select(selectedFriend);
        }
    }

    protected void messageWindowRefresh(){
        vBoxRecieve.getChildren().clear();
        if(serverHandler.getLocalMessagesBetweenUsers(selectedFriend)== null) { //request messages from server if no local memmory
            Message message = new Message(MessageType.RETURN_MESSAGES_BETWEEN_USERS,this.username,selectedFriend);
            serverHandler.setProcessedRequest(false);
            serverHandler.setGUIThread(this);
            serverHandler.sendMessage(message);
            //wait till process is not serviced by serverHandler
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
        }
        //show messages on massageWindow
        lastSendMessageTimeStamp = null;
        lastRecieveMessageTimeStamp = null;
        if(serverHandler.getLocalMessagesBetweenUsers(selectedFriend)!= null) { //if even on server 0 messages between, do nothing
            for (Message msg : serverHandler.getLocalMessagesBetweenUsers(selectedFriend)) {
                if (msg.getFromUser().equals(this.username)) {
                    showSendMessage(msg.getMessage(),msg.getCreatedTime());
                } else {
                    showRecievedMessage(msg.getMessage(),msg.getCreatedTime());
                }
            }
        }
    }

    public void showTimeStampBetweenMessages(Pos position,LocalDateTime timeStamp){
        HBox hBox = new HBox();
        hBox.setAlignment(position);
        Text text = new Text(timeStamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")));
        text.setId("text_font_color_grey");
        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("textflow_timeStamp");
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> vBoxRecieve.getChildren().add(hBox));
    }

    public void showSendMessage(String messageSend, LocalDateTime messageTime){
        if (lastSendMessageTimeStamp==null ||
                Duration.between(lastSendMessageTimeStamp.truncatedTo(ChronoUnit.MINUTES),
                        messageTime.truncatedTo(ChronoUnit.MINUTES)).toMinutes()>=1){
                lastSendMessageTimeStamp = messageTime;
                 showTimeStampBetweenMessages(Pos.CENTER_RIGHT,lastSendMessageTimeStamp);
        }
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getStyleClass().add("hbox_send");
        Text text = new Text(messageSend);
        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("textflow_send");
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> vBoxRecieve.getChildren().add(hBox));
    }

    public void showRecievedMessage(String messageRecieved, LocalDateTime messageTime){
        if (lastRecieveMessageTimeStamp==null ||
                Duration.between(lastRecieveMessageTimeStamp,messageTime).toMinutes()>=1){
            lastRecieveMessageTimeStamp = messageTime;
            showTimeStampBetweenMessages(Pos.CENTER_LEFT,lastRecieveMessageTimeStamp);
        }
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("hbox_recieve");
        Text text = new Text(messageRecieved);
        text.setId("text_font_color_white");
        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("textflow_recieve");
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> vBoxRecieve.getChildren().add(hBox));
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
