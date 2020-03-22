package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.RealTimeDbNodes;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.repository.MessageRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OutgoingMessageStatusUpdateTask extends AsyncTask<MessageStatusUpdate, Void, Void> {

    private MessageRepository messageRepository;
    private FirebaseAuth mAuth;

    public OutgoingMessageStatusUpdateTask(Context context){
        messageRepository = new MessageRepository(context);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected Void doInBackground(MessageStatusUpdate... messageStatusUpdates){
        Log.d("randomzy_debug","Updating status for " + messageStatusUpdates[0].getMessageId() + messageStatusUpdates[0].getMessageStatus());
        int messageStatus = messageStatusUpdates[0].getMessageStatus();
        if(!(messageStatus == MessageStatus.SENT))
            sendMessageStatus(messageStatusUpdates[0]);
        messageRepository.updateMessageStatus(messageStatusUpdates[0].getMessageId(), messageStatus);
        return null;
    }

    private void sendMessageStatus(MessageStatusUpdate messageStatusUpdate){
        String sendMessageStatusUpdateTo = messageStatusUpdate.getUserId();
        messageStatusUpdate.setUserId(mAuth.getCurrentUser().getUid());
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.MESSAGES_NODE);
        messageReference.child(sendMessageStatusUpdateTo).child(messageStatusUpdate.getMessageId()).setValue(messageStatusUpdate);
    }
}
