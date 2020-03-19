package com.ash.randomzy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ash.randomzy.R;
import com.ash.randomzy.asynctask.MessageAyncTask;
import com.ash.randomzy.asynctask.OutgoingMessageStatusUpdateTask;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetMessagesForUserEvent;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.event.UnreadMessagesByUserEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.utility.TimestampUtil;
import com.ash.randomzy.utility.UserUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActiveChat activeChat;
    private boolean initialMessageLoaded;

    private ScrollView scrollView;
    private ImageView sendButton;
    private EditText messageArea;
    private LinearLayout typingStatus, messageListLayout;
    private LinearLayout.LayoutParams chatBubbleLayoutParams;
    private LayoutInflater layoutInflater;
    private List<Message> messageList;

    private static final String TAG = "randomzy_debug";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initialMessageLoaded = false;
        mAuth = FirebaseAuth.getInstance();
        activeChat = new Gson().fromJson(getIntent().getStringExtra("activeChat"), ActiveChat.class);
        initViews();
        addListeners();
        populateMessageList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerToEvents();
        UserUtil.setChatOpenedFor(activeChat.getId());
        if(initialMessageLoaded)
            new MessageAyncTask(getApplicationContext(),MessageAyncTask.GET_UNREAD_MESSAGES_BY_USER, activeChat.getId()).execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterToEvents();
        UserUtil.setChatOpenedFor("");
    }

    @Override
    protected void onPause() {
        Log.d("randomzy_debug","Paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        scrollView = findViewById(R.id.scrollView);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        messageListLayout = findViewById(R.id.message_list_layout);
        typingStatus = findViewById(R.id.typingStatus);
    }

    private void addListeners() {
        sendButton.setOnClickListener((view -> {
            sendMessage();
        }));

        scrollView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            scrollToBottom();
        });
    }

    private void populateMessageList() {
        new MessageAyncTask(this, MessageAyncTask.GET_MESSAGES_FOR_USER_TASK, activeChat.getId()).execute();
    }

    private void registerToEvents() {
        EventBus.getDefault().register(this);
        Log.d(TAG, "Registered EventBus");
    }

    private void unRegisterToEvents() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Registered EventBus");
    }


    private void sendMessage() {
        Message message = getMessage();
        new MessageAyncTask(this, MessageAyncTask.MESSAGE_SEND_TASK).execute(message);
        addMessageToList(message);
        resetBottomArea();
    }

    private void addMessageToList(Message message) {
        if(message.getMessageStatus() != MessageStatus.READ && !message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {
            MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
            messageStatusUpdate.setUserId(message.getSentBy());
            messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
            messageStatusUpdate.setMessageId(message.getMessageId());
            messageStatusUpdate.setMessageStatus(MessageStatus.READ);
            new OutgoingMessageStatusUpdateTask(this).execute(messageStatusUpdate);
        }
        LinearLayout chatBubble = getChatBubble(message);
        messageListLayout.addView(chatBubble);
    }

    //====================================Subscribe To Events Start===========================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getMessage().getSentBy().equals(activeChat.getId())) {
            Log.d(TAG, "Received a new message");
            addMessageToList(event.getMessage());
            messageList.add(event.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageUpdate(MessageStatusUpdateEvent event) {
        if (event.getMessageStatusUpdate().getUserId().equals(activeChat.getId())) {
            MessageStatusUpdate messageStatusUpdate = event.getMessageStatusUpdate();
            for (int i = messageListLayout.getChildCount() - 1; i >= 0; i--) {
                Log.d(TAG, "Loop Run " + i);
                LinearLayout l = (LinearLayout) messageListLayout.getChildAt(i);
                if (((TextView) l.findViewById(R.id.messageId)).getText().toString().equals(messageStatusUpdate.getMessageId())) {
                    ImageView imageView = l.findViewById(R.id.messageStatusImageView);
                    setMessageStatusToImageView(imageView, messageStatusUpdate.getMessageStatus());
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAllMessagesForUserEvent(GetMessagesForUserEvent event) {
        Log.d(TAG, "Received Messages");
        this.messageList = event.getMessageList();
        for (int i = messageList.size() - 1; i >= 0; i--) {
            addMessageToList(messageList.get(i));
        }
        initialMessageLoaded = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadMessagesByUserEvent(UnreadMessagesByUserEvent event){
        Log.d(TAG, "Received Unread Messages");
        for (int i = event.getMessageList().size() - 1; i >= 0; i--) {
            addMessageToList(event.getMessageList().get(i));
            this.messageList.add(event.getMessageList().get(i));
        }
    }

    //====================================Subscribe To Events Complete===========================


    private Message getMessage() {
        Message message = new Message();
        message.setMessageType(MessageTypes.MESSAGE_TYPE_TEXT);
        message.setTimeStamp(System.currentTimeMillis());
        message.setMessage(messageArea.getText().toString());
        message.setSentTo(activeChat.getId());
        message.setSentBy(mAuth.getCurrentUser().getUid());
        message.setMessageStatus(MessageStatus.SENDING);
        message.setExtras("");
        message.setMessageId(UUID.randomUUID().toString());
        return message;
    }

    private LinearLayout getChatBubble(Message message) {
        chatBubbleLayoutParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutInflater = LayoutInflater.from(ChatActivity.this);
        LinearLayout chatBubble = null;
        if (message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.chat_bubble_right, null, false);
            ImageView messageStatusImageView = chatBubble.findViewById(R.id.messageStatusImageView);
            setMessageStatusToImageView(messageStatusImageView, message.getMessageStatus());
            chatBubbleLayoutParams.gravity = Gravity.RIGHT;
            chatBubbleLayoutParams.setMargins(100, 0, 0, 20);
            chatBubble.setBackgroundResource(R.drawable.roundleft);
        } else {
            chatBubble = (LinearLayout) layoutInflater.inflate(R.layout.chat_bubble_left, null, false);
            chatBubbleLayoutParams.gravity = Gravity.LEFT;
            chatBubbleLayoutParams.setMargins(0, 0, 100, 20);
            chatBubble.setBackgroundResource(R.drawable.roundright);
        }
        TextView messageTextView = chatBubble.findViewById(R.id.messageTextView);
        TextView timeStamTextView = chatBubble.findViewById(R.id.timeStampTextView);
        TextView messageIdTextView = chatBubble.findViewById(R.id.messageId);
        timeStamTextView.setText(TimestampUtil.getTimeIn12HourFormat(String.valueOf(message.getTimeStamp())));
        messageTextView.setText(message.getMessage());
        messageIdTextView.setText(message.getMessageId());
        messageTextView.setPadding(20, 20, 20, 20);
        messageTextView.setTextColor(Color.parseColor("#ffffff")); //Remove This to XML File
        chatBubble.setLayoutParams(chatBubbleLayoutParams);
        return chatBubble;
    }

    private void setMessageStatusToImageView(ImageView messageStatusImageView, int messageStatus) {
        switch (messageStatus) {
            case MessageStatus.SENT:
                messageStatusImageView.setImageResource(R.drawable.ic_done_white_14dp);
                break;
            case MessageStatus.DELIVERED:
                messageStatusImageView.setImageResource(R.drawable.ic_done_all_white_14dp);
                break;
            case MessageStatus.READ:
                messageStatusImageView.setImageResource(R.drawable.ic_done_all_blue_24dp);
                break;
        }
    }

    private void resetBottomArea() {
        messageArea.setText("");
    }

    private void scrollToBottom() {
        View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
        int sy = scrollView.getScrollY();
        int sh = scrollView.getHeight();
        int delta = bottom - (sy + sh);
        scrollView.smoothScrollBy(0, delta);
    }


    private class TypingThread extends Thread {
        int count = 0;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                    count++;
                    if (count == 20) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("typing_status")
                                .child(activeChat.getId())
                                .child(mAuth.getCurrentUser().getUid()).removeValue();
                        count = 0;
                    }
                } catch (InterruptedException e) {

                }
            }

        }
    }
}
