package com.gracecode.tracker.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import com.gracecode.tracker.util.Logger;

import java.util.ArrayList;
import java.util.Date;

public class ArchiveMeta {
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String END_TIME = "END_TIME";
    public static final String START_TIME = "START_TIME";
    public static final String TABLE_NAME = "meta";

    protected Archive archive;
    private Archive.ArchiveDatabaseHelper databaseHelper;

    public ArchiveMeta(Archive archive) {
        this.archive = archive;
        this.databaseHelper = archive.databaseHelper;
    }

    public String getDescription() {
        return null;
    }

    public boolean setDescription(String description) {
        return false;
    }

    /**
     * 获得当前已经记录的距离
     *
     * @return
     */
    public float getDistance() {
        ArrayList<Location> locations = archive.fetchAll();
        Location lastComputedLocation = null;
        float distance = 0;
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (lastComputedLocation != null) {
                distance += lastComputedLocation.distanceTo(location);
            }

            lastComputedLocation = location;
        }

        return distance;
    }

    public Date getStartTime() {

        return null;
    }

    public Date getEndTime() {
        return null;

    }

    public boolean setStartTime(Date time) {

        return false;
    }

    public boolean setEndTime(Date time) {
        return false;
    }

    public long getCount() {
        Cursor cursor;
        long count = 0;
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        try {
            cursor = database.rawQuery("SELECT count(id) AS count FROM " + Archive.TABLE_NAME
                + " LIMIT 1", null);
            cursor.moveToFirst();

            count = cursor.getLong(cursor.getColumnIndex(Archive.DATABASE_COLUMN.COUNT));
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        return count;
    }
}
