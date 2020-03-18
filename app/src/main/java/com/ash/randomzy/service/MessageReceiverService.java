package com.ash.randomzy.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageSentEvent;
import com.ash.randomzy.event.MessageUpdateEvent;
import com.ash.randomzy.model.MessageUpdate;
import com.ash.randomzy.repository.ActiveChatRepository;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageReceiverService extends Service {

    private FirebaseAuth mAuth;
    private DatabaseReference messageReference;
    private DatabaseReference messageUpdatesReference;
    private MessageRepository messageRepository;
    private ActiveChatRepository activeChatRepository;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("randomzy_debug", "Message Receiver Created");
        mAuth = FirebaseAuth.getInstance();
        initDatabaseReferences();
        addDatabaseReferenceListeners();
        messageRepository = new MessageRepository(getApplicationContext());
        activeChatRepository = new ActiveChatRepository(getApplicationContext());
        super.onCreate();
    }

    private void initDatabaseReferences() {
        messageReference = FirebaseDatabase.getInstance().getReference("messages")
                .child(mAuth.getCurrentUser().getUid());
        messageUpdatesReference = FirebaseDatabase.getInstance().getReference("message_updates")
                .child(mAuth.getCurrentUser().getUid());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("randomzy_debug", "Message Receiver Destroyed");
        super.onDestroy();
    }


    private void newMessageArrvied(DataSnapshot dataSnapshot, String s) {
        Log.d("randomzy_debug", "New Message Arrived");
        Message message = (Message) dataSnapshot.getValue(Message.class);
        if (!message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {
            EventBus.getDefault().post(new MessageReceiveEvent(message));
            new MessageTask(MessageTask.MESSAGE_TASK_INSERT).execute(message);
        }
    }

    private void messageUpdateArrived(DataSnapshot dataSnapshot, String s) {
        Log.d("randomzy_debug", "Message Update Arrived");
        MessageUpdate messageUpdate = dataSnapshot.getValue(MessageUpdate.class);
        if(messageUpdate.getMessageUpdateType() == MessageUpdate.MESSAGE_READ){
            EventBus.getDefault().post(new MessageUpdateEvent(messageUpdate));
            new MessageUpdateTask().execute(messageUpdate);
        }
    }

    private void deleteMessage(Message message){
        messageReference.child(message.getMessageId()).removeValue();
    }


    private void addDatabaseReferenceListeners() {

        messageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                newMessageArrvied(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        messageUpdatesReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messageUpdateArrived(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }


    private class MessageTask extends AsyncTask<Message, Void, Void> {

        private static final int MESSAGE_TASK_INSERT = 0;
        private static final int MESSAGE_TASK_DELETE = 1;
        private int taskType;

        public MessageTask(int taskType) {
            this.taskType = taskType;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            if (taskType == MESSAGE_TASK_INSERT) {
                messageRepository.insertMessage(messages[0]);
                deleteMessage(messages[0]);
                if (!UserUtil.hasUser(messages[0].getSentBy())) {
                    addNewActiveChat(messages[0]);
                }
            } else if (taskType == MESSAGE_TASK_DELETE)
                messageRepository.deleteMessage(messages[0].getMessageId());
            return null;
        }

        private void addNewActiveChat(Message message) {
            new ActiveChatAsyncTask(getApplicationContext(),ActiveChatAsyncTask.ADD_NEW_INCOMING_ACTIVE_CHAT,message).execute();
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }

    private class MessageUpdateTask extends AsyncTask<MessageUpdate, Void, Void>{

        @Override
        protected Void doInBackground(MessageUpdate... messageUpdates) {
            switch (messageUpdates[0].getMessageUpdateType()){
                case MessageUpdate.MESSAGE_READ:
                    messageRepository.updateMessageStatus(messageUpdates[0].getMessageId(), MessageStatus.READ);
                    break;
                case MessageUpdate.MESSAGE_EDITED:
                    messageRepository.updateMessageText(messageUpdates[0].getMessageId(),messageUpdates[0].getMessage());
            }
            return null;
        }
    }

}
