package com.ash.randomzy.repository;

import android.content.Context;

import com.ash.randomzy.db.RandomzyDatabase;
import com.ash.randomzy.model.ActiveChat;

import java.util.List;

public class ActiveChatRepository {

    private final String DBNAME = "dandomzy-db";

    private RandomzyDatabase randomzyDatabase;

    public ActiveChatRepository(Context context){
        randomzyDatabase = RandomzyDatabase.getInstance(context);
    }

    public int deleteActiveChat(String id){
        return randomzyDatabase.activeChatDao().deleteActiveChat(id);
    }

    public List<ActiveChat> getAllActiveChats(){
        return randomzyDatabase.activeChatDao().getAll();
    }

    public ActiveChat getActiveChat(String id){
        return randomzyDatabase.activeChatDao().getActiveChat(id);
    }

    public List<ActiveChat> getFavActiveChats(){
        return randomzyDatabase.activeChatDao().getFavActiveChats();
    }

    public int deleteAllActiveChats(){
        return randomzyDatabase.activeChatDao().deleteAllActiveChats();
    }

    public List<String> getAllIds(){
        return randomzyDatabase.activeChatDao().getAllIds();
    }
}
