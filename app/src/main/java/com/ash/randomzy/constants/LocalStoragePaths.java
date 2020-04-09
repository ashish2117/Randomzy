package com.ash.randomzy.constants;

import android.content.Context;

import com.ash.randomzy.service.AudioPlayerService;

import java.io.File;

public class LocalStoragePaths {
    private static final String RANDOMZY_APP_PATH = "/Randomzy";
    private static final String MEDIA_PATH = RANDOMZY_APP_PATH + "/media";
    private static final String IMAGES_PATH = MEDIA_PATH + "/images";
    private static final String AUDIO_PATH = MEDIA_PATH + "/audio";
    private static final String THUMBNAIL_PATH = IMAGES_PATH + "/thumbnails";
    private static final String VOICE_NOTES_PATH = AUDIO_PATH + "/voice";

    public static String getRandomzyAppPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + RANDOMZY_APP_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static String getMediaPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + MEDIA_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static String getImagesPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + IMAGES_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static String getAudioPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + AUDIO_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static String getThumbnailPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + THUMBNAIL_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static String getVoiceNotesPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath() + VOICE_NOTES_PATH;
        if (checkDirectory(path))
            return path;
        return null;
    }

    public static boolean checkDirectory(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists())
            return file.mkdirs();
        return true;
    }
}
