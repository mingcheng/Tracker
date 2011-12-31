package com.gracecode.gpsrecorder.dao;

import android.location.Location;

public class LocationItem extends Location {
    protected static final String PROVIDER = LocationItem.class.getName();
    int count = 0;

    public LocationItem() {
        super(PROVIDER);
    }

    public LocationItem(Location location) {
        super(location);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
