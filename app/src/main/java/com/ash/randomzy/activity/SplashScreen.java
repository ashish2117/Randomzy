package com.ash.randomzy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ash.randomzy.R;
import com.ash.randomzy.utility.ActivityLauncher;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        splashTimeout();
    }

    private void startApplication(){
        if(mAuth.getCurrentUser() == null)
            ActivityLauncher.startActivityClearCurrentTask(this, LoginActivity.class);
        else
            ActivityLauncher.startActivityClearCurrentTask(this, MainActivity.class);
    }

    private void splashTimeout() {
        Thread thread=new Thread(){
            @Override
            public void run()
            {
                try {
                    sleep(2000);
                    startApplication();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

}
