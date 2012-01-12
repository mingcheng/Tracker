package com.gracecode.gpsrecorder.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import com.gracecode.gpsrecorder.dao.GPSDatabase;
import com.gracecode.gpsrecorder.dao.LocationItem;


public class GPSWatcher implements LocationListener {


    public final static class FLAG {
        static final String STATUS = "status";
        static final String RECORDS = "records";
        static final String LATITUDE = "lastLatitude";
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

    protected GPSDatabase gpsDatabase;
    private Context context;

    public GPSWatcher(Context context, GPSDatabase database) {
        this.context = context;
        gpsDatabase = database;
    }


//    private static String lastLatitude;
//    private static String lastLongitude;
//
//    protected boolean isFlittedLocation(Location location) {
//        final DecimalFormat formatter = new DecimalFormat("####.###");
//        String tmpLongitude = formatter.format(location.getLongitude());
//        String tmpLatitude = formatter.format(location.getLatitude());
//
//        if (tmpLatitude.equals(lastLatitude) && tmpLongitude.equals(lastLongitude)) {
//            Log.v(TAG, String.format("The same latitude %s and longitude %s, ignore this.",
//                tmpLatitude, tmpLongitude));
//            return true;
//        }
//        lastLatitude = tmpLatitude;
//        lastLongitude = tmpLongitude;
//
//        return false;
//    }

    @Override
    public void onLocationChanged(Location location) {
//        if (isFlittedLocation(location)) {
//            return;
//        }

        long result = gpsDatabase.insert(new LocationItem(location));
        if (result >= 1) {
            Log.v(TAG, "GPS Record has been saved to database.");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:

                break;
            case LocationProvider.OUT_OF_SERVICE:

                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:

                break;
        }
    }


    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

