package com.gracecode.tracker.util;

import android.util.Log;

public class Logger {
    protected static final String TAG = "GPSRecorder";

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}
