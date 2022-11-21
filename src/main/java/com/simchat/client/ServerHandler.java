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

/**
 * Manage client-server communication, send messages (Message object), set their parameters according to demand from GUI.
 * Locally store user information data (friend list, messages between user and friend) from server.
 * Wake up GUI threads, if needed. Listen on socket for server -> client communication. Run in own thread.
 */
public class ServerHandler extends AbstractNetworkHandler implements Runnable{

    /**
     * Logged user. Obtained from GUI and verified to server.
     */
    private String clientUsername;

    private VBox vBoxRecieve;  //TODO Probably unused class variable. Check it, test it, get rid of it if not needed

    /**
     * Friend list of logged user. This is "JavaFX object" (defined in fxml), passed as parameter from ControllerUserWindow.
     * Used to actualize GUI, if new message received from server.
     */
    private ListView<String> listViewFriendList;

    /**
     * GUI thread, which is invoking some of ServerHandler functionality and waiting for woke up from ServerHandler thread.
     */
    private Object GUIThread;

    /**
     * Friends of currently logged user. Locally stored in RAM. Obtained from server if needed. Used in GUI for
     * showing friendlist. Hash map used because value (integer) for key (firend) is used to store information
     * how many messages was received from server (on background), but was not shown in GUI, because this friend
     * is not selected in GUI.
     */
    private HashMap<String,Integer> friendList;

    /**
     * Variables used as holder for response from server on specific request from GUI (log user, sign up user,
     * add friend).
     */
    private boolean logged;
    private boolean signedUp;
    private boolean addedFriend;

    /**
     * Use to inform that some request (client-server) communication was done and data was received from server.
     * Used as checking parameter in GUI thread, which is waiting for response from server.
     */
    private boolean processedRequest;

    /**
     * Messages between this user and friends. Locally stored in RAM. Obtained from server if needed. Used in GUI for
     * showing messages.
     */
    private HashMap<String,ArrayList<Message>> messageList;

    /**
     * Init socket for communication. Set default values if logged client and his messagelist.
     */
    public ServerHandler() throws IOException {
        this.logged=false;
        initSocketAndStreams();
        messageList=new HashMap<>();
    }

    /**
     * Main method of thread. Called from ClientMain (thread.start()). Listening on initialized socket. Listening for
     * Message object, get it´s type and according type (type of server response) call relevant method.
     */
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
                    case RETURN_FRIENDLIST: receiveFriendList(message); break;
                    case STANDARD_MESSAGE: receiveMessage(message); break;
                    case RETURN_MESSAGES_BETWEEN_USERS: receiveMessagesBetweenUsers(message); break;
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

    /**
     * Receive messages between this user and some friend from server and store locally.
     * Wake up GUI thread waiting for response.
     * @param message Received message from server.
     */
    private void receiveMessagesBetweenUsers(Message message) {
        messageList.put(message.getToUser(),message.getListOfMessages());
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    /**
     * Receive message from some user, parse it´s data and store them locally. If user not known, update friendlist.
     * Store data to hashmap messageList. Update GUI friendlist (if user not known or not selected now (so message
     * will not be shown in GUI message window (vbox) and it´s needed to inform in GUI, that new message from other user
     * was received)). Update GUI message window (vbox), if user who is this message from, is now selected in GUI.
     * @param message Received message from server.
     */
    private void receiveMessage(Message message) throws IOException {
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
            //TODO Platform.runlater shouldnt be necessary here. Check it, test it, get rid of it if not needed
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
                ((ControllerUserWindow) GUIThread).showReceivedMessage(messageRecieved,timeStamp);
            }
        }
        else{
            friendList.put(fromUser,friendList.get(fromUser)+1);

            //TODO change color of listcell when messages rising ->cellfactory or something
            //--------------- this is workaround (hotfix)  notification
            for (int i=0; i< listViewFriendList.getItems().size(); i++){
                String friend = listViewFriendList.getItems().get(i);
                int indexOfBrace = friend.indexOf("]");
                if(indexOfBrace!=-1){
                    friend = friend.substring(indexOfBrace+1+1,friend.length()); //another +1 for blank space
                }
                if (friend.equals(fromUser)&& friendList.get(fromUser)<10){
                    String finalFromUser1 = fromUser;
                    int finalIndex = i;
                    //TODO Platform.runlater shouldn´t be necessary here. Check it, test it, get rid of it if not needed
                    Platform.runLater(()->listViewFriendList.getItems().set(finalIndex,"[+"+friendList.get(finalFromUser1)+"] "+ finalFromUser1));
                }
            }
        }
        //----------- end of workaround is workaround (hotfix)  notification

    }

    //TODO Probably unused method. Check it, test it, get rid of it if not needed
    public void setvBoxReceive(VBox vBoxRecieve) {
        this.vBoxRecieve = vBoxRecieve;
    }

    /**
     * Set GUI friendlist listview to local friendlist listview for further updating this GUI element in this class.
     * @param listViewFriendList GUI list with which will be manipulated later.
     */
    public void setListViewFriendList(ListView<String> listViewFriendList) {
        this.listViewFriendList = listViewFriendList;
    }

    /**
     * Set response from server to AddFriend request and wake up waiting GUI thread.
     * @param message Received message from server.
     */
    private void addFriend(Message message) {
        addedFriend = message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    /**
     * Receive friendlist of this user from server and store it locally. Check if something in friendlist.
     * Initialize messagelist. Wake up waiting GUI thread.
     * @param message Received message from server.
     */
    private void receiveFriendList(Message message) {
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

    /**
     * Set response from server to SignUp user request and wake up waiting GUI thread
     * @param message Received message from server
     */
    private void signUp(Message message) {
        signedUp=message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    /**
     * Set response from server to Login user request and wake up waiting GUI thread
     * @param message Received message from server
     */
    private void logInClient(Message message) {
        logged=message.isServerResponse();
        synchronized (GUIThread) {
            processedRequest=true;
            GUIThread.notify();
        }
    }

    /**
     * Send message given from GUI to server. If its standard message (messages between users) store it locally too
     * @param message Received message from server
     */
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
        if(message.getMessageType()==MessageType.STANDARD_MESSAGE){//save message to local messages
            messageList.get(message.getToUser()).add(message);
        }
    }

    /**
     * Put user in hashmap messagelist with "null" messages with this user
     * @param messages
     * @param user user who will be putted in local hashmap massagelist
     */
    //TODO get rid of this rrayList<Message> messages, if not needed
    public void putInMessageList(String user, ArrayList<Message> messages){
        messageList.put(user,null);
    }

    /**
     * @return information, if user was added to friendlist from previous request to add friend
     */
    public boolean isAddedFriend() {
        return addedFriend;
    }

    /**
     * @return locally stored friendlist
     */
    public HashMap<String,Integer> getFriendList() {
        return friendList;
    }

    /**
     * @return information, if user was successfully logged from previous request to log in
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * @return information, if user was successfully signedUp from previous request to sign up
     */
    public boolean isSignedUp() {
        return signedUp;
    }

    /**
     * @return information, if request from GUI to server (trough this class) was precessed and information was
     * successfully sent to server and data was received.
     */
    public boolean isProcessedRequest() {
        return processedRequest;
    }

    /**
     *  Set parameter about processed request. Set in GUI classes to false before some request, which sleeps GUI thread,
     *  is sent to server trough this class. Set in this class to true after receiving data from server.
     * @param processedRequest set false before blocking communication to server from some Controller.
     */
    public void setProcessedRequest(boolean processedRequest) {
        this.processedRequest = processedRequest;
    }

    /**
     *  Set GUI thread object locally, to know, which thread should be woken up after client-server request is processed.
     * @param GUIThread GUI thread which shuld be localy stored to be able to be woken up from this thread.
     */
    public void setGUIThread(Object GUIThread) {
        this.GUIThread = GUIThread;
    }

    /**
     *  Set logged client username.
     * @param clientUsername client username.
     */
    public void setClientUsername(String clientUsername) {
       this.clientUsername= clientUsername;
    }

    /**
     * @return messages between logged user (this user) and selected user (friend).
     * @param selectedFriend which messages should be returned.
     */
    public ArrayList<Message> getLocalMessagesBetweenUsers(String selectedFriend) {
        return this.messageList.get(selectedFriend);
    }

    /**
     * @return currently logged user
     */
    public String getClientUsername() {
        return clientUsername;
    }
}
