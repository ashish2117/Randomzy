package com.ash.randomzy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.ash.randomzy.entity.Message;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyNotificationManager {

    private static final String CHANNEL_ID = "myChannelId";
    private static final int MESSAGE_NOTIFICATION = 0;

    public static void showTextMessageNotification(Context context, Message message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dp)
                .setContentTitle("New Message")
                .setContentText(message.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(MESSAGE_NOTIFICATION, builder.build());

    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "abc";
            String description = "abc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
