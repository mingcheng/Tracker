package com.gracecode.gpsrecorder.util;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Environment extends android.os.Environment {

    public static final String TAG = Environment.class.getName();

    public static final String PREF_AUTO_START = "autoStart";
    public static final String PREF_RECORD_BY = "recordBy";
    public static final String PREF_GPS_MINTIME = "gpsMinTime";
    public static final String PREF_GPS_MINDISTANCE = "gpsMinDistance";

    public static final String RECORD_BY_DAY = "RECORD_BY_DAY";
    public static final String RECORD_BY_TIMES = "RECORD_BY_TIMES";

    public static final String DEFAULT_GPS_MINTIME = "2000";
    public static final String DEFAULT_GPS_MINDISTANCE = "10";


    public static boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getExternalStoragePath() {
        if (isExternalStoragePresent()) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static File getStorageDirectory() {
        String storageDirectory = Environment.getExternalStoragePath()
            + File.separator + "gpsrecorder" + File.separator;

        storageDirectory += File.separator
            + new SimpleDateFormat("yyyyMM").format(new Date());

        return new File(storageDirectory);
    }


    public static File getDatabaseFile(String recordBy) {
        File storageDirectory = getStorageDirectory();
        String databaseFileName = System.currentTimeMillis() + ".sqlite";

        // If record by day, output the database filename like "20111203.sqlite" etc.
        if (recordBy.equals(RECORD_BY_DAY)) {
            databaseFileName = (new SimpleDateFormat("yyyyMMdd").format(new Date())) + ".sqlite";
        }

        File databaseFile = new File(storageDirectory.getAbsoluteFile() + File.separator + databaseFileName);
        Log.e("", databaseFile.getAbsolutePath());

        return databaseFile;
    }
}
