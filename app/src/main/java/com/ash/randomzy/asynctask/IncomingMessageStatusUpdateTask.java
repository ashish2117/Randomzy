package com.ash.randomzy.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.repository.MessageRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IncomingMessageStatusUpdateTask extends AsyncTask<MessageStatusUpdate, Void, Void> {

    private MessageRepository messageRepository;
    private FirebaseAuth mAuth;

    public IncomingMessageStatusUpdateTask(Context context){
        messageRepository = new MessageRepository(context);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected Void doInBackground(MessageStatusUpdate... messageStatusUpdates) {
        switch (messageStatusUpdates[0].getMessageStatus()) {
            case MessageStatus.READ:
                messageRepository.updateMessageStatus(messageStatusUpdates[0].getMessageId(), MessageStatus.READ);
                deleteMessageStatus(messageStatusUpdates[0]);
                break;
            case MessageStatus.DELIVERED:
                messageRepository.updateMessageStatus(messageStatusUpdates[0].getMessageId(), MessageStatus.DELIVERED);
                deleteMessageStatus(messageStatusUpdates[0]);
                break;
        }
        return null;
    }

    private void deleteMessageStatus(MessageStatusUpdate messageStatusUpdate){
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference("messages");
        messageReference.child(mAuth.getCurrentUser().getUid()).child(messageStatusUpdate.getMessageId()).removeValue();
    }

}
