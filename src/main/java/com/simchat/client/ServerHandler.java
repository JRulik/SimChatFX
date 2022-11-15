package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;

public class ServerHandler extends AbstractNetworkHandler implements Runnable{

    private String clientUsername;

    private VBox vBoxRecieve;
    private ListView<String> listViewFriendList;

    private Object GUIThread;

    private String[] friends;
    private boolean logged;
    private boolean signedUp;

    private boolean addedFriend;
    private boolean processedRequest;

    public ServerHandler(Object GUIThread) throws IOException {
        this.logged=false;
        initSocketAndStreams();
        this.GUIThread=GUIThread;
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
                    case LOGINMESSAGE: logInClient(message); break;
                    case SIGNUPMESSAGE: signUp(message); break;
                    case ADDFRIEND: addFriend(message); break;
                    case RETURNFRIENDLIST: recieveFriendList(message); break;
                    case STANDARTMESSAGE: recieveMessage(message); break;
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

    private void addFriend(Message message) {
        addedFriend = message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    private void recieveFriendList(Message message) {
        friends = message.getMessage().split("\\n");
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

    private void recieveMessage (Message message) throws IOException {
        String fromUser, toUser, messageRecieved;
        LocalDateTime createdTime = message.getCreatedTime();
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        messageRecieved = message.getMessage();

        //TODO UPDATE GUI INTERFACE
    }

    void sendMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }


    public boolean isAddedFriend() {
        return addedFriend;
    }

    public String[] getFriends() {
        return friends;
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


}
