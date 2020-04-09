package com.ash.randomzy.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.constants.FirebaseStoragePaths;
import com.ash.randomzy.constants.LocalStoragePaths;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.db.RealTimeDatabase;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.ImageMessageProgressEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ImageUploader {

    private int count;

    private Message message;
    private Context context;
    private Uri uri;
    private String imagePathInDb;
    private String thumbPathInDb;

    public ImageUploader(Message message, Context context, Uri uri) {
        this.message = message;
        this.context = context;
        this.uri = uri;
    }

    public void uploadImageMessage() {
        File file = new File(uri.getPath());
        String fileName = System.currentTimeMillis() + file.getName();
        Bitmap thumbBitmap = null;
        try {
            thumbBitmap = ImageUtil.getThumbnail(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri thumbUri = ImageUtil.writeImage(thumbBitmap, context, LocalStoragePaths.getThumbnailPath(context), fileName);
        File thumbFile = new File(thumbUri.getPath());
        imagePathInDb = FirebaseStoragePaths.PICTURES_PATH + fileName;
        thumbPathInDb = FirebaseStoragePaths.THUMBNAILS_PATH + fileName;
        count = 0;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(imagePathInDb).putFile(uri)
                .addOnSuccessListener((taskSnapshot) -> {
                    count++;
                    if (count == 2) onUpLoadComplete();;
                })
                .addOnFailureListener((e) -> {

                })
                .addOnProgressListener(taskSnapshot -> {
                    int progress = (int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                    EventBus.getDefault().post(new ImageMessageProgressEvent(message, progress));
                    try {
                        JSONObject jsonObject = new JSONObject(message.getExtras());
                        jsonObject.put("progress", progress);
                        new MessageAsyncTask(context, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

        storageReference.child(thumbPathInDb).putFile(thumbUri)
                .addOnSuccessListener((taskSnapshot) -> {
                    count++;
                    if (count == 2) onUpLoadComplete();
                });
    }

    private void sendMessageStatusUpdateForImageMessage() {
        MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
        messageStatusUpdate.setMessageStatus(MessageStatus.SENT);
        messageStatusUpdate.setMessageId(message.getMessageId());
        messageStatusUpdate.setForMessageType(MessageTypes.IMAGE);
        messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
        messageStatusUpdate.setUserId(message.getSentTo());
        MessageStatusUpdateEvent messageStatusUpdateEvent =
                new MessageStatusUpdateEvent(messageStatusUpdate, message.getSentBy(), message.getSentTo());
        EventBus.getDefault().post(messageStatusUpdateEvent);
        message.setMessageStatus(MessageStatus.SENT);
    }

    private void onUpLoadComplete(){
        new MessageAsyncTask(context, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
        sendMessageStatusUpdateForImageMessage();
        JSONObject jsonObject = new JSONObject();
        Message cloneMessage = message.clone();
        try {
            jsonObject.put("imageUri", thumbPathInDb);
            jsonObject.put("fullImageUri", imagePathInDb);
            cloneMessage.setExtras(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RealTimeDatabase.sendMessage(cloneMessage);
    }

}
