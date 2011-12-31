package com.gracecode.gpsrecorder.util;

public class Environment extends android.os.Environment {

    public static boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getExternalStoragePath() {
        if (isExternalStoragePresent()) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
}
