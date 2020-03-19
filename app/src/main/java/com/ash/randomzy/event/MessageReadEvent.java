package com.ash.randomzy.event;

public class MessageReadEvent {
    private String messageId;
    private String userId;

    public MessageReadEvent(String messageId, String userId){
        this.messageId =  messageId;
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getUserId() {
        return userId;
    }
}
