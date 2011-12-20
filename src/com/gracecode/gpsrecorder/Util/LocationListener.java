package com.gracecode.gpsrecorder.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import com.gracecode.gpsrecorder.R;

public class LocationListener implements android.location.LocationListener {

    private final String TAG = LocationListener.class.getName();

    private static Location lastLocationRecord;
    private static Database db;
    private Context context;

    public LocationListener(Context context) {
        this.context = context;
        db = new Database(context, context.getString(R.string.app_database_name));
    }

    public Location getLastLocationRecord() {
        return lastLocationRecord;
    }

    @Override
    public void onLocationChanged(Location loc) {
        ContentValues values = new ContentValues();
        values.put("latitude", loc.getLatitude());
        values.put("longitude", loc.getLongitude());
        values.put("speed", loc.getSpeed());
        values.put("bearing", loc.getBearing());
        values.put("altitude", loc.getAltitude());
        values.put("accuracy", loc.getAccuracy());
        values.put("time", loc.getTime());
        Log.v(TAG, values.toString());

        // Save to database
        try {
            SQLiteDatabase sql = db.getWritableDatabase();
            sql.insert("location", null, values);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        // Save the last location record
        lastLocationRecord = loc;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG, "GPS is enabled, reopen database.");
        db = new Database(context, context.getString(R.string.app_database_name));
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.w(TAG, "GPS is disabled");
        db.close();
    }
}

