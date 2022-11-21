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
import java.util.HashMap;
import java.util.ResourceBundle;

import static com.simchat.client.ClientMain.serverHandler;
import static com.simchat.client.ClientMain.stageCreator;


/**
 * Controller of window "Main window". Have receive vbox with scroll pane for received messages, text area for
 * user input (message to send), two buttons ("AddFriend" and "Send") and listview with friendlist. Controls are
 * defined in "Main-view.fxml".
 */
public class ControllerUserWindow extends AbstractNetworkHandler implements Initializable {
    /**
     * JavaFX controls, defined in "LogIn-view.fxml", which are shown on stage. Some of their
     * attributes, as listeners, are also defined in "Main-view.fxml".
     */
    @FXML
    private TextArea textAreaSend;
    @FXML
    private VBox vBoxReceive;
    @FXML
    private ScrollPane scrollPaneReceive;
    @FXML
    private ListView<String> listViewFriendList;
    @FXML
    private Label labelSelectedFriend;
    @FXML
    private Label labelUsername;

    /**
     * Logged user. Obtained from severHandler
     */
    private String username;

    /**
     * User selected in list view (friend list) which messages are shown in vbox (receive window) and with which
     * can user now (after selecting) text.
     */
    private String selectedFriend;

    /**
     * Time stamp of last receive message shown in vbox (receive window). Used for writing time of message in GUI if
     * time from last (this time stamp) and new shown is at least >1 min.
     */
    private LocalDateTime lastReceiveMessageTimeStamp;

    /**
     * Time stamp of last send message shown in vbox (receive window). Used for writing time of message in GUI if
     * time from last (this time stamp) and new shown is at least >1 min.
     */
    private LocalDateTime lastSendMessageTimeStamp;

    /**
     * Initialize method called before stage is shown. Set serverHandler gui variable to this to
     * further manipulation with server-client communication (in synchronized part). Add listeners to GUI controls
     * which could not be set in SceneBuilder. Parameters are defined in Initializable interface
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverHandler.setGUIThread(this);
        serverHandler.setListViewFriendList(listViewFriendList);
        serverHandler.setvBoxReceive(vBoxReceive);
        setUsername(serverHandler.getClientUsername());

        vBoxReceive.heightProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) ->{
                scrollPaneReceive.layout();
                scrollPaneReceive.setVvalue((Double) newValue);
        });

        friendlistRefresh();
        listViewFriendList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observableValue, String s, String t1)-> {
                    if ((s == null  || !s.equals(t1))&& t1!=null) {
                        selectedFriend = listViewFriendList.getSelectionModel().getSelectedItem();

                        //TODO change color of listcell when messages rising ->cellfactory or something ->see serverhandler
                        //--------this is workaround (hotfix) cleanup notification
                        int indexOfSelectedFriend = listViewFriendList.getItems().indexOf(selectedFriend);
                        int indexOfBrace = selectedFriend.indexOf("]");
                        if(indexOfBrace!=-1){
                            selectedFriend = selectedFriend.substring(indexOfBrace+1+1,selectedFriend.length());//another +1 for blank space
                            listViewFriendList.getItems().set(indexOfSelectedFriend,selectedFriend);
                        }
                        //---------end of workaround
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
                //this doesn´t work because even bubbling, "\n" is catched faster than this handler gets called
                //keyEvent.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
                if (keyEvent.isShiftDown()) {
                    textAreaSend.appendText(System.getProperty("line.separator"));
                } else {
                    textAreaSend.setText(textAreaSend.getText().substring(0, textAreaSend.getText().length() -1));//delete \n from string
                    butttonSendAction(new ActionEvent());
                }
            }
        });
    }

    /**
     * Method bounded with control "Button buttonSend" called when button is pressed or when ENTER is pressed in textarea
     * (user input). Check text in textarea if isn´t blank and if not, send via serverHandler to server.
     * @param e ActionEvent which invoke this method
     */
    @FXML
    protected void butttonSendAction(ActionEvent e) {
        if(selectedFriend==null){
            return;
        }
        String messageToSend =textAreaSend.getText();
        if (!messageToSend.isEmpty()) {
            LocalDateTime timeStamp = LocalDateTime.now();
            Message message = new Message(MessageType.STANDARD_MESSAGE, username,
                    selectedFriend, timeStamp, messageToSend);
            showSendMessage(messageToSend,timeStamp);
            serverHandler.sendMessage(message);
            textAreaSend.clear();
            textAreaSend.requestFocus();//to lost focus from button, back to textArea
        }
    }

    /**
     * Method bounded with control "Button buttonAddFriend" called when button is pressed. Opens new GUI window
     * for adding user as friend and forwards control to ControllerAddFriend. Waits for this new window to close.
     * Refresh friendlist after close.
     * @param e ActionEvent which invoke this method
     */
    @FXML
    protected void buttonAddFriendAction(ActionEvent e)  {
        Stage stage = stageCreator.createStage("AddFriend-view.fxml","icon.png","styles.css"
                ,"SimChatFX - Add Friend");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        serverHandler.setGUIThread(this);   //put back this as GUI thread in for server (for next lisrefresh)
        friendlistRefresh();
    }

    /**
     * Refresh friendlist (list view on right side of GUI). Use serverHandler local friendlist
     * (if was initialized, otherwise ask server for friendlist) and use it to map friend to GUI friendlist.
     */
    protected void friendlistRefresh() {
        String selectedFriendLocal =  listViewFriendList.getSelectionModel().getSelectedItem();
        listViewFriendList.getItems().clear();
        //if frienlist is not initialized or initialized with empty string (user with no friends) ask server (again)
        if (serverHandler.getFriendList()==null) {
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
        HashMap<String,Integer> friends = serverHandler.getFriendList();
        if (friends!=null){//check if there is only empty row in returned data from database
            listViewFriendList.getItems().addAll(friends.keySet());
        }
        if(selectedFriendLocal!=null){//select friend who was selected before clearing listView
            selectedFriend=selectedFriendLocal;
            listViewFriendList.getSelectionModel().select(selectedFriend);
        }
    }

    /**
     * Refresh window (vbox) with messages in according to with user is selected in friendlist (listview) in GUI. Refresh
     * is done from local memory (serverHandler). If no messages between this user and selected user in local memmory
     * ,then it´s asked server for messages and messages are got from database
     */
    protected void messageWindowRefresh(){
        vBoxReceive.getChildren().clear();
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
        lastReceiveMessageTimeStamp = null;
        if(serverHandler.getLocalMessagesBetweenUsers(selectedFriend)!= null) { //if even on server 0 messages between, do nothing
            serverHandler.getFriendList().put(selectedFriend,0);
            for (Message msg : serverHandler.getLocalMessagesBetweenUsers(selectedFriend)) {
                if (msg.getFromUser().equals(this.username)) {
                    showSendMessage(msg.getMessage(),msg.getCreatedTime());
                } else {
                    showReceivedMessage(msg.getMessage(),msg.getCreatedTime());
                }
            }
        }
    }

    /**
     * Show time information about send/received message in message window (vbox)
     */
    public void showTimeStampBetweenMessages(Pos position,LocalDateTime timeStamp){
        HBox hBox = new HBox();
        hBox.setAlignment(position);
        Text text = new Text(timeStamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")));
        text.setId("text_font_color_grey");
        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("textflow_timeStamp");
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> vBoxReceive.getChildren().add(hBox));
    }

    /**
     * Show send message in message window (vbox). If duration between last showen send message and this one, which
     * is going to be shown, is more than 1 minute, show time information about message before show this message
     */
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
        Platform.runLater(() -> vBoxReceive.getChildren().add(hBox));
    }

    /**
     * Show received message in message window (vbox). If duration between last showen received message and this one, which
     * is going to be shown, is more than 1 minute, show time information about message before show this message
     */
    public void showReceivedMessage(String messageReceived, LocalDateTime messageTime){
        if (lastReceiveMessageTimeStamp ==null ||
                Duration.between(lastReceiveMessageTimeStamp,messageTime).toMinutes()>=1){
            lastReceiveMessageTimeStamp = messageTime;
            showTimeStampBetweenMessages(Pos.CENTER_LEFT, lastReceiveMessageTimeStamp);
        }
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("hbox_receive");
        Text text = new Text(messageReceived);
        text.setId("text_font_color_white");
        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add("textflow_receive");
        hBox.getChildren().add(textFlow);
        Platform.runLater(() -> vBoxReceive.getChildren().add(hBox));
    }


    /**
     * @return currently logged client username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set this user username and show it in label above message window in GUI
     */
    public void setUsername(String username) {
        this.username = username;
        setLabelUsername(username);
    }

    /**
     * Set label username in label above message window in GUI
     */
    public void setLabelUsername(String username) {
        labelUsername.setText(username+"  ");
    }
}
