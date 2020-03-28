package com.ash.randomzy.activity;

import android.os.Bundle;

import com.ash.randomzy.R;
import com.ash.randomzy.utility.ActivityLauncher;
import com.ash.randomzy.utility.UserUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import com.ash.randomzy.activity.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity {

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