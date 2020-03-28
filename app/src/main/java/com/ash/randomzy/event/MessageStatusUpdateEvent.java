package com.ash.randomzy.event;

import com.ash.randomzy.model.MessageStatusUpdate;

public class MessageStatusUpdateEvent {

    private MessageStatusUpdate messageStatusUpdate;
    private String originator;
    private String activeChatId;

    public MessageStatusUpdateEvent(MessageStatusUpdate messageStatusUpdate, String originator, String activeChatId){
        this.messageStatusUpdate = messageStatusUpdate;
        this.originator = originator;
        this.activeChatId = activeChatId;
    }

    public MessageStatusUpdate getMessageStatusUpdate(){
        return messageStatusUpdate;
    }

    public String getOriginator() {
        return originator;
    }

    public String getActiveChatId() {
        return activeChatId;
    }
}
