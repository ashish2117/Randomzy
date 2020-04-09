package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.model.ActiveChat;
import com.ash.randomzy.entity.LocalUser;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetAllActiveChatEvent;
import com.ash.randomzy.event.GetAllFavActiveChat;
import com.ash.randomzy.event.NewActiveChatEvent;
import com.ash.randomzy.model.MessageCount;
import com.ash.randomzy.model.User;
import com.ash.randomzy.repository.ActiveChatRepository;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class ActiveChatAsyncTask extends AsyncTask<Void, Void, Void> {

    public static final int ADD_NEW_INCOMING_ACTIVE_CHAT = 1;
    public static final int ADD_NEW_OUTGOING_ACTIVE_CHAT = 2;
    public static final int GET_ALL_ACTIVE_CHAT = 3;
    public static final int GET_ALL_FAV_ACTIVE_CHAT = 4;
    public static final int POST_FAV_AND_ALL_ACTIVE_CHAT = 5;


    private static final String TAG = "randomzy_debug";

    private int activeChatTaskType;
    private ActiveChat activeChat;
    private Context context;
    private FirebaseAuth mAuth;
    private Message message;
    private ActiveChatRepository activeChatRepository;

    public ActiveChatAsyncTask(Context context, int activeChatTaskType){
        activeChatRepository = new ActiveChatRepository(context);
        this.mAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.activeChatTaskType = activeChatTaskType;
    }

    public ActiveChatAsyncTask(Context context, int activeChatTaskType, ActiveChat activeChat){
        this(context, activeChatTaskType);
        this.activeChat = activeChat;
    }

    public ActiveChatAsyncTask(Context context, int activeChatTaskType, Message message){
        this(context, activeChatTaskType);
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("randomzy_debug", "activeChatTaskType " + activeChatTaskType );
        switch (activeChatTaskType){
            case ADD_NEW_INCOMING_ACTIVE_CHAT:
            case ADD_NEW_OUTGOING_ACTIVE_CHAT:
                addNewIncomingActiveChat(message);
                break;
            case GET_ALL_ACTIVE_CHAT:
                getAllActiveChat();
            case GET_ALL_FAV_ACTIVE_CHAT:
                getAllFavActiveChat();
            case POST_FAV_AND_ALL_ACTIVE_CHAT:
                postFavAndAllActiveChat();
        }
        return null;
    }

    private void postFavAndAllActiveChat() {
        MessageRepository messageRepository = new MessageRepository(context);
        List<ActiveChat> allActiveChatList= activeChatRepository.getAllActiveChats();
        List<ActiveChat> favActiveChatList= activeChatRepository.getFavActiveChats();
        List<MessageCount> messageCounts = messageRepository.getMyUnreadMessageCount();
        Log.d(TAG, messageCounts.toString());
        Map<String, Integer> map = new HashMap<>();
        for(MessageCount messageCount: messageCounts){
            map.put(messageCount.getUserId(), messageCount.getCount());
        }
        for(ActiveChat activeChat:allActiveChatList){
            if(map.get(activeChat.getId()) != null)
                 activeChat.setUnreadCount(map.get(activeChat.getId()));
        }
        for(ActiveChat activeChat:favActiveChatList){
            if(map.get(activeChat.getId()) != null)
                activeChat.setUnreadCount(map.get(activeChat.getId()));
        }
        EventBus.getDefault().postSticky(new GetAllActiveChatEvent(allActiveChatList));
        EventBus.getDefault().postSticky(new GetAllFavActiveChat(favActiveChatList));
    }

    private void getAllActiveChat() {
        List<ActiveChat> activeChatList= activeChatRepository.getAllActiveChats();
        EventBus.getDefault().postSticky(new GetAllActiveChatEvent(activeChatList));
    }

    private  void  getAllFavActiveChat(){
        List<ActiveChat> activeChatList= activeChatRepository.getFavActiveChats();
        EventBus.getDefault().postSticky(new GetAllFavActiveChat(activeChatList));
    }

    private void addNewIncomingActiveChat(final Message message) {
        Log.d(TAG, "Adding new incoming active chat");
        String referToUser = null;
        if(activeChatTaskType == ADD_NEW_INCOMING_ACTIVE_CHAT){
            referToUser = message.getSentBy();
        }else if(activeChatTaskType == ADD_NEW_OUTGOING_ACTIVE_CHAT){
            referToUser = message.getSentTo();
        }

        Log.d(TAG, referToUser);

        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(referToUser);

        firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, dataSnapshot.toString());
                        User user = dataSnapshot.getValue(User.class);
                        LocalUser localUser = new LocalUser();
                        ActiveChat activeChat = new ActiveChat();
                        activeChat.setName(user.getName());
                        localUser.setName(user.getName());
                        activeChat.setIsFav(0);
                        localUser.setIsFav(0);
                        activeChat.setLastMessageTime(message.getTimeStamp());
                        activeChat.setLastMessageText(message.getMessage());
                        activeChat.setProfilePicUrlServer(user.getProfilePicThumbUrl());
                        localUser.setProfilePicUrlServer(user.getProfilePicThumbUrl());
                        if(activeChatTaskType == ADD_NEW_INCOMING_ACTIVE_CHAT) {
                            activeChat.setSentBy(message.getSentBy());
                            activeChat.setLastMessageStatus(MessageStatus.DELIVERED);
                            activeChat.setId(message.getSentBy());
                            localUser.setId(message.getSentBy());
                            UserUtil.addUser(message.getSentBy());
                        }
                        else {
                            activeChat.setSentBy(mAuth.getCurrentUser().getUid());
                            activeChat.setLastMessageStatus(MessageStatus.SENDING);
                            activeChat.setId(message.getSentTo());
                            localUser.setId(message.getSentTo());
                            UserUtil.addUser(message.getSentTo());
                        }
                        EventBus.getDefault().post(new NewActiveChatEvent(activeChat));
                        new LocalUserAsyncTask(context, LocalUserAsyncTask.ADD_LOCAL_USER_TASK).execute(localUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
