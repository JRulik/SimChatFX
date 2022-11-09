package com.simchat.shared.dataclasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {


    private MessageType messageType;
    private String fromUser;
    private String toUser;
    private LocalDateTime createdTime;
    private String message;

    public Message(MessageType messageType, String message) {
        this.messageType = messageType;
        this.fromUser = null;
        this.toUser = null;
        this.createdTime = null;
        this.message = message;
    }

    public Message(MessageType messageType, String fromUser, String toUser, LocalDateTime createdTime, String message) {
        this.messageType = messageType;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.createdTime = createdTime;
        this.message = message;
    }


    public Message(String from, String toUser, LocalDateTime createdTime, String message) {
        super();
        this.fromUser = from;
        this.toUser = toUser;
        this.createdTime = createdTime;
        this.message = message;
        messageType = MessageType.STANDARTMESSAGE;
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

}
