package com.ash.randomzy.model;

import com.ash.randomzy.constants.MessageTypes;
import com.google.gson.Gson;

public class MessageStatusUpdate {

    private int messageType;
    private String messageId;
    private long timeStamp;
    private int messageStatus;
    private String userId;

    public MessageStatusUpdate(){
        this.messageType = MessageTypes.MESSAGE_STATUS_UPDATE;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString(){
       return new Gson().toJson(this);
    }
}
