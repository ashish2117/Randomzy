package com.ash.randomzy.event;

import com.ash.randomzy.model.MessageStatusUpdate;

public class MessageStatusUpdateEvent {

    private MessageStatusUpdate messageStatusUpdate;

    public MessageStatusUpdateEvent(MessageStatusUpdate messageStatusUpdate){
        this.messageStatusUpdate = messageStatusUpdate;
    }

    public MessageStatusUpdate getMessageStatusUpdate(){
        return messageStatusUpdate;
    }
}
