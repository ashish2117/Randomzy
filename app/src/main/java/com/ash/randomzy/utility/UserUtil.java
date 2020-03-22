package com.ash.randomzy.utility;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ash.randomzy.repository.ActiveChatRepository;
import com.ash.randomzy.service.RealTimeDbListenerService;

import java.util.List;

public class UserUtil {

    private static List<String> userIds;
    private static String chatOpenedFor = "";

    private static final String TAG = "randomzy_debug";

    public static boolean hasUser(String id){
        return userIds.contains(id);
    }

    public static void initUserIds(Context context){
        ActiveChatRepository activeChatRepository = new ActiveChatRepository(context);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                userIds = activeChatRepository.getAllIds();
                Log.d(TAG, userIds.toString());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                context.startService(new Intent(context, RealTimeDbListenerService.class));
            }
        }.execute();
    }

    public static void addUser(String id){
        userIds.add(id);
    }

    public static String getChatOpenedFor() { return chatOpenedFor; }

    public static void setChatOpenedFor(String chatOpenedFor) {
        UserUtil.chatOpenedFor = chatOpenedFor;
    }

    public static boolean userInitDone(){
        if(userIds == null)
            return true;
        return false;
    }

}
