package com.ash.randomzy;

import android.app.Application;
import android.content.Intent;

import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.repository.ActiveChatRepository;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;

public class RandomzyApp extends Application {

    private FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        if(!(mAuth.getCurrentUser() == null)) {
            UserUtil.initUserIds(getApplicationContext());
        }
        //addUsers();
    }


    private void clearMessages(){
        MessageRepository repository = new MessageRepository(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.deleteAllMessages();
            }
        });
    }
    private void addUsers() {
        ActiveChatRepository repository = new ActiveChatRepository(getApplicationContext());
        for (int i = 0; i < 20; i++) {
            ActiveChat activeChat = new ActiveChat();
            activeChat.setLastTextStatus(MessageStatus.READ);
            activeChat.setLastTextTime(System.currentTimeMillis());
            activeChat.setName("Ashish " + i);
            activeChat.setProfilePicUrlLocal("");
            activeChat.setProfilePicUrlServer("");
            activeChat.setSentBy(i % 2);
            activeChat.setId("gwbna" + i);
            activeChat.setIsFav(i % 2);
            activeChat.setLastText("Hello from device " + i);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    repository.insertActiveChat(activeChat);
                }
            }).start();
        }
    }
}
