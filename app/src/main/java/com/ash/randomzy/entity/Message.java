package com.ash.randomzy.entity;

import com.google.gson.Gson;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message implements Serializable {

    @PrimaryKey
    @NonNull
    private String messageId;

    @ColumnInfo(name = "sentBy")
    private String sentBy;

    @ColumnInfo(name = "sentTo")
    private String sentTo;

    @ColumnInfo(name = "timeStamp")
    private long timeStamp;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "messageType")
    private int messageType;

    @ColumnInfo(name = "messageStatus")
    private int messageStatus;

    @ColumnInfo(name = "extras")
    private String extras;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    @Override
    public String toString(){
        return new Gson().toJson(this);
    }
}
