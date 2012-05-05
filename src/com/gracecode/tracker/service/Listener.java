package com.gracecode.tracker.service;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import com.gracecode.tracker.dao.Archive;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.util.Helper;

import java.math.BigDecimal;

/**
 * 绑定 LocationListener 回调并记录到数据库
 *
 * @author mingcheng<lucky@gracecode.com>
 */
public class Listener implements LocationListener {
    private final static int ACCURACY = 3;

    private Archive archive;
    private ArchiveMeta meta = null;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;

    public Listener(Archive archive) {
        this.archive = archive;
    }

    private boolean filter(Location location) {
        BigDecimal longitude = (new BigDecimal(location.getLongitude())).setScale(
            ACCURACY, BigDecimal.ROUND_HALF_UP);

        BigDecimal latitude = (new BigDecimal(location.getLatitude())).setScale(
            ACCURACY, BigDecimal.ROUND_HALF_UP);

        if (latitude.equals(lastLatitude) && longitude.equals(lastLongitude)) {
            return false;
        }

        lastLatitude = latitude;
        lastLongitude = longitude;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (filter(location) && archive.add(location)) {
            this.meta = archive.getMeta();
            Helper.Logger.i(String.format(
                "Location(%f, %f) has been saved into database.", lastLatitude, lastLongitude
            ));

            // 另外开个线程处理，避免线程锁住
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (meta != null) {
                        meta.setRawDistance();
                    }
                }
            }).start();
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
