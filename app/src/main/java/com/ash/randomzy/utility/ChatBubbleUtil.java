package com.ash.randomzy.utility;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ash.randomzy.R;
import com.ash.randomzy.activity.ImageViewerActivity;
import com.ash.randomzy.entity.Message;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;


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

    public static LinearLayout getImageMessageChatBubble(Message message, Activity context) throws JSONException {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout imageMessageChatBubble;
        LinearLayout.LayoutParams chatBubbleLayoutParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        JSONObject extras = new JSONObject(message.getExtras());
        Uri uri = Uri.parse(extras.getString("imageUri"));
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSentBy())) {
            chatBubbleLayoutParams.gravity = Gravity.RIGHT;
            imageMessageChatBubble = (LinearLayout) layoutInflater.inflate(R.layout.image_message_right, null, false);
            ImageView imageView = imageMessageChatBubble.findViewById(R.id.messageStatusImageView);
            ImageView imageSentView = imageMessageChatBubble.findViewById(R.id.image_message_image_view);
            imageSentView.setImageURI(uri);
            imageSentView.setTag(uri.toString());
            MessageStatusUtil.setMessageStatusToImageViewForImageMessage(imageView, message.getMessageStatus());
        } else {
            chatBubbleLayoutParams.gravity = Gravity.LEFT;
            imageMessageChatBubble = (LinearLayout) layoutInflater.inflate(R.layout.image_message_left, null, false);
            ImageView imageSentView = imageMessageChatBubble.findViewById(R.id.image_message_image_view);
            ImageUtil.setBlurImageToImageView(imageSentView, ImageUtil.getThumbnail(context, uri), context);
            imageSentView.setTag(uri.toString());
        }
        ((TextView) imageMessageChatBubble.findViewById(R.id.image_message_timestamp_text_view))
                .setText(TimestampUtil.getTimeIn12HourFormat(System.currentTimeMillis()));
        CircleProgressBar circleProgressBar = imageMessageChatBubble.findViewById(R.id.progress_circ);
        circleProgressBar.setProgress(60);
        chatBubbleLayoutParams.setMargins(0, 0, 0, 10);
        imageMessageChatBubble.setLayoutParams(chatBubbleLayoutParams);
        return imageMessageChatBubble;
    }
}
