package com.ash.randomzy.event;

import com.ash.randomzy.entity.ActiveChat;

public class NewActiveChatEvent {

    private ActiveChat activeChat;

    public NewActiveChatEvent(ActiveChat activeChat){
        this.activeChat = activeChat;
    }

    public ActiveChat getActiveChat() {
        return activeChat;
    }
}
