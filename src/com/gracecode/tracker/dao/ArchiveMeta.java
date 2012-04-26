package com.gracecode.tracker.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
    public static final String DISTANCE = "DISTANCE";
    public static final String TABLE_NAME = "meta";
    public static final double KM_PER_HOUR_CNT = 3.597;

    protected Archive archive;
    private SQLiteDatabase database;
    private static final int FUNC_AVG = 0x1;
    private static final int FUNC_MAX = 0x2;

    public ArchiveMeta(Archive archive) {
        this.archive = archive;
        this.database = archive.database;
    }

    protected boolean set(String name, String value) {
        ContentValues values = new ContentValues();
        values.put(Archive.DATABASE_COLUMN.META_NAME, name);
        values.put(Archive.DATABASE_COLUMN.META_VALUE, value);

        long result = 0;
        try {
            if (isExists(name)) {
                result = database.update(TABLE_NAME, values,
                    Archive.DATABASE_COLUMN.META_NAME + "='" + name + "'", null);
            } else {
                result = database.insert(TABLE_NAME, null, values);
            }
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        // 自动返回最后更新的数据更新时间
//        if (result > 0) {
//            File file = new File(archive.getName());
//            file.setLastModified(getEndTime().getTime());
//        }

        return result > 0 ? true : false;
    }

    protected String get(String name) {
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

    protected String get(String name, String defaultValue) {
        String value = get(name);
        if (value.equals("") && defaultValue.length() > 0) {
            return defaultValue;
        }

        return value;
    }

    protected boolean isExists(String name) {
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


    public Date getStartTime() {
        return new Date(Long.parseLong(get(START_TIME), 10));
    }

    public Date getEndTime() {
        try {
            long endTime = Long.parseLong(get(END_TIME), 10);
            return new Date(endTime);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean setStartTime(Date date) {
        long time = date.getTime();
        return set(START_TIME, String.valueOf(time));
    }

    public boolean setEndTime(Date date) {
        long time = date.getTime();
        return set(END_TIME, String.valueOf(time));
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }

    public boolean setDescription(String description) {
        boolean result = set(DESCRIPTION, description);
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


    /**
     * 获得当前已经记录的距离
     *
     * @return
     */
    public float getRawDistance() {
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

    public boolean setRawDistance() {
        float distance = getRawDistance();
        return set(DISTANCE, String.valueOf(distance));
    }

    public float getDistance() {
        return Float.parseFloat(get(DISTANCE, "0.0"));
    }

    private float getSpeed(int type) {
        String func;
        switch (type) {
            case FUNC_AVG:
                func = "avg";
                break;
            case FUNC_MAX:
            default:
                func = "max";
                break;
        }

        String sql = "SELECT " + func + "(" + Archive.DATABASE_COLUMN.SPEED
            + ") AS " + Archive.DATABASE_COLUMN.SPEED
            + " FROM " + Archive.TABLE_NAME + " LIMIT 1";

        Cursor cursor;
        float speed = 0;
        try {
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            speed = cursor.getFloat(cursor.getColumnIndex(Archive.DATABASE_COLUMN.SPEED));
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        return speed;
    }

    public float getAverageSpeed() {
        return getSpeed(FUNC_AVG);
    }

    public float getMaxSpeed() {
        return getSpeed(FUNC_MAX);
    }
}
