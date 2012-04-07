package com.gracecode.gpsrecorder.recorder;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import com.gracecode.gpsrecorder.util.Logger;

import java.text.DecimalFormat;


public class Listener implements LocationListener {
    private Archive archive;

    public Listener(Archive archive) {
        this.archive = archive;
    }

    private static String lastLatitude;
    private static String lastLongitude;

    protected boolean isFlittedLocation(Location location) {
        final DecimalFormat formatter = new DecimalFormat("####.###");
        String tmpLongitude = formatter.format(location.getLongitude());
        String tmpLatitude = formatter.format(location.getLatitude());

        if (tmpLatitude.equals(lastLatitude) && tmpLongitude.equals(lastLongitude)) {
            Logger.e(String.format("The same latitude %s and longitude %s, ignore this.",
                tmpLatitude, tmpLongitude));
            return true;
        }
        lastLatitude = tmpLatitude;
        lastLongitude = tmpLongitude;

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isFlittedLocation(location)) {
            return;
        }
        if (archive.add(location)) {
            Logger.i(String.format(
                "Location(%f,%f) has been saved into database.", location.getLatitude(), location.getLongitude()
            ));
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

