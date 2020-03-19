package com.ash.randomzy.repository;

import android.content.Context;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.db.RandomzyDatabase;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.entity.Message;

import java.util.List;

import androidx.room.Room;

public class MessageRepository {

    private final String DBNAME = "dandomzy-db";

    private RandomzyDatabase randomzyDatabase;

    public List<Message> getAll(){
        return randomzyDatabase.messageDao().getAll();
    }

    public List<Message> getMessagesForUser(String userId, int numberOfMessages){
        return randomzyDatabase.messageDao().getMessagesForUser(userId, numberOfMessages);
    }

    public MessageRepository(Context context){
        randomzyDatabase = Room.databaseBuilder(context, RandomzyDatabase.class,DBNAME).build();
    }

    public void insertMessage(Message message){
        randomzyDatabase.messageDao().insertMessage(message);
    }

    public Message getMessage(String id){
        return randomzyDatabase.messageDao().getMessage(id);
    }

    public int deleteMessage(String id){
        return randomzyDatabase.messageDao().deleteMessage(id);
    }

    public int deleteAllMessages(){
        return randomzyDatabase.messageDao().deleteAllMessages();
    }

    public int updateMessageStatus(String id, int messageStatus){
        return randomzyDatabase.messageDao().updateMessageStatus(id, messageStatus);
    }

    public int updateMessageText(String messageId, String message){
        return randomzyDatabase.messageDao().updateMessageText(messageId, message);
    }

    public List<Message> getUnreadMessagesSentByUser(String sentBy){
        return randomzyDatabase.messageDao().getMessageSentByUserOfExcludingStatus(MessageStatus.READ, sentBy);
    }
}
