package com.ash.randomzy;

import android.app.Application;

import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.asynctask.MessageAyncTask;
import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.repository.LocalUserRepository;
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
            new MessageAyncTask(getApplicationContext(),MessageAyncTask.SEND_UNSENT_MESSAGES).execute();
            new ActiveChatAsyncTask(this, ActiveChatAsyncTask.POST_FAV_AND_ALL_ACTIVE_CHAT).execute();
        }
    }


    private void clearMessages(){
        MessageRepository repository = new MessageRepository(getApplicationContext());
        LocalUserRepository localUserRepository = new LocalUserRepository(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.deleteAllMessages();
                localUserRepository.deleteAllLocalUsers();
            }
        }).start();
    }
}
