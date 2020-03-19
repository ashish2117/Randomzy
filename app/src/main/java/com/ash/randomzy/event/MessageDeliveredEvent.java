package com.ash.randomzy.event;

public class MessageDeliveredEvent {
    private String messageId;
    private String userId;

    public MessageDeliveredEvent(String messageId, String userId){
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
