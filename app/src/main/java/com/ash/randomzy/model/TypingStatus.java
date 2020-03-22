package com.ash.randomzy.model;

import com.ash.randomzy.constants.MessageTypes;

public class TypingStatus {

    private String userId;
    private int messageType;

    public TypingStatus() {
        this.messageType = MessageTypes.MESSAGE_TYPE_TYPING;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public int getMessageType() {
        return messageType;
    }
}
