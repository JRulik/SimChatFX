package com.simchat.database;

import java.time.LocalDateTime;

public class Message {

    private String from;
    private String to;
    private LocalDateTime createdTime;
    private String message;

    public Message(String from, String to, LocalDateTime createdTime, String message) {
        super();
        this.from = from;
        this.to = to;
        this.createdTime = createdTime;
        this.message = message;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
