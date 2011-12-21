package com.gracecode.gpsrecorder.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;

public class Location implements android.location.LocationListener {

    private final String TAG = Location.class.getName();

    private static android.location.Location lastLocationRecord;
    private static SQLiteDatabase db;
    private Context context;

    public Location(Context context) {
        this.context = context;
        db = new Database(context).getWritableDatabase();
    }

    public android.location.Location getLastLocationRecord() {
        return lastLocationRecord;
    }

    private String latitude, longitude;

    @Override
    public void onLocationChanged(android.location.Location loc) {
        ContentValues values = new ContentValues();

        DecimalFormat formatter = new DecimalFormat("####.##");
        String tmpLongitude = formatter.format(loc.getLongitude());
        String tmpLatitude = formatter.format(loc.getLatitude());

        if (tmpLatitude.equals(latitude) && tmpLongitude.equals(longitude)) {
            Log.v(TAG, String.format("The same latitude %f and longitude %f, ignore this.",
                loc.getLatitude(), loc.getLongitude()));
            return;
        }
        latitude = tmpLatitude;
        longitude = tmpLongitude;

        values.put("latitude", loc.getLatitude());
        values.put("longitude", loc.getLongitude());
        values.put("speed", loc.getSpeed());
        values.put("bearing", loc.getBearing());
        values.put("altitude", loc.getAltitude());
        values.put("accuracy", loc.getAccuracy());
        values.put("time", loc.getTime());

        try {
            db.insert("location", null, values);
            Log.v(TAG, String.format("Record latitude %s and longitude %s is saved.", latitude, longitude));
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        // Save the last location record
        lastLocationRecord = loc;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "GPS is enabled, reopen database.");
        db = new Database(context).getWritableDatabase();
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.w(TAG, "GPS is disabled");
        db.close();
    }
}

