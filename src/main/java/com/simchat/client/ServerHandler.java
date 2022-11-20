package com.simchat.client;

import com.simchat.shared.dataclasses.AbstractNetworkHandler;
import com.simchat.shared.dataclasses.Message;
import com.simchat.shared.dataclasses.MessageType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static com.simchat.client.ClientMain.serverHandler;

public class ServerHandler extends AbstractNetworkHandler implements Runnable{
    private String clientUsername;
    private VBox vBoxRecieve;
    private ListView<String> listViewFriendList;
    private Object GUIThread;
    private HashMap<String,Integer> friendList;
    private boolean logged;
    private boolean signedUp;
    private boolean addedFriend;
    private boolean processedRequest;
    private HashMap<String,ArrayList<Message>> messageList;

    public ServerHandler() throws IOException {
        this.logged=false;
        initSocketAndStreams();
        messageList=new HashMap<>();
    }
    @Override
    public void run() {
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
                    default:
                }
            } catch (IOException|ClassNotFoundException e) {
                closeEverything();
                System.out.println("[Error] -Server communication error!");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void recieveMessagesBetweenUsers(Message message) {
        messageList.put(message.getToUser(),message.getListOfMessages());
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }


    private void recieveMessage (Message message) throws IOException {
        String fromUser, toUser, messageRecieved;
        fromUser = message.getFromUser();
        toUser = message.getToUser();
        messageRecieved = message.getMessage();
        LocalDateTime timeStamp = message.getCreatedTime();

        if (fromUser.equals(clientUsername)){ //if msg from this user, set fromUser for next commnads
            fromUser=toUser;
        }
        if(!friendList.containsKey(fromUser)){//if fromUser not in friendlist
            String finalFromUser = fromUser;
            friendList.put(fromUser,0);
            messageList.put(fromUser, null);
            //Platform.runLater(()->listViewFriendList.getItems().add(finalFromUser));  //this dont show +1 incoming msg when new friend
            Platform.runLater(()->listViewFriendList.getItems().add("[+1] "+ finalFromUser)); //this is hotfix for problem above
        }
        if(messageList.get(fromUser) !=null){
            messageList.get(fromUser).add(message);
        }

        //Show message on screen if fromUser is selected in list
        if(listViewFriendList.getSelectionModel().getSelectedItem()!= null &&
                listViewFriendList.getSelectionModel().getSelectedItem().equals(fromUser)) {
            fromUser = message.getFromUser();//get variable fromUser back to correct value from message
            if (fromUser.equals(clientUsername)) {
                ((ControllerUserWindow) GUIThread).showSendMessage(messageRecieved,timeStamp);
            }
            else {
                ((ControllerUserWindow) GUIThread).showRecievedMessage(messageRecieved,timeStamp);
            }
        }
        else{
            friendList.put(fromUser,friendList.get(fromUser)+1);

            //TODO change color of listcell when messages rising ->cellfactory or something
            //this is workaround (hotfix)  notification
            for (int i=0; i< listViewFriendList.getItems().size(); i++){
                String friend = listViewFriendList.getItems().get(i);
                int indexOfBrace = friend.indexOf("]");
                if(indexOfBrace!=-1){
                    friend = friend.substring(indexOfBrace+1+1,friend.length()); //another +1 for blank space
                }
                if (friend.equals(fromUser)&& friendList.get(fromUser)<10){
                    String finalFromUser1 = fromUser;
                    int finalIndex = i;
                    Platform.runLater(()->listViewFriendList.getItems().set(finalIndex,"[+"+friendList.get(finalFromUser1)+"] "+ finalFromUser1));
                }
            }
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
        ArrayList<String> friendListFromDatabse= new ArrayList<String> (Arrays.asList(message.getMessage().split("\\n")));

        if (friendList != null) {
            for (String friend : friendListFromDatabse) {
                if (!friendList.containsKey(friend) && !friend.equals("")) {
                    friendList.put(friend, 0);
                    messageList.put(friend, null);
                }
            }
        }
        else{
            friendList= new HashMap<>();
            messageList=new HashMap<>();
            for (String friend : friendListFromDatabse) {
                if (!friend.equals("")) {
                    friendList.put(friend, 0);
                    messageList.put(friend, null);
                }
            }
        }

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


    protected void sendMessage(Message message)  {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection with server lost!", ButtonType.OK);
            alert.showAndWait();
            closeEverything();
            System.out.println("[Error] - Can´t send message to server!");
            e.printStackTrace();
            System.exit(0);
        }
        if(message.getMessageType()==MessageType.STANDART_MESSAGE){//save message to local messages
            messageList.get(message.getToUser()).add(message);
        }
    }

    public void putInMessageList(String user, ArrayList<Message> messages){
        messageList.put(user,null);
    }
    public boolean isAddedFriend() {
        return addedFriend;
    }

    public HashMap<String,Integer> getFriendList() {
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

    public String getClientUsername() {
        return clientUsername;
    }
}
