package com.ash.randomzy.db;

import android.content.Context;
import android.net.Uri;

import com.ash.randomzy.R;
import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.asynctask.OutgoingMessageStatusUpdateTask;
import com.ash.randomzy.constants.FirebaseStoragePaths;
import com.ash.randomzy.constants.MediaProgress;
import com.ash.randomzy.constants.LocalStoragePaths;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.ImageMessageDownloadedEvent;
import com.ash.randomzy.event.ImageMessageProgressEvent;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class FirebaseStorageManager {

    JSONObject extras;

    public void downloadImageMessageThumbAndPostMessage(Message message, Context context) {
        String thumbUri = null;
        try {
            extras = new JSONObject(message.getExtras());
            thumbUri = extras.getString("imageUri");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str[] = thumbUri.split("/");
        String fileName = str[str.length - 1];
        File mediaStorageDir =new File(LocalStoragePaths.getThumbnailPath(context));
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return;
            }
        }
        File mediaFile;
        String pathName = mediaStorageDir.getPath() + File.separator + fileName;
        mediaFile = new File(pathName);
        StorageReference reference = FirebaseStorage.getInstance().getReference();
        reference.child(thumbUri).getFile(mediaFile).addOnSuccessListener((taskSnapshot -> {
            try {
                extras.put(context.getResources().getString(R.string.image_uri), Uri.fromFile(mediaFile));
                extras.put(context.getResources().getString(R.string.image_view_progress), MediaProgress.MEDIA_DOWNLOADED_PROGRESS_VALUE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.setExtras(extras.toString());
            new MessageAsyncTask(context, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
            EventBus.getDefault().post(new MessageReceiveEvent(message));
        }));
    }

    public void downloadImageMessageOriginalImage(Message message, Context context) {
        String originalImageUri = null;
        try {
            extras = new JSONObject(message.getExtras());
            originalImageUri = extras.getString("fullImageUri");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str[] = originalImageUri.split("/");
        String fileName = str[str.length - 1];
        File mediaStorageDir = new File(context.getExternalFilesDir(null).getAbsolutePath()
                + LocalStoragePaths.getImagesPath(context));
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return;
            }
        }
        File mediaFile;
        String pathName = mediaStorageDir.getPath() + File.separator + fileName;
        mediaFile = new File(pathName);
        StorageReference reference = FirebaseStorage.getInstance().getReference();
        reference.child(originalImageUri).getFile(mediaFile).addOnSuccessListener((taskSnapshot -> {
            try {
                extras.put("fullImageUri", Uri.fromFile(mediaFile));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.setExtras(extras.toString());
            new MessageAsyncTask(context, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
            EventBus.getDefault().post(new ImageMessageDownloadedEvent(message));
        }))
                .addOnProgressListener((taskSnapshot -> {
                    int progress = (int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                    EventBus.getDefault().post(new ImageMessageProgressEvent(message, progress));
                    try {
                        extras.put(context.getResources().getString(R.string.image_view_progress), progress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message.setExtras(extras.toString());
                    new MessageAsyncTask(context, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
                }));
    }

    public static void sendVoiceNote(Message message, Uri uri, Context context, Long lengthInMillis) {
        String fileName = System.currentTimeMillis() + ".3gp";
        String filePath = FirebaseStoragePaths.VOICE_NOTES_PATH
                + message.getSentBy()
                + File.separator + fileName;
        StorageReference voiceNotesRef = FirebaseStorage.getInstance().getReference()
                .child(filePath);
        voiceNotesRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("voiceNoteUri", filePath);
                jsonObject.put("lengthInMillis", lengthInMillis);
                message.setExtras(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
            messageStatusUpdate.setForMessageType(MessageTypes.VOICE_NOTE);
            messageStatusUpdate.setUserId(message.getSentTo());
            messageStatusUpdate.setTimeStamp(message.getTimeStamp());
            messageStatusUpdate.setMessageId(message.getMessageId());
            messageStatusUpdate.setMessageStatus(MessageStatus.SENT);
            EventBus.getDefault().post(new MessageStatusUpdateEvent(messageStatusUpdate, message.getSentBy(), message.getSentTo()));
            RealTimeDatabase.sendMessage(message);
            new OutgoingMessageStatusUpdateTask(context).execute(messageStatusUpdate);
        });
    }
}
