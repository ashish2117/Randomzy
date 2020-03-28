package com.ash.randomzy.utility;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ImageUtil {

    public static Bitmap getThumbnail(Context context, Uri uri){
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(), ContentUris.parseId(uri),
                MediaStore.Images.Thumbnails.MICRO_KIND, null );
        return bitmap;
    }

    public static void setBlurImageToImageView(ImageView imageView, Bitmap bitmap, Context context){
        Glide.with(context)
                .load(bitmap)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 3)))
                .into(imageView);
    }

    public static void setBlurImageToImageView(ImageView imageView, Uri imageUri, Context context){
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 3)))
                .into(imageView);
    }

}
