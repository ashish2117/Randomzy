package com.ash.randomzy.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ImageUtil {

    public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source);
        } else {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        if (bitmap != null)
            bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        return bitmap;
    }

    public static void setBlurImageToImageView(ImageView imageView, Bitmap bitmap, Context context) {
        Glide.with(context)
                .load(bitmap)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 3)))
                .into(imageView);
    }

    public static void setBlurImageToImageView(ImageView imageView, Uri imageUri, Context context) {
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 3)))
                .into(imageView);
    }

    public static Uri writeImage(Bitmap bitmap, Context context, String pathInAppStorage, String fileName) {
        File mediaStorageDir = new File(pathInAppStorage);
        File mediaFile;
        String pathName = mediaStorageDir.getPath() + File.separator + fileName;
        mediaFile = new File(pathName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(mediaFile);
    }

}
