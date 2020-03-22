package com.ash.randomzy.utility;

import android.widget.ImageView;

import com.ash.randomzy.R;
import com.ash.randomzy.constants.MessageStatus;

public class MessageStatusUtil {

    public static void setMessageStatusToImageView(ImageView messageStatusImageView, int messageStatus) {
        switch (messageStatus) {
            case MessageStatus.SENT:
                messageStatusImageView.setImageResource(R.drawable.ic_done_text_secondary_14dp);
                break;
            case MessageStatus.DELIVERED:
                messageStatusImageView.setImageResource(R.drawable.ic_done_all_text_secondary_14dp);
                break;
            case MessageStatus.READ:
                messageStatusImageView.setImageResource(R.drawable.ic_done_all_blue_14dp);
                break;case MessageStatus.SENDING:
                 messageStatusImageView.setImageResource(R.drawable.ic_access_time_text_secondary_14dp);
        }
    }
}
