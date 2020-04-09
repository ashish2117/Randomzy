package com.ash.randomzy.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;

public class AudioPlayerService extends LifecycleService {

    private SimpleExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        this.player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                Util.getUserAgent(getApplicationContext(), "Randomzy"));
        Uri audioUri = Uri.parse("https://pwdown.com/113462/variation/190K/Genda%20Phool%20-%20Badshah.mp3");
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(audioUri);
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
