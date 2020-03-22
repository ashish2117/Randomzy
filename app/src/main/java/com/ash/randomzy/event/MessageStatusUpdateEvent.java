package com.ash.randomzy.event;

import com.ash.randomzy.model.MessageStatusUpdate;

public class MessageStatusUpdateEvent {

    private MessageStatusUpdate messageStatusUpdate;
    private String originator;

    public MessageStatusUpdateEvent(MessageStatusUpdate messageStatusUpdate, String originator){
        this.messageStatusUpdate = messageStatusUpdate;
        this.originator = originator;
    }

    public MessageStatusUpdate getMessageStatusUpdate(){
        return messageStatusUpdate;
    }

    public String getOriginator() {
        return originator;
    }
}
