package com.ash.randomzy.event;

public class MessageSentEvent {

    private String messageId;

    public MessageSentEvent(String messageId){
        this.messageId =  messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
