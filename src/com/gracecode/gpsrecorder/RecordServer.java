package com.gracecode.gpsrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import com.gracecode.gpsrecorder.util.Location;

public class RecordServer extends Service {

    private final String TAG = RecordServer.class.getName();

    LocationManager locManager;
    Location loc;

    @Override
    public void onCreate() {
        super.onCreate();
        bindLocationListener();
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


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "Start the server");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locManager.removeUpdates(loc);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
