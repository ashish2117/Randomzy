package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

public class MessageOutGoingEvent {

    private Message message;

    public MessageOutGoingEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
