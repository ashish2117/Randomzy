package com.ash.randomzy.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;

import com.ash.randomzy.constants.RealTimeDbNodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected static final String TAG = BaseActivity.class.getName();

    public static boolean isAppWentToBg = false;
    public static boolean isWindowFocused = false;
    public static boolean isBackPressed = false;
    public static boolean isScreenAwake = true;

    private static DatabaseReference onlineStatusRef;
    private FirebaseAuth mAuth;
    private PowerManager powerManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        onlineStatusRef = FirebaseDatabase.getInstance().getReference(RealTimeDbNodes.USERS_NODE)
                .child(mAuth.getCurrentUser().getUid()).child(RealTimeDbNodes.ONLINE_STATUS_NODE);
        powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart isAppWentToBg " + isAppWentToBg);
        applicationWillEnterForeground();
        super.onStart();
    }

    private void applicationWillEnterForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            setOnline();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop ");
        appInBackGround();
    }

    public void appInBackGround() {
        isScreenAwake = (Build.VERSION.SDK_INT < 20? powerManager.isScreenOn():powerManager.isInteractive());
        if(!isScreenAwake) {
            setOffline();
            isAppWentToBg = true;
            return;
        }
        if (!isWindowFocused) {
            isAppWentToBg = true;
            Log.d(TAG, "onStart isAppWentToBg " + isAppWentToBg);
            setOffline();
        }
    }

    @Override
    public void onBackPressed() {
        if (!(this instanceof MainActivity))
            isBackPressed = true;
        Log.d(TAG, "onBackPressed " + isBackPressed + this.getLocalClassName());
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "Focus " + hasFocus);
        if(hasFocus && !isScreenAwake) {
            setOnline();
            isAppWentToBg = false;
        }
        isWindowFocused = hasFocus;
        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    public static void setOnline(){
        onlineStatusRef.setValue("Online");
    }

    public static void setOffline(){
        onlineStatusRef.setValue((ServerValue.TIMESTAMP));
    }
}
