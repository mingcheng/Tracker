package com.gracecode.gpsrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import com.gracecode.gpsrecorder.util.LocationListener;

public class RecordServer extends Service {

    private final String TAG = RecordServer.class.getName();

    LocationManager locManager;
    LocationListener locListener;

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
        locListener = new LocationListener(this.getApplicationContext());

        Log.e(TAG, "Start location listener");
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.e(TAG, "Start the server");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locManager.removeUpdates(locListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
