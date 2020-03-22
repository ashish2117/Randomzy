package com.ash.randomzy.dao;

import com.ash.randomzy.model.ActiveChat;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ActiveChatDao {

    @Query("SELECT l.id, l.isFav, l.name, l.profilePicUrlLocal, l.profilePicUrlServer," +
            "m.messageStatus as lastTextStatus, m.message as lastText, m.sentBy, m.timeStamp as lastTextTime, 0 as unreadCount " +
            "FROM Message m, LocalUser l WHERE  l.id = m.sentBy OR l.id = m.sentTo GROUP BY l.id HAVING MAX(m.timeStamp)")
    List<ActiveChat> getAll();

    @Query("SELECT l.id, l.isFav, l.name, l.profilePicUrlLocal, l.profilePicUrlServer, " +
            "m.messageStatus as lastTextStatus, m.message as lastText, m.sentBy, m.timeStamp as lastTextTime, 0 as unreadCount " +
            "FROM Message m, LocalUser l WHERE  (l.id = m.sentBy OR l.id) AND l.id +:id = m.sentTo GROUP BY l.id ")
    ActiveChat getActiveChat(String id);

    @Query("DELETE FROM LocalUser where id =:id")
    int deleteActiveChat(String id);

    @Query("SELECT l.id, l.isFav, l.name, l.profilePicUrlLocal, l.profilePicUrlServer, " +
            "m.messageStatus as lastTextStatus, m.message as lastText, m.sentBy, m.timeStamp as lastTextTime, 0 as unreadCount " +
            "FROM Message m, LocalUser l WHERE  (l.id = m.sentBy OR l.id = m.sentTo) AND l.isFav =1 GROUP BY l.id HAVING MAX(m.timeStamp)")
    List<ActiveChat> getFavActiveChats();

    @Query("DELETE FROM LocalUser")
    int deleteAllActiveChats();

    @Query("SELECT id FROM LocalUser")
    List<String> getAllIds();
}
