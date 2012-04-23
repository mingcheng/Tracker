package com.gracecode.tracker.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import com.gracecode.tracker.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class ArchiveMeta {
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String END_TIME = "END_TIME";
    public static final String START_TIME = "START_TIME";
    public static final String TABLE_NAME = "meta";

    protected Archive archive;
    private Archive.ArchiveDatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public ArchiveMeta(Archive archive) {
        this.archive = archive;
        this.databaseHelper = archive.databaseHelper;
        this.database = databaseHelper.getWritableDatabase();
    }


    protected boolean setMeta(String name, String value) {
        ContentValues values = new ContentValues();
        values.put(Archive.DATABASE_COLUMN.META_NAME, name);
        values.put(Archive.DATABASE_COLUMN.META_VALUE, value);

        long result = 0;
        try {
            if (isMetaExists(name)) {
                result = database.update(TABLE_NAME, values,
                    Archive.DATABASE_COLUMN.META_NAME + "='" + name + "'", null);
            } else {
                result = database.insert(TABLE_NAME, null, values);
            }
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        return result > 0 ? true : false;
    }

    protected String getMeta(String name) {
        Cursor cursor;
        String result = "";
        try {
            String sql = "SELECT " + Archive.DATABASE_COLUMN.META_VALUE
                + " FROM " + TABLE_NAME
                + " WHERE " + Archive.DATABASE_COLUMN.META_NAME + "='" + name + "'"
                + " LIMIT 1";

            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            result = cursor.getString(cursor.getColumnIndex(Archive.DATABASE_COLUMN.META_VALUE));
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        } catch (CursorIndexOutOfBoundsException e) {
            Logger.e(e.getMessage());
        }

        return result;
    }

    protected boolean isMetaExists(String name) {
        Cursor cursor;
        int count = 0;
        try {
            cursor = database.rawQuery(
                "SELECT count(id) AS count"
                    + " FROM " + TABLE_NAME
                    + " WHERE " + Archive.DATABASE_COLUMN.META_NAME + "='" + name + "'", null);
            cursor.moveToFirst();

            count = cursor.getInt(cursor.getColumnIndex(Archive.DATABASE_COLUMN.COUNT));
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        } catch (CursorIndexOutOfBoundsException e) {
            Logger.e(e.getMessage());
        }

        return count > 0 ? true : false;
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
        return new Date(Long.parseLong(getMeta(START_TIME), 10));
    }

    public Date getEndTime() {
        try {
            long endTime = Long.parseLong(getMeta(END_TIME), 10);
            return new Date(endTime);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean setStartTime(Date date) {
        long time = date.getTime();
        return setMeta(START_TIME, String.valueOf(time));
    }

    public boolean setEndTime(Date date) {
        long time = date.getTime();
        return setMeta(END_TIME, String.valueOf(time));
    }

    public String getDescription() {
        return getMeta(DESCRIPTION);
    }

    public boolean setDescription(String description) {
        boolean result = setMeta(DESCRIPTION, description);
        if (result) {
            File file = new File(archive.getArchiveFileName());
            file.setLastModified(getEndTime().getTime());
        }
        return result;
    }

    public long getCount() {
        Cursor cursor;
        long count = 0;
        try {
            cursor = database.rawQuery(
                "SELECT count(id) AS count FROM "
                    + Archive.TABLE_NAME
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
