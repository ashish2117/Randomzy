package com.ash.randomzy.utility;

import android.net.Uri;

import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.ImageMessageProgressEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class ImageUploader {

    public static void uploadImageMessage(Uri uri, Message message){
        File file = new File(uri.getPath());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("sent_pictures");
        storageReference.child(System.currentTimeMillis() + file.getName()).putFile(uri)
                .addOnSuccessListener((taskSnapshot) ->{
                    MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
                    messageStatusUpdate.setMessageStatus(MessageStatus.SENT);
                    messageStatusUpdate.setMessageId(message.getMessageId());
                    messageStatusUpdate.setForMessageType(MessageTypes.MESSAGE_TYPE_IMAGE);
                    messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
                    messageStatusUpdate.setUserId(message.getSentTo());
                    MessageStatusUpdateEvent messageStatusUpdateEvent =
                            new MessageStatusUpdateEvent(messageStatusUpdate, message.getSentBy(), message.getSentTo());
                    EventBus.getDefault().post(messageStatusUpdateEvent);
                })
                .addOnFailureListener((e) -> {

                })
                .addOnProgressListener((taskSnapshot -> {
                    int progress = (int)((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                    EventBus.getDefault().post(new ImageMessageProgressEvent(message, progress));
                }));
    }
}
