package com.gracecode.tracker.service;

import android.location.GpsStatus;
import com.gracecode.tracker.util.Helper;

public class StatusListener implements GpsStatus.Listener {

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Helper.Logger.i("GPS event is started.");
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Helper.Logger.i("GPS event is first fixed.");
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Helper.Logger.i("GPS EVENT SATELLITE STATUS.");
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                Helper.Logger.i("GPS event is stopped.");
                break;
        }
    }
}
