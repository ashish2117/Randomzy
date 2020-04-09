package com.ash.randomzy.utility;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ash.randomzy.R;
import com.ash.randomzy.constants.MediaProgress;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.Message;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatBubbleUtil {

    public static LinearLayout getTextMessageChatBubble(Message message, Context context) {
        LinearLayout.LayoutParams chatBubbleLayoutParams;
        LayoutInflater layoutInflater;
        chatBubbleLayoutParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutInflater = LayoutInflater.from(context);
        LinearLayout chatBubble = null;
        if (message.getSentBy().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.chat_bubble_right, null, false);
            ImageView messageStatusImageView = chatBubble.findViewById(R.id.messageStatusImageView);
            MessageStatusUtil.setMessageStatusToImageView(messageStatusImageView, message.getMessageStatus());
            chatBubbleLayoutParams.gravity = Gravity.RIGHT;
            chatBubbleLayoutParams.setMargins(100, 0, 0, 10);
            chatBubble.setBackgroundResource(R.drawable.right_message_bg_25dp_radius);
        } else {
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.chat_bubble_left, null, false);
            chatBubbleLayoutParams.gravity = Gravity.LEFT;
            chatBubbleLayoutParams.setMargins(0, 0, 100, 10);
            chatBubble.setBackgroundResource(R.drawable.left_message_bg_25dp_radius);
        }
        TextView messageTextView = chatBubble.findViewById(R.id.messageTextView);
        TextView timeStamTextView = chatBubble.findViewById(R.id.timeStampTextView);
        TextView messageIdTextView = chatBubble.findViewById(R.id.messageId);
        timeStamTextView.setText(TimestampUtil.getTimeIn12HourFormat(String.valueOf(message.getTimeStamp())));
        messageTextView.setText(message.getMessage());
        messageIdTextView.setText(message.getMessageId());
        messageTextView.setPadding(20, 20, 20, 20);
        chatBubble.setLayoutParams(chatBubbleLayoutParams);
        return chatBubble;
    }

    public static LinearLayout getImageMessageChatBubble(Message message, Activity context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout imageMessageChatBubble;
        LinearLayout.LayoutParams chatBubbleLayoutParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        JSONObject extras;
        String uri = null;
        String fullImageUri = null;
        int progress = 0;
        try {
            extras = new JSONObject(message.getExtras());
            uri = extras.getString("imageUri");
            fullImageUri = extras.getString("fullImageUri");
            progress = extras.getInt("progress");
        } catch (JSONException e) {
        }
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSentBy())) {
            chatBubbleLayoutParams.gravity = Gravity.RIGHT;
            imageMessageChatBubble = (LinearLayout) layoutInflater.inflate(R.layout.image_message_right, null, false);
            ImageView imageView = imageMessageChatBubble.findViewById(R.id.messageStatusImageView);
            MessageStatusUtil.setMessageStatusToImageViewForImageMessage(imageView, message.getMessageStatus());
            ImageView imageSentView = imageMessageChatBubble.findViewById(R.id.image_message_image_view);
            imageSentView.setImageURI(Uri.parse(uri));
            imageSentView.setTag(R.string.image_uri, uri);
        } else {
            chatBubbleLayoutParams.gravity = Gravity.LEFT;
            imageMessageChatBubble = (LinearLayout) layoutInflater.inflate(R.layout.image_message_left, null, false);
            ImageView imageSentView = imageMessageChatBubble.findViewById(R.id.image_message_image_view);
            if (progress == MediaProgress.MEDIA_DOWNLOADED_PROGRESS_VALUE) {
                ImageUtil.setBlurImageToImageView(imageSentView, Uri.parse(uri), context);
                imageSentView.setTag(R.string.image_uri, uri);
            } else if(progress == 100){
                if (fullImageUri != null) {
                    imageSentView.setImageURI(Uri.parse(fullImageUri));
                    imageSentView.setTag(R.string.image_uri, fullImageUri);
                    imageMessageChatBubble.findViewById(R.id.downLoadImage).setVisibility(View.GONE);
                    imageMessageChatBubble.findViewById(R.id.progress_circ).setVisibility(View.GONE);
                }
            }
        }
        ((TextView) imageMessageChatBubble.findViewById(R.id.image_message_timestamp_text_view))
                .setText(TimestampUtil.getTimeIn12HourFormat(System.currentTimeMillis()));
        CircleProgressBar circleProgressBar = imageMessageChatBubble.findViewById(R.id.progress_circ);
        circleProgressBar.setProgress(progress);
        if (message.getMessageStatus() != MessageStatus.SENDING)
            circleProgressBar.setVisibility(View.GONE);
        chatBubbleLayoutParams.setMargins(0, 0, 0, 10);
        imageMessageChatBubble.setLayoutParams(chatBubbleLayoutParams);
        return imageMessageChatBubble;
    }

    public static LinearLayout getVoiceNoteChatBubble(Message message, Context context){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout chatBubble;
        LinearLayout.LayoutParams chatBubbleLayoutParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        JSONObject extras;
        long lengthInMillis = 0l;
        try {
            extras = new JSONObject(message.getExtras());
            lengthInMillis = extras.getLong("lengthInMillis");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String length = TimestampUtil.getMinutesInString(lengthInMillis);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSentBy())){
            chatBubbleLayoutParams.gravity = Gravity.RIGHT;
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.voice_note_layout_right, null, false);
            MessageStatusUtil.setMessageStatusToImageView(chatBubble.findViewById(R.id.messageStatusImageView), message.getMessageStatus());
            chatBubbleLayoutParams.setMargins(0, 0, 0, 10);
            if(message.getMessageStatus() == MessageStatus.SENDING) {
                chatBubble.findViewById(R.id.voice_note_playback_image_view).setVisibility(View.INVISIBLE);
            }else {
                chatBubble.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }
        }else{
            chatBubbleLayoutParams.gravity = Gravity.LEFT;
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.voice_note_layout_left, null, false);
            chatBubbleLayoutParams.setMargins(0, 0, 100, 10);
            chatBubble.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }
        ((TextView)chatBubble.findViewById(R.id.voice_note_length_text_view)).setText(length);
        ((TextView)chatBubble.findViewById(R.id.timeStampTextView)).setText(TimestampUtil.getTimeIn12HourFormat(message.getTimeStamp()));
        chatBubble.setLayoutParams(chatBubbleLayoutParams);
        return chatBubble;
    }
}
