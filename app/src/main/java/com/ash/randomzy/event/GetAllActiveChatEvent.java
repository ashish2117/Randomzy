package com.ash.randomzy.event;

import com.ash.randomzy.entity.ActiveChat;

import java.util.List;

public class GetAllActiveChatEvent {

    private List<ActiveChat> activeChatList;

    public GetAllActiveChatEvent(List<ActiveChat> activeChatList) {
        this.activeChatList = activeChatList;
    }

    public List<ActiveChat> getActiveChatList() {
        return activeChatList;
    }
}
