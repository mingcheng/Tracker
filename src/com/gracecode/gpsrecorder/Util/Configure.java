package com.gracecode.gpsrecorder.util;

import android.content.Context;
import android.util.Log;
import com.gracecode.gpsrecorder.R;

import java.io.File;
import java.util.Date;

public class Configure {
    private static final String TAG = Configure.class.getName();

    protected static Context context;
    private static Configure instance = null;

    Configure(Context context) {
        this.context = context;
    }

    public static Configure getInstance(Context context) {
        if (instance == null) {
            instance = new Configure(context);
        }
        return instance;
    }

    public File getStorageDirectory() {
        return getStorageDirectory(new Date());
    }

    public File getStorageDirectory(Date now) {

        String storageDirectory = Environment.getExternalStoragePath() + File.separator
            + context.getString(R.string.app_database_store_path);
        storageDirectory += File.separator + new java.text.SimpleDateFormat("yyyyMM").format(now);

        return new File(storageDirectory);
    }

    public File getDatabaseFile() {
        return getDatabaseFile(new Date());
    }

    public File getDatabaseFile(Date date) {

        String DatabaseFileName = new java.text.SimpleDateFormat("yyyyMMdd").format(date) + ".sqlite";

        File storageDirectory = getStorageDirectory(date);
        if (!storageDirectory.exists()) {
            Log.w(TAG, String.format("The database file: %s is not exists, build parent directory first.",
                storageDirectory.getAbsolutePath()));
            storageDirectory.mkdirs();
        }

        return new File(storageDirectory.getAbsolutePath() + File.separator + DatabaseFileName);
    }
}
