package com.ash.randomzy.activity;

import android.content.Intent;
import android.os.Bundle;

import com.ash.randomzy.R;
import com.ash.randomzy.constants.MessageStatus;
import com.ash.randomzy.entity.ActiveChat;
import com.ash.randomzy.model.User;
import com.ash.randomzy.service.MessageReceiverService;
import com.ash.randomzy.utility.ActivityLauncher;
import com.ash.randomzy.utility.UserUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import com.ash.randomzy.activity.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button mainMenu;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        fab = findViewById(R.id.fab);
        mainMenu = findViewById(R.id.mainMenu);

        mAuth = FirebaseAuth.getInstance();

        if (!UserUtil.userInitDone())
            UserUtil.initUserIds(this);

        addListeners();

    }

    private void addListeners() {
        fab.setOnClickListener((view) -> {
            searchRandom();
        });

        mainMenu.setOnClickListener((view -> {
            inflateMenu(view);
        }));
    }

    private void inflateMenu(View view) {
        final PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener((menuItem -> {
            if (menuItem.getItemId() == R.id.logout_menu) {
                mAuth.signOut();
                ActivityLauncher.startActivityClearCurrentTask(MainActivity.this, LoginActivity.class);
            }
            return true;
        }));
        popup.show();
    }

    private void searchRandom() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d: dataSnapshot.getChildren()){
                    if(d.getKey().equals("cb2F9iUd1faR8Hsqr79gx0pgcXk1")){
                        ActiveChat activeChat = new ActiveChat();
                        activeChat.setLastTextTime(0l);
                        activeChat.setLastText("");
                        activeChat.setId("QBtOqSDzANgbJaF45Fg1XsWfd5U2");
                        activeChat.setName("Ashish");
                        activeChat.setLastTextStatus(MessageStatus.SENDING);
                        activeChat.setSentBy(0);
                        activeChat.setIsFav(0);
                        activeChat.setProfilePicUrlServer("");
                        activeChat.setProfilePicUrlLocal("");
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("activeChat", activeChat.toString());
                        startActivity(intent);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_menu) {
            ActivityLauncher.startActivityClearCurrentTask(this, LoginActivity.class);
            mAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }
}