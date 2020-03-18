package com.ash.randomzy.dao;

import com.ash.randomzy.entity.ActiveChat;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ActiveChatDao {

    @Insert
    void insertActiveChat(ActiveChat activeChat);

    @Query("SELECT * FROM ActiveChat")
    List<ActiveChat> getAll();

    @Query("SELECT * FROM ActiveChat where id =:id")
    ActiveChat getActiveChat(String id);

    @Query("DELETE FROM ActiveChat where id =:id")
    int deleteActiveChat(String id);

    @Query("SELECT * FROM ActiveChat WHERE isFav = 1")
    List<ActiveChat> getFavActiveChats();

    @Query("DELETE FROM ActiveChat")
    int deleteAllActiveChats();

    @Query("SELECT id FROM ActiveChat")
    List<String> getAllIds();
}
