package com.ash.randomzy.event;

import com.ash.randomzy.model.ActiveChat;

import java.util.List;

public class GetAllFavActiveChat {

    private List<ActiveChat> favActiveChatList;

    public GetAllFavActiveChat(List<ActiveChat> favActiveChatList) {
        this.favActiveChatList = favActiveChatList;
    }

    public List<ActiveChat> getFavActiveChatList() {
        return favActiveChatList;
    }
}
