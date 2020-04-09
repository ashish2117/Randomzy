package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

public class ImageMessageDownloadedEvent {

    private Message message;

    public ImageMessageDownloadedEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
