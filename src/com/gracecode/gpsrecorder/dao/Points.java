package com.gracecode.gpsrecorder.dao;

import android.location.Location;

public class Points extends Location {
    protected static final String PROVIDER = Points.class.getName();
    int count = 0;

    public Points() {
        super(PROVIDER);
    }

    public Points(Location location) {
        super(location);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
