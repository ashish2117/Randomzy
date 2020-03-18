package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

public class MessageReceiveEvent {


    private Message message;

    public MessageReceiveEvent(Message message){
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
