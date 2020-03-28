package com.ash.randomzy;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import com.ash.randomzy.activity.BaseActivity;
import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.repository.LocalUserRepository;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.nio.channels.NonWritableChannelException;

import androidx.annotation.NonNull;

public class RandomzyApp extends Application {

    private FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        if(!(mAuth.getCurrentUser() == null)) {
            UserUtil.initUserIds(getApplicationContext());
            new MessageAsyncTask(getApplicationContext(),MessageAsyncTask.SEND_UNSENT_MESSAGES).execute();
            new ActiveChatAsyncTask(this, ActiveChatAsyncTask.POST_FAV_AND_ALL_ACTIVE_CHAT).execute();
        }
        if(Build.VERSION.SDK_INT > 28) {
            ConnectivityManager.NetworkCallback nc = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    BaseActivity.setOnline();
                    super.onAvailable(network);
                }
            };
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
