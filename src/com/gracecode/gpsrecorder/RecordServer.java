package com.gracecode.gpsrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import com.gracecode.gpsrecorder.util.Location;

public class RecordServer extends Service {

    private final String TAG = RecordServer.class.getName();

    private static final int LED_NOTIFICATION_ID = 1;
    protected static final int AIRPLANE_MODE_ON = 1;
    protected static final int AIRPLANE_MODE_OFF = 0;
    private NotificationManager notificationManager;
    private LocationManager locManager;
    private Location loc;
    private ContentResolver contentResolver;

    @Override
    public void onCreate() {
        super.onCreate();
        bindLocationListener();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    private void turnOnLED() {
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 1000;
        notif.ledOffMS = 1500;
        notificationManager.notify(LED_NOTIFICATION_ID, notif);
    }

    private void turnOffLED() {
        notificationManager.cancel(LED_NOTIFICATION_ID);
    }


    /**
     * 绑定 GPS，获得地理位置等信息
     */
    public void bindLocationListener() {
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        loc = new Location(this.getApplicationContext());

        Log.e(TAG, "Start location listener");
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, loc);
    }


    public void setAirPlaneMode(int mode) {
        contentResolver = getContentResolver();

        try {
            Log.e(TAG, "" + Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON));
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

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
        sendBroadcast(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        turnOnLED();
        setAirPlaneMode(AIRPLANE_MODE_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnOffLED();
        locManager.removeUpdates(loc);
        setAirPlaneMode(AIRPLANE_MODE_OFF);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
