package com.gracecode.gpsrecorder.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.dao.LocationItem;

import java.text.DecimalFormat;


public class GPSWatcher implements LocationListener {


    public final static class FLAG {
        static final String STATUS = "status";
        static final String RECORDS = "records";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String SPEED = "speed";
        static final String BEARING = "bearing";
        static final String ALTITUDE = "altitude";
        static final String ACCURACY = "accuracy";
        static final String TIME = "time";
        static final String COUNT = "count";
        static final String DELETE = "del";
    }

    private final String TAG = GPSWatcher.class.getName();

    private static GPSDatabase gpsDatabase;
    private Context context;

    public GPSWatcher(Context context) {
        this.context = context;
        gpsDatabase = GPSDatabase.getInstance();
    }


    private String latitude, longitude;

    protected boolean isFlittedLocation(Location location) {
        DecimalFormat formatter = new DecimalFormat("####.####");
        String tmpLongitude = formatter.format(location.getLongitude());
        String tmpLatitude = formatter.format(location.getLatitude());

        if (tmpLatitude.equals(latitude) && tmpLongitude.equals(longitude)) {
            Log.v(TAG, String.format("The same latitude %f and longitude %f, ignore this.",
                location.getLatitude(), location.getLongitude()));
            return false;
        }
        latitude = tmpLatitude;
        longitude = tmpLongitude;


        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isFlittedLocation(location)) {
            return;
        }

        long result = gpsDatabase.insert(new LocationItem(location));
        if (result >= 1) {
            Log.v(TAG, "GPS Record has been saved into database.");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "GPS is enabled, reopen database.");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.w(TAG, "GPS is disabled");
        gpsDatabase.close();
    }
}

