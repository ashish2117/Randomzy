package com.ash.randomzy.dao;

import com.ash.randomzy.entity.Message;
import com.ash.randomzy.model.MessageCount;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface MessageDao {

    int MAX_MESSAGES_FOR_USER = 15;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage (Message message);

    @Query("SELECT * FROM Message")
    List<Message> getAll();

    @Query("SELECT * FROM Message WHERE messageId =:id")
    Message getMessage(String id);

    @Query("SELECT * FROM Message WHERE sentBy =:userId OR sentTo =:userId ORDER BY timeStamp DESC LIMIT :numOfMessages")
    List<Message> getMessagesForUser(String userId, int numOfMessages);

    @Query("DELETE FROM Message WHERE messageId =:id")
    int deleteMessage(String id);

    @Query("DELETE FROM Message")
    int deleteAllMessages();

    @Query("UPDATE Message SET messageStatus =:messageStatus WHERE messageId =:id")
    int updateMessageStatus(String id, int messageStatus);

    @Query("UPDATE Message SET message =:message WHERE messageId =:id")
    int updateMessageText(String id, String message);

    @Query("SELECT * FROM message WHERE messageStatus !=:messageStatus AND sentBy =:sentBy ORDER BY timeStamp DESC")
    List<Message> getMessageSentByUserOfExcludingStatus(int messageStatus, String sentBy);

    @Query("SELECT * FROM message WHERE messageStatus =:messageStatus")
    List<Message> getAll(int messageStatus);

   @Query("SELECT sentBy as userId, COUNT(*) as count FROM Message where messageStatus =:messageStatus AND sentBy !=:userId GROUP BY sentBy")
    List<MessageCount> getMessageCountOfStatus(int messageStatus, String userId);
}
