package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.repository.LocalUserRepository;

public class LocalUserAsyncTask extends AsyncTask<LocalUser, Void, Void> {

    public static final int ADD_LOCAL_USER_TASK = 0;
    public static final int UPDATE_IS_FAV = 1;
    private Context context;
    private int taskType;
    private LocalUserRepository localUserRepository;

    public  LocalUserAsyncTask(Context context, int taskType){
        this.context = context;
        this.taskType = taskType;
        this.localUserRepository = new LocalUserRepository(context);
    }
    @Override
    protected Void doInBackground(LocalUser... localUsers) {
        switch (taskType){
            case ADD_LOCAL_USER_TASK:
                addNewLocalUser(localUsers[0]);
            case UPDATE_IS_FAV:
                updateIsFav(localUsers[0]);
        }
        return null;
    }

    private void updateIsFav(LocalUser localUser) {
        localUserRepository.updateIsFav(localUser.getId(), localUser.getIsFav());
        new ActiveChatAsyncTask(context, ActiveChatAsyncTask.POST_FAV_AND_ALL_ACTIVE_CHAT).execute();
    }

    private void addNewLocalUser(LocalUser localUser) {
        localUserRepository.insertLocalUser(localUser);
    }
}
