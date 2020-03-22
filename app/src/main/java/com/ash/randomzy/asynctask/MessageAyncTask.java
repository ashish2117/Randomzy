package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetMessagesForUserEvent;
import com.ash.randomzy.event.MessageOutGoingEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.event.UnreadMessagesByUserEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MessageAyncTask extends AsyncTask<Message, Void, Void> {

    public static final int MESSAGE_SEND_TASK = 0;
    public static final int GET_MESSAGES_FOR_USER_TASK = 1;
    public static final int INSERT_DB_DELETE_REMOTE_TASK = 2;
    public static final int GET_UNREAD_MESSAGES_BY_USER = 3;
    public static final int SEND_UNSENT_MESSAGES = 4;

    private static final String TAG = "randomzy_debug";

    private int messageTaskType;
    private FirebaseAuth mAuth;
    private Context context;
    private MessageRepository messageRepository;
    private String userId;

    public MessageAyncTask(Context context, int messageTaskType) {
        this.context = context;
        this.messageTaskType = messageTaskType;
        this.mAuth = FirebaseAuth.getInstance();
        this.messageRepository = new MessageRepository(context);
    }

    public MessageAyncTask(Context context, int messageTaskType, String userId) {
        this.context = context;
        this.messageTaskType = messageTaskType;
        this.mAuth = FirebaseAuth.getInstance();
        this.messageRepository = new MessageRepository(context);
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Message... messages) {
        switch (this.messageTaskType) {
            case MESSAGE_SEND_TASK:
                sendMessage(messages[0]);
                break;
            case GET_MESSAGES_FOR_USER_TASK:
                getMessagesForUser(userId, 40);
                break;
            case INSERT_DB_DELETE_REMOTE_TASK:
                insertDbDeleteRemoteTask(messages[0]);
            case GET_UNREAD_MESSAGES_BY_USER:
                getUnreadMessagesSentByUser(userId);
            case SEND_UNSENT_MESSAGES:
                sendUnsentMessages();
        }
        return null;
    }

    private void getUnreadMessagesSentByUser(String userId) {
        List<Message> list = messageRepository.getUnreadMessagesSentByUser(userId);
        EventBus.getDefault().post(new UnreadMessagesByUserEvent(list));
    }

    private void insertMessageTodb(Message message) {
        messageRepository.insertMessage(message);
    }

    private void insertDbDeleteRemoteTask(Message message) {
        message.setMessageStatus(MessageStatus.READ);
        insertMessageTodb(message);
        deleteMessage(message);
        if (!UserUtil.hasUser(message.getSentBy())) {
            addNewActiveChat(message);
        }
    }

    private void deleteMessage(Message message) {
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages")
                .child(message.getSentTo());
        messageReference.child(message.getMessageId()).removeValue();
    }

    private void sendUnsentMessages() {
        List<Message> list = messageRepository.getAll(MessageStatus.SENDING);
        if(list.size() ==0 )
            return;
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages");
        for(Message message: list){
            messageReference.child(message.getSentTo()).child(message.getMessageId()).setValue(message)
                    .addOnCompleteListener((task -> {
                        postOutGoingMessageStatusUdate(message, MessageStatus.SENT);
                    }));
        }
    }

    private void addNewActiveChat(Message message) {
        new ActiveChatAsyncTask(context, ActiveChatAsyncTask.ADD_NEW_INCOMING_ACTIVE_CHAT, message).execute();
    }

    private void getMessagesForUser(String userId, int numberOfMessages) {
        Log.d(TAG, "Getting messages for user " + userId);
        List<Message> allMessages = messageRepository.getMessagesForUser(userId, numberOfMessages);
        EventBus.getDefault().post(new GetMessagesForUserEvent(allMessages));
        Log.d(TAG, allMessages.size() + " Messages Found");
    }

    private void sendMessage(Message message) {
        Log.d(TAG, "Message Sender sending a message " + message.getMessageId());
        messageRepository.insertMessage(message);
        checkActiveChatAvail(message);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.child(message.getSentTo())
                .child(message.getMessageId())
                .setValue(message)
                .addOnFailureListener((failure) -> {
                    Toast.makeText(context, failure.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener((task -> {
            if (task.isSuccessful()) {
                postOutGoingMessageStatusUdate(message, MessageStatus.SENT);
            }
        }));
    }

    private void postOutGoingMessageStatusUdate(Message message, int messageStatus){
        MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
        messageStatusUpdate.setUserId(message.getSentTo());
        messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
        messageStatusUpdate.setMessageId(message.getMessageId());
        messageStatusUpdate.setMessageStatus(MessageStatus.SENT);
        EventBus.getDefault().post(new MessageStatusUpdateEvent(messageStatusUpdate, mAuth.getCurrentUser().getUid()));
        new OutgoingMessageStatusUpdateTask(context)
                .execute(messageStatusUpdate);
    }

    private void checkActiveChatAvail(Message message) {
        if (!UserUtil.hasUser(message.getSentTo())) {
            new ActiveChatAsyncTask(context, ActiveChatAsyncTask.ADD_NEW_OUTGOING_ACTIVE_CHAT, message).execute();
            return;
        }else {
            EventBus.getDefault().post(new MessageOutGoingEvent(message));
        }
    }
}
