package com.ash.randomzy.activity.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ash.randomzy.R;
import com.ash.randomzy.activity.ChatActivity;
import com.ash.randomzy.adapter.ActiveChatAdapter;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.constants.SentBy;
import com.ash.randomzy.event.TypingEvent;
import com.ash.randomzy.model.ActiveChat;
import com.ash.randomzy.entity.Message;
import com.ash.randomzy.event.GetAllActiveChatEvent;
import com.ash.randomzy.event.GetAllFavActiveChat;
import com.ash.randomzy.event.MessageOutGoingEvent;
import com.ash.randomzy.event.MessageReceiveEvent;
import com.ash.randomzy.event.MessageStatusUpdateEvent;
import com.ash.randomzy.event.NewActiveChatEvent;
import com.ash.randomzy.model.MessageStatusUpdate;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private RecyclerView recyclerView;
    private ActiveChatAdapter adapter;
    private int index;
    private FirebaseAuth mAuth;
    private CountDownTimer timer;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        mAuth = FirebaseAuth.getInstance();
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        int index = getArguments().getInt(ARG_SECTION_NUMBER);
        //if (index == 1)
            //new ActiveChatAsyncTask(getContext(), ActiveChatAsyncTask.GET_ALL_ACTIVE_CHAT).execute();
        //else if (index == 2)
            //new ActiveChatAsyncTask(getContext(), ActiveChatAsyncTask.GET_ALL_FAV_ACTIVE_CHAT).execute();
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }

        });
        EventBus.getDefault().register(PlaceholderFragment.this);
        return root;
    }

    private void initAdapter(List<ActiveChat> activeChatList) {
        adapter = new ActiveChatAdapter(activeChatList, (activeChat) -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("activeChat", activeChat.toString());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    //===========================Subscribe To Events Start=========================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewActiveChat(NewActiveChatEvent event) {
        ActiveChat activeChat = event.getActiveChat();
        if ((index == 1 && activeChat.getIsFav() == 0) || (index == 2 && activeChat.getIsFav() == 1)) {
            adapter.addNewActiveChat(activeChat);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetAllActiveChatEvent(GetAllActiveChatEvent event) {
        if (index == 1) {
            initAdapter(event.getActiveChatList());
            EventBus.getDefault().removeStickyEvent(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetAllFavActiveChatEvent(GetAllFavActiveChat event) {
        if (index == 2) {
            initAdapter(event.getFavActiveChatList());
            EventBus.getDefault().removeStickyEvent(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageStatusUpdate(MessageStatusUpdateEvent messageStatusUpdateEvent) {
        MessageStatusUpdate messageStatusUpdate = messageStatusUpdateEvent.getMessageStatusUpdate();
        ActiveChatPosition activeChatPosition = getActiveChatPosition(messageStatusUpdate.getUserId());
        if (activeChatPosition != null) {
            ActiveChat activeChat = activeChatPosition.activeChat;
            activeChat.setLastTextStatus(messageStatusUpdate.getMessageStatus());
            if(messageStatusUpdate.getMessageStatus() == MessageStatus.READ
                && messageStatusUpdateEvent.getOriginator().equals(mAuth.getCurrentUser().getUid()))
                activeChat.setUnreadCount(activeChat.getUnreadCount() -1);
            adapter.notifyItemChanged(activeChatPosition.position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceive(MessageReceiveEvent messageReceiveEvent) {
        Message message = messageReceiveEvent.getMessage();
        ActiveChatPosition activeChatPosition = getActiveChatPosition(message.getSentBy());
        if (activeChatPosition != null) {
            ActiveChat activeChat = activeChatPosition.activeChat;
            activeChat.setLastText(message.getMessage());
            activeChat.setLastTextTime(message.getTimeStamp());
            activeChat.setSentBy(message.getSentBy());
            activeChat.setUnreadCount(activeChat.getUnreadCount() + 1);
            adapter.notifyItemChanged(activeChatPosition.position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageOutGoing(MessageOutGoingEvent messageOutGoingEvent) {
        Message message = messageOutGoingEvent.getMessage();
        ActiveChatPosition activeChatPosition = getActiveChatPosition(message.getSentTo());
        if (activeChatPosition != null) {
            activeChatPosition.activeChat.setLastText(message.getMessage());
            activeChatPosition.activeChat.setLastTextTime(message.getTimeStamp());
            activeChatPosition.activeChat.setSentBy(message.getSentBy());
            activeChatPosition.activeChat.setLastTextStatus(message.getMessageStatus());
            adapter.notifyItemChanged(activeChatPosition.position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTyping(TypingEvent typingEvent){
        ActiveChatPosition activeChatPosition = getActiveChatPosition(typingEvent.getTypingStatus().getUserId());
        if(activeChatPosition != null) {
            activeChatPosition.activeChat.setIsTyping(1);
            adapter.notifyItemChanged(activeChatPosition.position);
            if(timer != null)
                timer.cancel();
            timer = new CountDownTimer(1500,1500){
                @Override
                public void onTick(long l) { }

                @Override
                public void onFinish() {
                    activeChatPosition.activeChat.setIsTyping(0);
                    adapter.notifyItemChanged(activeChatPosition.position);
                }
            };
            timer.start();
        }
    }


    //===========================Subscribe To Events End===========================

    private ActiveChatPosition getActiveChatPosition(String userId) {
        ActiveChatPosition activeChatPosition = null;
        List<ActiveChat> activeChats = adapter.getActiveChatList();
        for (int i = 0; i < activeChats.size(); i++) {
            if (activeChats.get(i).getId().equals(userId)) {
                activeChatPosition = new ActiveChatPosition();
                activeChatPosition.activeChat = activeChats.get(i);
                activeChatPosition.position = i;
                break;
            }
        }
        return activeChatPosition;
    }

    private class ActiveChatPosition {
        public int position;
        public ActiveChat activeChat;
    }
}