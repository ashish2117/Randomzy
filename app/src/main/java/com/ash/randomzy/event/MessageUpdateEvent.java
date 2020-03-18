package com.ash.randomzy.event;

import com.ash.randomzy.model.MessageUpdate;

public class MessageUpdateEvent {

    private MessageUpdate messageUpdate;

    public MessageUpdateEvent(MessageUpdate messageUpdate){
        this.messageUpdate = messageUpdate;
    }

    public MessageUpdate getMessageUpdate(){
        return messageUpdate;
    }
}
