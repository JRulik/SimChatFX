package com.simchat.shared.dataclasses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Used for client-server communication. This instances of this class are send over the network.
 * It´s used for special communication (log in, sign up, add friend) and standard communication (messages
 * between users)
 */
public class Message implements Serializable {

    /**
     * Type of message, used on both (client/server) sides to recognise what to do with this message.
     */
    private MessageType messageType;

    /**
     * flag set by server to client as response on specific request (as login, sign up, get friendlist etc.)
     */
    private boolean serverResponse;

    /**
     * used to transfer list of all messages between users stored on server side (in database) to client
     */
    private ArrayList<Message> listOfMessages;

    /**
     * User from whom this message is coming (used in standard communication)
     */
    private String fromUser;

    /**
     * User to whom is this message addressed (used in standard communication)
     */
    private String toUser;

    /**
     * Time of creation of this message (used in standard communication)
     */
    private LocalDateTime createdTime;

    /**
     * actual message from user (used in standard communication)
     */
    private String message;

    /**
     * Create message of specific parameters.
     * @param messageType Type of message (standard/sign up etc.)
     * @param fromUser User who this message create/ from whom this message is coming
     * @param toUser  User to whom is this message addressed
     * @param listOfMessages list of all messages between users
     */
    public Message(MessageType messageType, String fromUser, String toUser, ArrayList<Message> listOfMessages) {
        this.messageType = messageType;
        this.listOfMessages = listOfMessages;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    /**
     * Create message of specific parameters.
     * @param messageType Type of message (standard/sign up etc.)
     * @param fromUser User who this message create/ from whom this message is coming
     * @param toUser  User to whom is this message addressed
     */
    public Message(MessageType messageType, String fromUser, String toUser) {
        this.messageType = messageType;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    /**
     * Create message of specific parameters.
     * @param messageType Type of message (standard/sign up etc.)
     * @param message Useful data, message which is going to be sent
     */
    public Message(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    /**
     * Create message of specific parameters. Used on server side.
     * @param messageType Type of message (standard/sign up etc.)
     * @param serverResponse Response of server on previous received request (for example successfully added friend)
     */
    public Message(MessageType messageType, boolean serverResponse) {
        this.messageType = messageType;
        this.serverResponse = serverResponse;
    }

    /**
     * Create message of specific parameters. Used on client side (requesting special command from server, for example receive
     * friendlist)
     * @param messageType Type of message (standard/sign up etc.)
     */
    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Create message of specific parameters. Used in standard communication (messages between users)
     * @param messageType Type of message (standard/sign up etc.)
     * @param fromUser User who this message create/ from whom this message is coming
     * @param toUser  User to whom is this message addressed
     * @param createdTime Time of creation of this message
     * @param message Useful data, message which is going to be sent
     */
    public Message(MessageType messageType, String fromUser, String toUser, LocalDateTime createdTime, String message) {
        this.messageType = messageType;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.createdTime = createdTime;
        this.message = message;
    }

    /**
     * @return String representation of this class with only user important data (useful only for standard communication)
     */
    @Override
    public String toString(){
        return "["+fromUser+";"+toUser+";"+createdTime+";"+message+"]";
    }

    /**
     * @return Message type of this message
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @return User from whom this message is coming
     */
    public String getFromUser() {
        return fromUser;
    }

    /**
     * @return User to whom is this message addressed
     */
    public String getToUser() {
        return toUser;
    }

    /**
     * @return Time when this message was created
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * @return Message between two users
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return value of server response (on specific command previously received)
     */
    public boolean isServerResponse() {
        return serverResponse;
    }

    /**
     * @return get messages between users
     */
    public ArrayList<Message> getListOfMessages() {
        return listOfMessages;
    }

    /**
     * @return set message of this message
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
