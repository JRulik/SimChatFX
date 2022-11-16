package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ServerHandler extends AbstractNetworkHandler implements Runnable{


    private String clientUsername;

    private VBox vBoxRecieve;

    private ListView<String> listViewFriendList;
    private Object GUIThread;

    private ArrayList<String> friendList;
    private boolean logged;
    private boolean signedUp;

    private boolean addedFriend;
    private boolean processedRequest;

    private HashMap<String,ArrayList<Message>> messageList;



    public ServerHandler(Object GUIThread) throws IOException {
        this.logged=false;
        initSocketAndStreams();
        this.GUIThread=GUIThread;
        messageList=new HashMap<>();
    }

    /*
    public ServerHandler(String clientUsername, VBox vBoxRecieve, ListView<String> listViewFriendList) {
        this.clientUsername = clientUsername;
        this.vBoxRecieve = vBoxRecieve;
        this.listViewFriendList = listViewFriendList;
    }*/

    @Override
    public void run() {
        String messageFromClient;
        Message message;
        while (socket.isConnected()) {
            try {
                message = (Message) objectInputStream.readObject();
                MessageType typeOfMessage = message.getMessageType();
                switch (typeOfMessage) {
                    case LOGIN_MESSAGE: logInClient(message); break;
                    case SIGNUP_MESSAGE: signUp(message); break;
                    case ADD_FRIEND: addFriend(message); break;
                    case RETURN_FRIENDLIST: recieveFriendList(message); break;
                    case STANDART_MESSAGE: recieveMessage(message); break;
                    case RETURN_MESSAGES_BETWEEN_USERS: recieveMessagesBetweenUsers(message); break;
                    //TODO case on returnRecievedMessages
                    default: //TODO logged client
                }
            } catch (IOException e) {
                closeEverything();
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void recieveMessagesBetweenUsers(Message message) {
        //TODO naplnit lokal pole se zrpavami
        messageList.put(message.getToUser(),message.getListOfMessages());
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }


    private void recieveMessage (Message message) throws IOException {
        String fromUser, toUser, messageRecieved;
        LocalDateTime createdTime = message.getCreatedTime();
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        messageRecieved = message.getMessage();


        if(!listViewFriendList.getItems().contains(fromUser)){
            Platform.runLater(()->listViewFriendList.getItems().add(fromUser));
        }
        if(!messageList.containsKey(fromUser)){
            messageList.put(fromUser, new ArrayList<Message>());
        }
        messageList.get(fromUser).add(message);

        if(listViewFriendList.getSelectionModel().getSelectedItem()!= null &&
                listViewFriendList.getSelectionModel().getSelectedItem().equals(fromUser)) {
            //SHOW
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
        else{
            //TODO UPDATE GUI THAT OTHER USER SEND YOU MSG (+1 to that row in tab or something)
        }

    }

    public void setvBoxRecieve(VBox vBoxRecieve) {
        this.vBoxRecieve = vBoxRecieve;
    }

    public void setListViewFriendList(ListView<String> listViewFriendList) {
        this.listViewFriendList = listViewFriendList;
    }

    private void addFriend(Message message) {
        addedFriend = message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    private void recieveFriendList(Message message) {
        friendList = new ArrayList<String> (Arrays.asList(message.getMessage().split("\\n")));
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    private void signUp(Message message) {
        signedUp=message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    private void logInClient(Message message) {
        logged=message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }



    void sendMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
        if(message.getMessageType()==MessageType.STANDART_MESSAGE){
            messageList.get(message.getToUser()).add(message);
        }
    }


    public boolean isAddedFriend() {
        return addedFriend;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public boolean isLogged() {
        return logged;
    }

    public boolean isSignedUp() {
        return signedUp;
    }

    public boolean isProcessedRequest() {
        return processedRequest;
    }

    public void setProcessedRequest(boolean processedRequest) {
        this.processedRequest = processedRequest;
    }

    public void setGUIThread(Object GUIThread) {
        this.GUIThread = GUIThread;
    }

    public void setClientUsername(String clientUsername) {
       this.clientUsername= clientUsername;
    }

    public ArrayList<Message> getLocalMessagesBetweenUsers(String selectedFriend) {
        return this.messageList.get(selectedFriend);
    }
}
