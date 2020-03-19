package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

import java.util.List;

public class UnreadMessagesByUserEvent {

    private List<Message> messageList;

    public UnreadMessagesByUserEvent(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<Message> getMessageList() {
        return messageList;
    }
}
