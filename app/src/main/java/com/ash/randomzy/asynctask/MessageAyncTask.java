package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetMessagesForUserEvent;
import com.ash.randomzy.event.MessageSentEvent;
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
    public static final int UPDATE_DB_MESSAGE_SENT_TASK = 2;

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

    public MessageAyncTask(Context context, int messageTaskType, String userId){
        this.context = context;
        this.messageTaskType = messageTaskType;
        this.mAuth = FirebaseAuth.getInstance();
        this.messageRepository = new MessageRepository(context);
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Message... messages) {
        switch (this.messageTaskType){
            case MESSAGE_SEND_TASK :
                sendMessage(messages[0]);
                break;
            case  GET_MESSAGES_FOR_USER_TASK :
                getMessagesForUser(userId, 40);
                break;
            case UPDATE_DB_MESSAGE_SENT_TASK:
                updateDbMessageSent(messages[0]);
        }
        return null;
    }

    private void updateDbMessageSent(Message message){
        int rows = messageRepository.updateMessageStatus(message.getMessageId(), MessageStatus.SENT);
        Log.d(TAG, "Updated " + rows + " messages");
    }

    private void getMessagesForUser(String userId, int numberOfMessages) {
        Log.d(TAG,"Getting messages for user " + userId);
        List<Message> allMessages = messageRepository.getMessagesForUser(userId, numberOfMessages);
        EventBus.getDefault().post(new GetMessagesForUserEvent(allMessages));
        Log.d(TAG,allMessages.size() + " Messages Found");
    }

    private void sendMessage(Message message) {
        Log.d(TAG, "Message Sender sending a message" + message.getMessageId());
        messageRepository.insertMessage(message);
        checkActiveChatAvail(message);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.child(message.getSentTo())
                .child(message.getMessageId())
                .setValue(message)
                .addOnFailureListener((failure) -> {
                    Toast.makeText(context, failure.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener((task -> {
            if (task.isSuccessful())
                EventBus.getDefault().post(new MessageSentEvent(message.getMessageId()));
                new MessageAyncTask(context, UPDATE_DB_MESSAGE_SENT_TASK).execute(message);
        }));
    }

    private void checkActiveChatAvail(Message message){
        if(!UserUtil.hasUser(message.getSentTo())){
            Log.d(TAG, "User Not Avail " + message.getSentTo());
            new ActiveChatAsyncTask(context, ActiveChatAsyncTask.ADD_NEW_OUTGOING_ACTIVE_CHAT,message).execute();
            return;
        }
        Log.d(TAG, "User Avail");
    }
}
