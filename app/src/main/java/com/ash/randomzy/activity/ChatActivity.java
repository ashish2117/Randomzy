package com.ash.randomzy.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ash.randomzy.R;
import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.asynctask.LocalUserAsyncTask;
import com.ash.randomzy.asynctask.MessageAsyncTask;
import com.ash.randomzy.asynctask.OutgoingMessageStatusUpdateTask;
import com.ash.randomzy.constants.LocalStoragePaths;
import com.ash.randomzy.constants.MediaProgress;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.MessageTypes;
import com.ash.randomzy.constants.RealTimeDbNodes;
import com.ash.randomzy.db.FirebaseStorageManager;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetMessagesForUserEvent;
import com.ash.randomzy.event.ImageMessageDownloadedEvent;
import com.ash.randomzy.event.ImageMessageProgressEvent;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.event.TypingEvent;
import com.ash.randomzy.event.UnreadMessagesByUserEvent;
import com.ash.randomzy.model.ActiveChat;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.ash.randomzy.model.TypingStatus;
import com.ash.randomzy.repository.MessageRepository;
import com.ash.randomzy.utility.ActiveChatUtil;
import com.ash.randomzy.utility.ChatBubbleUtil;
import com.ash.randomzy.utility.MessageStatusUtil;
import com.ash.randomzy.utility.NetworkUtil;
import com.ash.randomzy.utility.TimestampUtil;
import com.ash.randomzy.utility.UserUtil;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import de.hdodenhof.circleimageview.CircleImageView;

import static java.util.Objects.requireNonNull;

public class ChatActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private ActiveChat activeChat;

    private boolean initialMessageLoaded;
    private boolean isWindowFocused;
    private boolean isSendCircleImageViewLongPressed;
    private String voiceNoteOutputFile;
    private Long voiceNoteRecStartedAt;

    private ScrollView scrollView;
    private ImageView sendButton;
    private EditText messageArea;
    private LinearLayout typingStatus, messageListLayout;
    private View customView;
    private TextView userNameTextView, onlineStatusTextView;
    private CircleImageView sendCircleImageView;

    private Menu menu;

    private View.OnClickListener onMessageClicked;
    private View.OnLongClickListener onLongClickListener;
    private View.OnClickListener onDownloadImageClicked;
    private View.OnClickListener onAudioPlaybackClicked;


    private Map<String, LinearLayout> messageLayoutMap;
    private Map<String, Message> messages;
    private Map<String, Message> selectedMessages;
    private CountDownTimer timer;

    private DatabaseReference fireAndForgetRef;
    private DatabaseReference onlineStatusRef;

    private static final String TAG = "randomzy_debug";
    private static final String TYPING_TEXT = "Typing...";
    private static final int PICK_IMAGE_REQUEST = 0;

    private String playingVoiceNoteForMessageId;
    private Handler handlerToPlayVoiceNote;
    private Runnable runnableToPlayVoiceNote;

    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;

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
        isSendCircleImageViewLongPressed = false;
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    private void deleteOrFav() {
        if (selectedMessages.size() == 0)
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
            message.setMessageType(MessageTypes.IMAGE);
            JSONObject jsonObject = null;
            LinearLayout imageChatBubble = null;
            try {
                jsonObject = new JSONObject().put("imageUri", imageUri.toString());
                message.setExtras(jsonObject.toString());
                imageChatBubble = ChatBubbleUtil.getImageMessageChatBubble(message, this);
                ImageView imageView = imageChatBubble.findViewById(R.id.image_message_image_view);
                imageView.setOnClickListener(onMessageClicked);
                imageView.setOnLongClickListener(onLongClickListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addChatBubbleToMessageListLayout(imageChatBubble, message);
            messages.put(message.getMessageId(), message);
            new MessageAsyncTask(this, MessageAsyncTask.IMAGE_MESSAGE_SEND_TASK, imageUri).execute(message);
        }
    }

    private void setIsFavOption() {
        MenuItem deleteOrFav = menu.findItem(R.id.deleteOrFav);
        if (activeChat.getIsFav() == 0)
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
                    new ActiveChatAsyncTask(ChatActivity.this, ActiveChatAsyncTask.POST_FAV_AND_ALL_ACTIVE_CHAT).execute();
                    super.onPostExecute(aVoid);
                }
            }.execute(selectedMessages.keySet());
        }
    }

    private void toggleIsFav() {
        if (activeChat.getIsFav() == 0) {
            activeChat.setIsFav(1);
            menu.findItem(R.id.deleteOrFav).setIcon(getResources().getDrawable(R.drawable.ic_star_white_24dp));
        } else {
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
        sendCircleImageView = findViewById(R.id.send_circle_image_view);

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

        sendCircleImageView.setOnLongClickListener((view -> {
            recordVoiceNote();
            return true;
        }));

        sendCircleImageView.setOnTouchListener(((view, motionEvent) -> {
            return sendCircleImageViewTouched(view, motionEvent);
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
            Message message = (Message) view.getTag();
            if (message == null)
                if (selectedMessages.size() == 0) {
                    if (!(view instanceof ImageView))
                        return;
                    ImageView imageView = (ImageView) view;
                    Intent intent = new Intent(this, ImageViewerActivity.class);
                    intent.putExtra("imageUri", imageView.getTag(R.string.image_uri).toString());
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

        onDownloadImageClicked = view -> {
            if (NetworkUtil.isConnectedToNetwork(this)) {
                LinearLayout chatBubble = (LinearLayout) ((FrameLayout) view.getParent()).getParent();
                Message message = (Message) chatBubble.getTag();
                new FirebaseStorageManager().downloadImageMessageOriginalImage(message, ChatActivity.this);
                view.setVisibility(View.GONE);
                CircleProgressBar circleProgressBar = chatBubble.findViewById(R.id.progress_circ);
                circleProgressBar.setVisibility(View.VISIBLE);
            } else
                Toast.makeText(this, "Not connected to Internet", Toast.LENGTH_LONG).show();
        };

        onAudioPlaybackClicked = view -> {
            audioPlaybackClicked(view);
        };
    }

    private void audioPlaybackClicked(View view) {
        ImageView playBackImageView = (ImageView) view;
        LinearLayout chatBubble = (LinearLayout) playBackImageView.getParent().getParent();
        Message message = (Message) chatBubble.getTag();
        try {
            JSONObject extras = new JSONObject(message.getExtras());
            if (!message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {

                if (!extras.has("progress")) {
                    downloadVoiceNoteAndPlay(message, chatBubble, extras, playBackImageView);
                    return;
                }
            }
            playVoiceNote(message, chatBubble, extras, playBackImageView);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void downloadVoiceNoteAndPlay(Message message, LinearLayout chatBubble, JSONObject extras, ImageView playBackImageView) {
        String filePath = null;
        try {
            filePath = extras.getString("voiceNoteUri");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (filePath == null)
            return;
        String str[] = filePath.split("/");
        String filename = str[str.length - 1];
        File file = new File(LocalStoragePaths.getVoiceNotesPath(this)
                + File.separator
                + filename);
        Log.d("testtt", file.getAbsolutePath());
        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();
        chatBubble.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        playBackImageView.setVisibility(View.INVISIBLE);
        firebaseStorage.child(filePath).getFile(file).addOnSuccessListener((taskSnapshot -> {
            try {
                extras.put("voiceNoteUri", Uri.fromFile(file).toString());
                extras.put("progress", MediaProgress.MEDIA_DOWNLOADED_PROGRESS_VALUE);
                message.setExtras(extras.toString());
                chatBubble.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                playBackImageView.setVisibility(View.VISIBLE);
                playVoiceNote(message, chatBubble, extras, playBackImageView);
                new MessageAsyncTask(this, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    private void playVoiceNote(Message message, LinearLayout chatBubble, JSONObject extras, ImageView playBackImageView) {
        if (playingVoiceNoteForMessageId != null && !playingVoiceNoteForMessageId.equals(message.getMessageId())) {
            resetVoiceNoteChatBubble(messages.get(playingVoiceNoteForMessageId));
            resetMediaPlayer();
        }
        SeekBar seekBar = chatBubble.findViewById(R.id.voice_note_seek_bar);
        TextView textView = chatBubble.findViewById(R.id.voice_note_length_text_view);
        try {
            String voiceNoteUri = extras.getString("voiceNoteUri");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playBackImageView.setImageResource(R.drawable.ic_play_arrow_gray_24dp);
                return;
            } else if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 0) {
                mediaPlayer.start();
                playBackImageView.setImageResource(R.drawable.ic_pause_gray_24dp);
                return;
            }
            mediaPlayer = new MediaPlayer();
            handlerToPlayVoiceNote = new Handler();
            runnableToPlayVoiceNote = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        long mCurrentPosition = mediaPlayer.getCurrentPosition();
                        int mCurrentPositionInSec = (int) mCurrentPosition / 500;
                        seekBar.setProgress(mCurrentPositionInSec);
                        textView.setText(TimestampUtil.getMinutesInString(mediaPlayer.getCurrentPosition()));
                    }
                    handlerToPlayVoiceNote.postDelayed(this, 500);
                }
            };
            mediaPlayer.setDataSource(voiceNoteUri);
            mediaPlayer.prepare();
            seekBar.setMax((mediaPlayer.getDuration() / 1000) * 2);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        mediaPlayer.seekTo(seekBar.getProgress() * 500);
                }
            });
            mediaPlayer.setOnCompletionListener((m) -> {
                playBackImageView.setImageResource(R.drawable.ic_play_arrow_gray_24dp);
                seekBar.setProgress(0);
                resetMediaPlayer();
            });
            mediaPlayer.start();
            playingVoiceNoteForMessageId = message.getMessageId();
            playBackImageView.setImageResource(R.drawable.ic_pause_gray_24dp);
            textView.setText(TimestampUtil.getMinutesInString(0));
            ChatActivity.this.runOnUiThread(runnableToPlayVoiceNote);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void resetMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        handlerToPlayVoiceNote.removeCallbacks(runnableToPlayVoiceNote);
        runnableToPlayVoiceNote = null;
        handlerToPlayVoiceNote = null;
        playingVoiceNoteForMessageId = null;
    }

    private void resetVoiceNoteChatBubble(Message message) {
        LinearLayout chatBubble = messageLayoutMap.get(message.getMessageId());
        JSONObject extras = null;
        try {
            extras = new JSONObject(message.getExtras());
            ((TextView) chatBubble.findViewById(R.id.voice_note_length_text_view))
                    .setText(TimestampUtil.getMinutesInString(extras.getLong("lengthInMillis")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((SeekBar) chatBubble.findViewById(R.id.voice_note_seek_bar)).setProgress(0);
        ((ImageView) chatBubble.findViewById(R.id.voice_note_playback_image_view)).setImageResource(R.drawable.ic_play_arrow_gray_24dp);
    }

    private void recordVoiceNote() {
        if (!isSendCircleImageViewLongPressed) {
            isSendCircleImageViewLongPressed = true;
            voiceNoteOutputFile = LocalStoragePaths.getVoiceNotesPath(this)
                    + File.separator
                    + System.currentTimeMillis()
                    + ".3gp";
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myAudioRecorder.setOutputFile(voiceNoteOutputFile);
            try {
                myAudioRecorder.prepare();
                myAudioRecorder.start();
                voiceNoteRecStartedAt = System.currentTimeMillis();
                Toast.makeText(this, "Recording Audio", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sendCircleImageViewTouched(View view, MotionEvent event) {
        view.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isSendCircleImageViewLongPressed) {
                myAudioRecorder.stop();
                Long fileLengthInMillis = System.currentTimeMillis() - voiceNoteRecStartedAt;
                myAudioRecorder.release();
                myAudioRecorder = null;
                Uri voiceNoteUri = Uri.fromFile(new File(voiceNoteOutputFile));
                Message message = new Message();
                message.setMessage("");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("voiceNoteUri", voiceNoteOutputFile);
                    jsonObject.put("lengthInMillis", fileLengthInMillis);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message.setExtras(jsonObject.toString());
                message.setMessageType(MessageTypes.VOICE_NOTE);
                message.setTimeStamp(System.currentTimeMillis());
                message.setSentBy(mAuth.getCurrentUser().getUid());
                message.setSentTo(activeChat.getId());
                message.setMessageStatus(MessageStatus.SENDING);
                message.setMessageId(UUID.randomUUID().toString());
                new MessageAsyncTask(this, MessageAsyncTask.INSERT_MESSAGE_TO_DB).execute(message);
                addMessageToList(message);
                FirebaseStorageManager.sendVoiceNote(message, voiceNoteUri, this, fileLengthInMillis);
                isSendCircleImageViewLongPressed = false;
            }
        }
        return false;
    }

    private boolean toggleMessageSelection(View view) {
        LinearLayout linearLayout = null;
        if (view instanceof LinearLayout) {
            linearLayout = (LinearLayout) view;
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            linearLayout = (LinearLayout) ((FrameLayout) imageView.getParent()).getParent();
        }
        String messageId = ((Message) linearLayout.getTag()).getMessageId();
        if (selectedMessages.containsKey(messageId))
            unSelectMessage(linearLayout, true);
        else
            selectMessage(linearLayout);
        return true;
    }

    private void selectMessage(LinearLayout chatBubble) {
        if (selectedMessages.size() == 0)
            menu.findItem(R.id.deleteOrFav).setIcon(getResources().getDrawable(R.drawable.ic_delete_white_24dp));
        Message message = (Message) chatBubble.getTag();
        selectedMessages.put(message.getMessageId(), message);
        chatBubble.setBackground(getResources().getDrawable(R.drawable.rounded_corners_blue_25dp_raduis));
        if (!(message.getMessageType() == MessageTypes.VOICE_NOTE))
            ((TextView) chatBubble.findViewById(R.id.messageTextView)).setTextColor(getResources().getColor(R.color.white));
    }

    private void unSelectMessage(LinearLayout chatBubble, boolean removeFromSelectedMessagesMap) {
        Message message = (Message) chatBubble.getTag();
        String messageId = message.getMessageId();
        if (removeFromSelectedMessagesMap)
            selectedMessages.remove(messageId);
        if (chatBubble.findViewById(R.id.messageStatusImageView) != null)
            chatBubble.setBackground(getResources().getDrawable(R.drawable.right_message_bg_25dp_radius));
        else
            chatBubble.setBackground(getResources().getDrawable(R.drawable.left_message_bg_25dp_radius));
        if (!(message.getMessageType() == MessageTypes.VOICE_NOTE))
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
        new MessageAsyncTask(this, MessageAsyncTask.TEXT_MESSAGE_SEND_TASK).execute(message);
        addMessageToList(message);
        resetBottomArea();
    }

    private void addMessageToList(Message message) {
        if (isWindowFocused && message.getMessageStatus() != MessageStatus.READ
                && !message.getSentBy().equals(mAuth.getCurrentUser().getUid())) {
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
        if (message.getMessageType() == MessageTypes.TEXT) {
            chatBubble = ChatBubbleUtil.getTextMessageChatBubble(message, this);
            chatBubble.setOnLongClickListener(onLongClickListener);
            chatBubble.setOnClickListener(onMessageClicked);
        } else if (message.getMessageType() == MessageTypes.IMAGE) {
            chatBubble = ChatBubbleUtil.getImageMessageChatBubble(message, this);
            ImageView imageView = chatBubble.findViewById(R.id.image_message_image_view);
            imageView.setOnClickListener(onMessageClicked);
            imageView.setOnLongClickListener(onLongClickListener);
            ImageView downloadImageView = chatBubble.findViewById(R.id.downLoadImage);
            if (downloadImageView != null)
                downloadImageView.setOnClickListener(onDownloadImageClicked);
        } else if (message.getMessageType() == MessageTypes.VOICE_NOTE) {
            chatBubble = ChatBubbleUtil.getVoiceNoteChatBubble(message, this);
            chatBubble.setOnLongClickListener(onLongClickListener);
            chatBubble.setOnClickListener(onMessageClicked);
            chatBubble.findViewById(R.id.voice_note_playback_image_view).setOnClickListener(onAudioPlaybackClicked);
        }
        addChatBubbleToMessageListLayout(chatBubble, message);
        messages.put(message.getMessageId(), message);
    }

    private void addChatBubbleToMessageListLayout(LinearLayout chatBubble, Message message) {
        chatBubble.setTag(message);
        messageListLayout.addView(chatBubble);
        messageLayoutMap.put(message.getMessageId(), chatBubble);
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
            LinearLayout chatBubble = messageLayoutMap.get(messageStatusUpdate.getMessageId());
            if (chatBubble != null) {
                ImageView imageView = chatBubble.findViewById(R.id.messageStatusImageView);
                if (messageStatusUpdate.getForMessageType() == MessageTypes.TEXT)
                    MessageStatusUtil.setMessageStatusToImageView(imageView, messageStatusUpdate.getMessageStatus());
                else if (messageStatusUpdate.getForMessageType() == MessageTypes.IMAGE) {
                    MessageStatusUtil.setMessageStatusToImageViewForImageMessage(imageView, messageStatusUpdate.getMessageStatus());
                    CircleProgressBar progressBar = chatBubble.findViewById(R.id.progress_circ);
                    progressBar.setVisibility(View.INVISIBLE);
                } else if (messageStatusUpdate.getForMessageType() == MessageTypes.VOICE_NOTE) {
                    MessageStatusUtil.setMessageStatusToImageView(imageView, messageStatusUpdate.getMessageStatus());
                    if (messageStatusUpdate.getMessageStatus() == MessageStatus.SENT) {
                        chatBubble.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        chatBubble.findViewById(R.id.voice_note_playback_image_view).setVisibility(View.VISIBLE);
                    }
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

    @Subscribe
    public void onImageMessageDownloaded(ImageMessageDownloadedEvent event) {
        Message message = event.getMessage();
        Uri uri = null;
        try {
            JSONObject extras = new JSONObject(message.getExtras());
            uri = Uri.parse(extras.getString("fullImageUri"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (uri != null) {
            LinearLayout chatBubble = messageLayoutMap.get(message.getMessageId());
            ImageView imageView = chatBubble.findViewById(R.id.image_message_image_view);
            imageView.setImageURI(uri);
            imageView.setTag(R.string.image_uri, uri.toString());
            chatBubble.findViewById(R.id.progress_circ).setVisibility(View.GONE);
            chatBubble.setTag(message);
        }
    }

    //====================================Subscribe To Events Complete===========================


    private Message getMessage() {
        if (messageArea.getText().toString().isEmpty())
            return null;
        Message message = new Message();
        message.setMessageType(MessageTypes.TEXT);
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
