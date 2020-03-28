package com.ash.randomzy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ash.randomzy.asynctask.IncomingMessageStatusUpdateTask;
import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.asynctask.OutgoingMessageStatusUpdateTask;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.constants.RealTimeDbNodes;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.event.TypingEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.model.TypingStatus;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RealTimeDbListenerService extends Service {

    private FirebaseAuth mAuth;
    private DatabaseReference messageReference;
    private DatabaseReference fireAndForgetRef;
    private DatabaseReference onlineStatusRef;

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
        super.onCreate();
    }

    private void initDatabaseReferences() {
        messageReference = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.MESSAGES_NODE)
                .child(mAuth.getCurrentUser().getUid());
        fireAndForgetRef = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.FIRE_AND_FORGET_NODE)
                .child(mAuth.getCurrentUser().getUid());
        onlineStatusRef = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.USERS_NODE)
                .child(mAuth.getCurrentUser().getUid()).child(RealTimeDbNodes.ONLINE_STATUS_NODE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onlineStatusRef.setValue("Online");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("randomzy_debug", "Message Receiver Destroyed");
        super.onDestroy();
    }


    private void newMessageArrvied(DataSnapshot dataSnapshot, String s) {
        Log.d("randomzy_debug", "New Message Arrived");
        int meesageType = ((Long) dataSnapshot.child("messageType").getValue()).intValue();
        switch (meesageType) {
            case MessageTypes.MESSAGE_TYPE_TEXT:
                newTextMessageArrived(dataSnapshot, s);
                break;
            case MessageTypes.MESSAGE_STATUS_UPDATE:
                newMessageStatusUpdateArrived(dataSnapshot, s);
                break;
        }
    }

    private void fireAndForgetMessageArrived(DataSnapshot dataSnapshot, String s) {
        Log.d("randomzy_debug", "New Fire and Forget Message Arrived");
        int meesageType = ((Long) dataSnapshot.child("messageType").getValue()).intValue();
        switch (meesageType){
            case MessageTypes.MESSAGE_TYPE_TYPING:
                typingStatusArrived(dataSnapshot, s);
        }
    }

    public void newTextMessageArrived(DataSnapshot dataSnapshot, String s) {
        Message message = dataSnapshot.getValue(Message.class);
        String sentBy = message.getSentBy();
        if (!sentBy.equals(mAuth.getCurrentUser().getUid())) {
            EventBus.getDefault().post(new MessageReceiveEvent(message));
            new MessageAsyncTask(getApplicationContext(), MessageAsyncTask.INSERT_DB_DELETE_REMOTE_TASK).execute(message);
            Log.d("randomzy_debug", "===" + UserUtil.getChatOpenedFor() + "===");
            if (!UserUtil.getChatOpenedFor().equals(sentBy)) {
                Log.d("randomzy_debug", "No Chat Opened");
                MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
                messageStatusUpdate.setUserId(sentBy);
                messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
                messageStatusUpdate.setMessageId(message.getMessageId());
                messageStatusUpdate.setMessageStatus(MessageStatus.DELIVERED);
                messageStatusUpdate.setForMessageType(message.getMessageType());
                new OutgoingMessageStatusUpdateTask(getApplicationContext()).execute(messageStatusUpdate);
            }
        }
    }


    private void newMessageStatusUpdateArrived(DataSnapshot dataSnapshot, String s) {
        MessageStatusUpdate messageStatusUpdate = dataSnapshot.getValue(MessageStatusUpdate.class);
        EventBus.getDefault().post(new MessageStatusUpdateEvent(messageStatusUpdate,
                messageStatusUpdate.getUserId(), messageStatusUpdate.getUserId()));
        new IncomingMessageStatusUpdateTask(getApplicationContext()).execute(messageStatusUpdate);
    }

    private void typingStatusArrived(DataSnapshot dataSnapshot, String s) {
        TypingStatus typingStatus = dataSnapshot.getValue(TypingStatus.class);
        EventBus.getDefault().post(new TypingEvent(typingStatus));
        fireAndForgetRef.child(dataSnapshot.getKey()).removeValue();
    }


    private void addDatabaseReferenceListeners() {

        onlineStatusRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

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



        ChildEventListener fireAndForgetListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                fireAndForgetMessageArrived(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        fireAndForgetRef.removeValue().addOnCompleteListener((task -> {
            if(task.isSuccessful()){
               fireAndForgetRef.addChildEventListener(fireAndForgetListener);
            }
        }));
    }


}
