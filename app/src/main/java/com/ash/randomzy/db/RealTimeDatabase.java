package com.ash.randomzy.db;

import android.provider.ContactsContract;
import android.util.Log;

import com.ash.randomzy.constants.RealTimeDbNodes;
import com.ash.randomzy.entity.Message;
import com.google.firebase.database.FirebaseDatabase;

public class RealTimeDatabase {

    public static void sendMessage(Message message){
        FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.MESSAGES_NODE)
                .child(message.getSentTo()).child(message.getMessageId()).setValue(message);
    }
}
