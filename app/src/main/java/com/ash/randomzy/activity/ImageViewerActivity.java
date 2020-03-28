package com.ash.randomzy.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ash.randomzy.R;

import androidx.appcompat.app.ActionBar;

public class ImageViewerActivity extends BaseActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        Uri uri = Uri.parse(getIntent().getStringExtra("imageUri"));
        imageView = findViewById(R.id.image_message_image_view);
        imageView.setImageURI(uri);
    }
}
