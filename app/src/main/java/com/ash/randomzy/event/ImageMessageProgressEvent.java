package com.ash.randomzy.event;

import com.ash.randomzy.entity.Message;

public class ImageMessageProgressEvent {

    private Message message;
    private int progress;

    public ImageMessageProgressEvent(Message message, int progress) {
        this.message = message;
        this.progress = progress;
    }

    public Message getImageMessage() {
        return message;
    }

    public int getProgress() {
        return progress;
    }
}
