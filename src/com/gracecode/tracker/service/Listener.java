package com.gracecode.tracker.service;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.util.Helper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 绑定 LocationListener 回调并记录到数据库
 *
 * @author mingcheng<lucky@gracecode.com>
 */
public class Listener implements LocationListener {
    private final static int ACCURACY = 3;
    private final static int CACHE_SIZE = 5;

    private Archiver archiver;
    private ArchiveMeta meta = null;
    private BigDecimal lastLatitude;
    private BigDecimal lastLongitude;
    private HashMap<Long, Location> locationCache;
    private Recorder.ServiceBinder binder = null;

    public Listener(Archiver archiver) {
        this.archiver = archiver;
        this.locationCache = new HashMap<Long, Location>();
    }

    public Listener(Archiver archiver, Recorder.ServiceBinder binder) {
        this.archiver = archiver;
        this.locationCache = new HashMap<Long, Location>();
        this.binder = binder;
    }

    private boolean filter(Location location) {
        BigDecimal longitude = (new BigDecimal(location.getLongitude()))
            .setScale(ACCURACY, BigDecimal.ROUND_HALF_UP);

        BigDecimal latitude = (new BigDecimal(location.getLatitude()))
            .setScale(ACCURACY, BigDecimal.ROUND_HALF_UP);

        if (latitude.equals(lastLatitude) && longitude.equals(lastLongitude)) {
            return false;
        }

        lastLatitude = latitude;
        lastLongitude = longitude;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Save fitted location into database
        if (filter(location)) {
            locationCache.put(System.currentTimeMillis(), location);
            if (locationCache.size() > CACHE_SIZE) {
                flushCache();
            }

            // 计算动态路径
            this.meta = archiver.getMeta();
            if (meta != null) {
                meta.setRawDistance();
            }
        }
    }

    /**
     * flush cache
     */
    public void flushCache() {
        Iterator<Long> iterator = locationCache.keySet().iterator();
        while (iterator.hasNext()) {
            Long timeMillis = iterator.next();
            Location location = locationCache.get(timeMillis);
            if (archiver.add(location, timeMillis)) {
                Helper.Logger.i(String.format(
                    "Location(%f, %f) has been saved into database.", location.getLatitude(), location.getLongitude()));
            }
        }

        locationCache.clear();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
//                    binder.startRecord();
                Helper.Logger.i("Location provider is available.");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Helper.Logger.w("Location provider is out of service.");
                //binder.stopRecord();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Helper.Logger.w("Location provider is temporarily unavailable.");
                //binder.stopRecord();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Helper.Logger.i("Location provider is enabled.");
    }

    @Override
    public void onProviderDisabled(String s) {
        Helper.Logger.w("Location provider is disabled.");
    }
}
