package com.gracecode.tracker.service;

import android.location.GpsStatus;

public class StatusListener implements GpsStatus.Listener {

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:

                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:

                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                break;

            case GpsStatus.GPS_EVENT_STOPPED:

                break;
        }
    }
}
