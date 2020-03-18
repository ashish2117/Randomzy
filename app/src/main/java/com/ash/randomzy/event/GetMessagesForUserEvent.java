package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

import java.util.List;

public class GetMessagesForUserEvent {

    private List<Message> messageList;

    public GetMessagesForUserEvent(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<Message> getMessageList() {
        return messageList;
    }
}
