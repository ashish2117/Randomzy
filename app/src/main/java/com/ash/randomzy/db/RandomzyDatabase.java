package com.ash.randomzy.db;

import com.ash.randomzy.dao.ActiveChatDao;
import com.ash.randomzy.dao.MessageDao;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.entity.Message;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ActiveChat.class, Message.class}, version = 1, exportSchema = false)
public abstract class RandomzyDatabase extends RoomDatabase {
    public abstract ActiveChatDao activeChatDao();
    public abstract MessageDao messageDao();
}
