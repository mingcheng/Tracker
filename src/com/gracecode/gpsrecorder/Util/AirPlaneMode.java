package com.gracecode.gpsrecorder.util;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class AirPlaneMode {
    public static final int AIRPLANE_MODE_ON = 0x010;
    public static final int AIRPLANE_MODE_OFF = 0x000;
    private ContentResolver contentResolver;
    protected Context context;

    AirPlaneMode(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }


    public void setAirPlaneMode(int mode) {
        switch (mode) {
            case AIRPLANE_MODE_OFF:
                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_OFF);
                break;

            case AIRPLANE_MODE_ON:
                Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, AIRPLANE_MODE_ON);
                break;

            default:
                return;
        }

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.sendBroadcast(intent);
    }

    public int getCurrentAirPlaneMode() {
        try {
            return Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e(e.getMessage());
        }
        return 0;
    }
}
