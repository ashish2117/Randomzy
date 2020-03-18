package com.ash.randomzy.activity.ui.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ash.randomzy.asynctask.ActiveChatAsyncTask;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.event.GetAllActiveChatEvent;
import com.ash.randomzy.event.GetAllFavActiveChat;
import com.ash.randomzy.event.MessageSentEvent;
import com.ash.randomzy.event.NewActiveChatEvent;
import com.ash.randomzy.listener.OnItemClickListener;
import com.ash.randomzy.repository.ActiveChatRepository;
import com.ash.randomzy.utility.ActivityLauncher;

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

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        EventBus.getDefault().register(PlaceholderFragment.this);
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
        if (index == 1)
            new ActiveChatAsyncTask(getContext(), ActiveChatAsyncTask.GET_ALL_ACTIVE_CHAT).execute();
        else if(index == 2)
            new ActiveChatAsyncTask(getContext(), ActiveChatAsyncTask.GET_ALL_FAV_ACTIVE_CHAT).execute();
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });
        return root;
    }

    private void initAdapter(List<ActiveChat> activeChatList) {
        adapter = new ActiveChatAdapter(activeChatList, new OnItemClickListener() {
            @Override
            public void onItemClick(ActiveChat activeChat) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("userId", activeChat.toString());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    //===========================Subscribe To Events Start=========================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewActiveChat(NewActiveChatEvent event) {
        ActiveChat activeChat = event.getActiveChat();
        if((index == 1 && activeChat.getIsFav() == 0)||(index == 2 && activeChat.getIsFav() == 1)){
            adapter.addNewActiveChat(activeChat);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAllActiveChatEvent(GetAllActiveChatEvent event) {
        if (index == 1) {
            initAdapter(event.getActiveChatList());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAllFavActiveChatEvent(GetAllFavActiveChat event) {
        if (index == 2) {
            initAdapter(event.getFavActiveChatList());
        }
    }

    //===========================Subscribe To Events End===========================

}