package com.ash.randomzy.utility;

import android.content.Context;
import android.content.Intent;

public class ActivityLauncher {

    public static void startActivityClearCurrentTask(Context context, Class<?> activity) {
        Intent intent=new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
