package com.ash.randomzy.event;

import com.ash.randomzy.model.TypingStatus;

public class TypingEvent {

    private TypingStatus typingStatus;

    public TypingEvent(TypingStatus typingStatus) {
        this.typingStatus = typingStatus;
    }

    public TypingStatus getTypingStatus() {
        return typingStatus;
    }
}
