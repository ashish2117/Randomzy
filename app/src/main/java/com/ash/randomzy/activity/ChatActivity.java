package com.ash.randomzy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ash.randomzy.R;
import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.asynctask.LocalUserAsyncTask;
import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.asynctask.OutgoingMessageStatusUpdateTask;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.RealTimeDbNodes;
import com.ash.randomzy.event.ImageMessageProgressEvent;
import com.ash.randomzy.event.TypingEvent;
import com.ash.randomzy.event.UnreadMessagesByUserEvent;
import com.ash.randomzy.model.ActiveChat;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetMessagesForUserEvent;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.model.TypingStatus;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.ActiveChatUtil;
import com.ash.randomzy.utility.ChatBubbleUtil;
import com.ash.randomzy.utility.ImageUploader;
import com.ash.randomzy.utility.MessageStatusUtil;
import com.ash.randomzy.utility.TimestampUtil;
import com.ash.randomzy.utility.UserUtil;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.*;

public class ChatActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private ActiveChat activeChat;

    private boolean initialMessageLoaded;
    private boolean isWindowFocused;

    private ScrollView scrollView;
    private ImageView sendButton;
    private EditText messageArea;
    private LinearLayout typingStatus, messageListLayout;
    private View customView;
    private TextView userNameTextView, onlineStatusTextView;
    private Menu menu;
    private View.OnClickListener onMessageClicked;
    private View.OnLongClickListener onLongClickListener;


    private Map<String, LinearLayout> messageLayoutMap;
    private Map<String, Message> messages;
    private Map<String, Message> selectedMessages;
    private CountDownTimer timer;

    private DatabaseReference fireAndForgetRef;
    private DatabaseReference onlineStatusRef;

    private static final String TAG = "randomzy_debug";
    private static final String TYPING_TEXT = "Typing...";
    private static final int PICK_IMAGE_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initialMessageLoaded = false;
        mAuth = FirebaseAuth.getInstance();
        activeChat = new Gson().fromJson(getIntent().getStringExtra("activeChat"), ActiveChat.class);
        messageLayoutMap = new HashMap<>(50);
        messages = new HashMap<>(50);
        selectedMessages = new HashMap<>(10);
        isWindowFocused = true;
        initViews();
        addListeners();
        registerToEvents();
        populateMessageList();
        initDbReferences();
        addDbReferenceListeners();
    }


    @Override
    protected void onStart() {
        super.onStart();
        UserUtil.setChatOpenedFor(activeChat.getId());
        if (initialMessageLoaded)
            new MessageAsyncTask(getApplicationContext(), MessageAsyncTask.GET_UNREAD_MESSAGES_BY_USER, activeChat.getId()).execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserUtil.setChatOpenedFor("");
    }

    @Override
    public void onBackPressed() {
        if (selectedMessages.size() == 0) {
            super.onBackPressed();
            unRegisterToEvents();
        } else {
            Iterator<String> iterator = selectedMessages.keySet().iterator();
            while (iterator.hasNext()) {
                String messageId = iterator.next();
                unSelectMessage(requireNonNull(messageLayoutMap.get(messageId)), false);
                iterator.remove();
            }
            setIsFavOption();
        }
    }

    @Override
    protected void onPause() {
        Log.d("randomzy_debug", "Paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_activity_menu, menu);
        this.menu = menu;
        setIsFavOption();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_chat:
                break;
            case R.id.attachFile:
                attachmentSelected();
                break;
            case R.id.deleteOrFav:
                deleteOrFav();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteOrFav(){
        if(selectedMessages.size() == 0)
            toggleIsFav();
        else
            deleteSelectedMessages();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.isWindowFocused = hasFocus;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Message message = new Message();
            message.setMessageStatus(MessageStatus.SENDING);
            message.setMessage("");
            message.setMessageId(UUID.randomUUID().toString());
            message.setSentBy(mAuth.getCurrentUser().getUid());
            message.setSentTo(activeChat.getId());
            message.setTimeStamp(System.currentTimeMillis());
            message.setMessageType(MessageTypes.MESSAGE_TYPE_IMAGE);
            JSONObject jsonObject = null;
            LinearLayout imageChatBubble = null;
            try {
                jsonObject = new JSONObject().put("imageUri", imageUri.toString());
                message.setExtras(jsonObject.toString());
                imageChatBubble = ChatBubbleUtil.getImageMessageChatBubble(message, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addChatBubbleToMessageListLayout(imageChatBubble, message.getMessageId());
            messages.put(message.getMessageId(), message);
            new MessageAsyncTask(this, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
            ImageUploader.uploadImageMessage(imageUri, message);
        }
    }

    private void setIsFavOption(){
        MenuItem deleteOrFav = menu.findItem(R.id.deleteOrFav);
        if(activeChat.getIsFav() == 0)
            deleteOrFav.setIcon(getResources().getDrawable(R.drawable.ic_star_border_white_24dp));
        else
            deleteOrFav.setIcon(getResources().getDrawable(R.drawable.ic_star_white_24dp));
    }

    private void attachmentSelected() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    public void deleteSelectedMessages() {
        if (selectedMessages.size() != 0) {
            for (Map.Entry entry : selectedMessages.entrySet()) {
                messageListLayout.removeView(messageLayoutMap.get(entry.getKey()));
                messages.remove(entry.getKey());
            }
            setIsFavOption();
            new AsyncTask<Set<String>, Void, Void>() {

                @Override
                protected Void doInBackground(Set<String>... sets) {
                    new MessageRepository(ChatActivity.this).deleteMultipleMessages(sets[0]);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    selectedMessages.clear();
                    new ActiveChatAsyncTask(ChatActivity.this,ActiveChatAsyncTask.POST_FAV_AND_ALL_ACTIVE_CHAT).execute();
                    super.onPostExecute(aVoid);
                }
            }.execute(selectedMessages.keySet());
        }
    }

    private void toggleIsFav() {
        if(activeChat.getIsFav() == 0) {
            activeChat.setIsFav(1);
            menu.findItem(R.id.deleteOrFav).setIcon(getResources().getDrawable(R.drawable.ic_star_white_24dp));
        }
        else {
            activeChat.setIsFav(0);
            menu.findItem(R.id.deleteOrFav).setIcon(getResources().getDrawable(R.drawable.ic_star_border_white_24dp));
        }
        new LocalUserAsyncTask(this, LocalUserAsyncTask.UPDATE_IS_FAV)
                .execute(ActiveChatUtil.getLocalUserFromActiveChat(activeChat));
    }

    private void initViews() {
        scrollView = findViewById(R.id.scrollView);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        messageListLayout = findViewById(R.id.message_list_layout);
        typingStatus = findViewById(R.id.typingStatus);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_chat_activity);
        customView = getSupportActionBar().getCustomView();
        userNameTextView = customView.findViewById(R.id.usernameText);
        onlineStatusTextView = customView.findViewById(R.id.onlineStatusText);
        typingStatus.setVisibility(View.GONE);
        userNameTextView.setText(activeChat.getName());
    }

    private void addListeners() {

        sendButton.setOnClickListener((view -> {
            sendMessage();
        }));

        scrollView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            scrollToBottom();
        });

        customView.findViewById(R.id.backButton).setOnClickListener((view -> {
            finish();
        }));

        messageArea.addTextChangedListener(new TextWatcher() {
            private long lastSent = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (System.currentTimeMillis() - lastSent > 1500) {
                    TypingStatus typingStatus = new TypingStatus();
                    typingStatus.setUserId(mAuth.getCurrentUser().getUid());
                    fireAndForgetRef.push().setValue(typingStatus);
                    lastSent = System.currentTimeMillis();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        onMessageClicked = (view) -> {
            if (selectedMessages.size() == 0) {
                if (!(view instanceof ImageView))
                    return;
                ImageView imageView = (ImageView) view;
                Intent intent = new Intent(this, ImageViewerActivity.class);
                intent.putExtra("imageUri", imageView.getTag().toString());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
                startActivity(intent, options.toBundle());
            } else {
                toggleMessageSelection(view);
            }
        };

        onLongClickListener = (view) -> {
            if (selectedMessages.size() == 0) {
                toggleMessageSelection(view);
                return true;
            }
            return false;
        };
    }

    private boolean toggleMessageSelection(View view) {
        LinearLayout linearLayout = null;
        if (view instanceof LinearLayout) {
            linearLayout = (LinearLayout) view;
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            linearLayout = (LinearLayout) ((FrameLayout) imageView.getParent()).getParent();
        }
        String messageId = linearLayout.getTag().toString();
        if (selectedMessages.containsKey(messageId))
            unSelectMessage(linearLayout, true);
        else
            selectMessage(linearLayout);
        return true;
    }

    private void selectMessage(LinearLayout chatBubble) {
        if (selectedMessages.size() == 0)
            menu.findItem(R.id.deleteOrFav).setIcon(getResources().getDrawable(R.drawable.ic_delete_white_24dp));
        String messageId = chatBubble.getTag().toString();
        selectedMessages.put(messageId, messages.get(messageId));
        chatBubble.setBackground(getResources().getDrawable(R.drawable.rounded_corners_blue_25dp_raduis));
        ((TextView) chatBubble.findViewById(R.id.messageTextView)).setTextColor(getResources().getColor(R.color.white));
    }

    private void unSelectMessage(LinearLayout chatBubble, boolean removeFromSelectedMessagesMap) {
        String messageId = chatBubble.getTag().toString();
        if (removeFromSelectedMessagesMap)
            selectedMessages.remove(messageId);
        if (chatBubble.findViewById(R.id.messageStatusImageView) != null)
            chatBubble.setBackground(getResources().getDrawable(R.drawable.right_message_bg_25dp_radius));
        else
            chatBubble.setBackground(getResources().getDrawable(R.drawable.left_message_bg_25dp_radius));
        ((TextView) chatBubble.findViewById(R.id.messageTextView)).setTextColor(getResources().getColor(R.color.colorTextPrimary));
        if (selectedMessages.size() == 0)
            setIsFavOption();
    }

    private void populateMessageList() {
        new MessageAsyncTask(this, MessageAsyncTask.GET_MESSAGES_FOR_USER_TASK, activeChat.getId()).execute();
    }

    private void initDbReferences() {
        fireAndForgetRef = FirebaseDatabase.getInstance()
                .getReference(RealTimeDbNodes.FIRE_AND_FORGET_NODE)
                .child(activeChat.getId());
        onlineStatusRef = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.USERS_NODE)
                .child(activeChat.getId()).child(RealTimeDbNodes.ONLINE_STATUS_NODE);
    }

    private void addDbReferenceListeners() {
        onlineStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object status = dataSnapshot.getValue();
                if (status instanceof String)
                    onlineStatusTextView.setText("Online");
                else
                    onlineStatusTextView.setText("Last Seen " + TimestampUtil.getTimeIn12HourFormat((Long) status));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void registerToEvents() {
        EventBus.getDefault().register(this);
        Log.d(TAG, "Registered EventBus");
    }

    private void unRegisterToEvents() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Unregistered EventBus");
    }


    private void sendMessage() {
        Message message = getMessage();
        if (message == null)
            return;
        new MessageAsyncTask(this, MessageAsyncTask.MESSAGE_SEND_TASK).execute(message);
        addMessageToList(message);
        resetBottomArea();
    }

    private void addMessageToList(Message message) {
        if (isWindowFocused && message.getMessageStatus() != MessageStatus.READ && !message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {
            MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
            messageStatusUpdate.setUserId(message.getSentBy());
            messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
            messageStatusUpdate.setMessageId(message.getMessageId());
            messageStatusUpdate.setMessageStatus(MessageStatus.READ);
            messageStatusUpdate.setForMessageType(message.getMessageType());
            EventBus.getDefault().post(new MessageStatusUpdateEvent(messageStatusUpdate,
                    mAuth.getCurrentUser().getUid(), message.getSentBy()));
            new OutgoingMessageStatusUpdateTask(this).execute(messageStatusUpdate);
        }
        LinearLayout chatBubble = null;
        if (message.getMessageType() == MessageTypes.MESSAGE_TYPE_TEXT) {
            chatBubble = ChatBubbleUtil.getTextMessageChatBubble(message, this);
            chatBubble.setOnLongClickListener(onLongClickListener);
            chatBubble.setOnClickListener(onMessageClicked);
        } else if (message.getMessageType() == MessageTypes.MESSAGE_TYPE_IMAGE) {
            try {
                chatBubble = ChatBubbleUtil.getImageMessageChatBubble(message, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ImageView imageView = chatBubble.findViewById(R.id.image_message_image_view);
            imageView.setOnClickListener(onMessageClicked);
            imageView.setOnLongClickListener(onLongClickListener);
        }
        addChatBubbleToMessageListLayout(chatBubble, message.getMessageId());
        messages.put(message.getMessageId(), message);
    }

    private void addChatBubbleToMessageListLayout(LinearLayout chatBubble, String messageId) {
        chatBubble.setTag(messageId);
        messageListLayout.addView(chatBubble);
        messageLayoutMap.put(messageId, chatBubble);
    }

    //====================================Subscribe To Events Start===========================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getMessage().getSentBy().equals(activeChat.getId())) {
            Log.d(TAG, "Received a new message");
            addMessageToList(event.getMessage());
            onlineStatusTextView.setText("Online");
            if (timer != null)
                timer.cancel();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageUpdate(MessageStatusUpdateEvent event) {
        Log.d(this.getClass().getName(), event.getMessageStatusUpdate().toString());
        MessageStatusUpdate messageStatusUpdate = event.getMessageStatusUpdate();
        if (event.getMessageStatusUpdate().getUserId().equals(activeChat.getId())
                && (!event.getOriginator().equals(mAuth.getCurrentUser().getUid())
                || messageStatusUpdate.getMessageStatus() == MessageStatus.SENT)) {
            LinearLayout l = messageLayoutMap.get(messageStatusUpdate.getMessageId());
            if (l != null) {
                ImageView imageView = l.findViewById(R.id.messageStatusImageView);
                if (messageStatusUpdate.getForMessageType() == MessageTypes.MESSAGE_TYPE_TEXT)
                    MessageStatusUtil.setMessageStatusToImageView(imageView, messageStatusUpdate.getMessageStatus());
                else if (messageStatusUpdate.getForMessageType() == MessageTypes.MESSAGE_TYPE_IMAGE) {
                    MessageStatusUtil.setMessageStatusToImageViewForImageMessage(imageView, messageStatusUpdate.getMessageStatus());
                    CircleProgressBar progressBar = l.findViewById(R.id.progress_circ);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAllMessagesForUserEvent(GetMessagesForUserEvent event) {
        Log.d(TAG, "Received Messages " + event.getMessageList().size());
        for (int i = event.getMessageList().size() - 1; i >= 0; i--) {
            addMessageToList(event.getMessageList().get(i));
        }
        initialMessageLoaded = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadMessagesByUserEvent(UnreadMessagesByUserEvent event) {
        Log.d(TAG, "Received Unread Messages");
        for (int i = event.getMessageList().size() - 1; i >= 0; i--) {
            Message message = event.getMessageList().get(i);
            MessageStatusUpdate messageStatusUpdate = new MessageStatusUpdate();
            messageStatusUpdate.setUserId(message.getSentBy());
            messageStatusUpdate.setTimeStamp(System.currentTimeMillis());
            messageStatusUpdate.setMessageId(message.getMessageId());
            messageStatusUpdate.setMessageStatus(MessageStatus.READ);
            messageStatusUpdate.setForMessageType(message.getMessageType());
            EventBus.getDefault().post(new MessageStatusUpdateEvent(messageStatusUpdate,
                    mAuth.getCurrentUser().getUid(), message.getSentBy()));
            new OutgoingMessageStatusUpdateTask(this).execute(messageStatusUpdate);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTyping(TypingEvent typingEvent) {
        if (typingEvent.getTypingStatus().getUserId().equals(activeChat.getId())) {
            onlineStatusTextView.setText(TYPING_TEXT);
            if (timer != null)
                timer.cancel();
            timer = new CountDownTimer(1500, 1500) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    onlineStatusTextView.setText("Online");
                }
            };
            timer.start();
        }
    }

    @Subscribe
    public void onImageMessageProgress(ImageMessageProgressEvent event) {
        LinearLayout chatBubble = messageLayoutMap.get(event.getImageMessage().getMessageId());
        if (chatBubble != null) {
            CircleProgressBar progressBar = chatBubble.findViewById(R.id.progress_circ);
            progressBar.setProgress(event.getProgress());
        }
    }
    //====================================Subscribe To Events Complete===========================

    public void showImage(View view) {
        Toast.makeText(this, "hello", Toast.LENGTH_SHORT);
    }

    private Message getMessage() {
        if (messageArea.getText().toString().isEmpty())
            return null;
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

}
