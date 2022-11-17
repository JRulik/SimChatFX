package com.simchat.shared.dataclasses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Message implements Serializable {


    private MessageType messageType;
    private boolean serverResponse;

    private ArrayList<Message> listOfMessages;
    private String fromUser;
    private String toUser;
    private LocalDateTime createdTime;
    private String message;

    public ArrayList<Message> getListOfMessages() {
        return listOfMessages;
    }

    public Message(MessageType messageType, String fromUser, String toUser, ArrayList<Message> listOfMessages) {
        this.messageType = messageType;
        this.listOfMessages = listOfMessages;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public Message(MessageType messageType, String fromUser, String toUser) {
        this.messageType = messageType;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public Message(MessageType messageType, boolean serverResponse) {
        this.messageType = messageType;
        this.serverResponse = serverResponse;
    }

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, String fromUser, String toUser, LocalDateTime createdTime, String message) {
        this.messageType = messageType;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.createdTime = createdTime;
        this.message = message;
    }

    @Override
    public String toString(){
        return "["+fromUser+";"+toUser+";"+createdTime+";"+message+"]";
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public String getMessage() {
        return message;
    }

    public boolean isServerResponse() {
        return serverResponse;
    }

}
