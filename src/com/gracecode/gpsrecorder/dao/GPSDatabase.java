package com.gracecode.gpsrecorder.dao;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.util.Date;

import static android.database.sqlite.SQLiteDatabase.CREATE_IF_NECESSARY;

public class GPSDatabase {
    private final String TAG = GPSDatabase.class.getName();

    private final int STATUS_DELETED = 0x1;
    private final int STATUS_NORMAL = 0x0;


    public final static class COLUMN {
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String SPEED = "speed";
        static final String BEARING = "bearing";
        static final String ALTITUDE = "altitude";
        static final String ACCURACY = "accuracy";
        static final String TIME = "time";
        static final String COUNT = "count";
        static final String DELETE = "del";
    }

    protected static final String TABLE_NAME = "location";

    private static final String SQL_CREATE_LOCATION_TABLE =
        "create table " + TABLE_NAME + " ("
            + "id integer primary key autoincrement, "
            + "latitude double not null, "
            + "longitude double not null,"
            + "speed double not null, "
            + "bearing float not null,"
            + "altitude double not null,"
            + "accuracy float not null,"
            + "time long not null,"
            + "del int default 0"
            + ");";

    private static final String SQL_CREATE_META_TABLE =
        "create table meta("
            + "id integer primary key autoincrement, "
            + "name string default null,"
            + "value string default null"
            + ");";

    private SQLiteDatabase sqliteDatabase;
    private static GPSDatabase instance = null;

    public static GPSDatabase getInstance() {
        return instance;
    }

    public static GPSDatabase getInstance(File file) {
        if (instance == null) {
            instance = new GPSDatabase(file);
        }

        return instance;
    }

    public GPSDatabase(File file) throws SQLiteException {
        openDatabase(file);
    }

    /**
     * @param databaseFile
     */
    protected void openDatabase(File databaseFile) {
        boolean isNeedCreateTable = false;
        if (!databaseFile.exists()) {
            isNeedCreateTable = true;
        }

        sqliteDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, CREATE_IF_NECESSARY);

        if (isNeedCreateTable) {
            sqliteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
            sqliteDatabase.execSQL(SQL_CREATE_META_TABLE);
        }

    }

    /**
     * @return
     */
    public long getValvedCount() {
        long count = 0;
        Cursor cursor;

        try {
            cursor = sqliteDatabase.rawQuery("SELECT count(id) AS count FROM " + TABLE_NAME + " WHERE del = " + STATUS_NORMAL
                + " LIMIT 1", null);
            cursor.moveToFirst();

            count = cursor.getLong(cursor.getColumnIndex(COLUMN.COUNT));
            cursor.close();
        } catch (SQLiteException e) {

        }
        return count;
    }

    public long insert(LocationItem location) {
        ContentValues values = new ContentValues();

        values.put(COLUMN.LATITUDE, location.getLatitude());
        values.put(COLUMN.LONGITUDE, location.getLongitude());
        values.put(COLUMN.SPEED, location.getSpeed());
        values.put(COLUMN.BEARING, location.getBearing());
        values.put(COLUMN.ALTITUDE, location.getAltitude());
        values.put(COLUMN.ACCURACY, location.getAccuracy());
        values.put(COLUMN.TIME, location.getTime());
        values.put(COLUMN.DELETE, STATUS_NORMAL);

        try {
            return sqliteDatabase.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        return 0;
    }

    private LocationItem getSingleLocationItemFromCursor(Cursor cursor) {
        LocationItem locationItem = new LocationItem();

        locationItem.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.LATITUDE)));
        locationItem.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.LONGITUDE)));
        locationItem.setBearing(cursor.getFloat(cursor.getColumnIndex(COLUMN.BEARING)));
        locationItem.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.ALTITUDE)));
        locationItem.setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN.ACCURACY)));
        locationItem.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN.SPEED)));
        locationItem.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN.TIME)));
        locationItem.setCount(cursor.getInt(cursor.getColumnIndex(COLUMN.COUNT)));

        return locationItem;
    }

    public LocationItem getLastRecords() {
        Cursor result;
        LocationItem data = new LocationItem();
        data.setTime((new Date()).getTime());

        try {
            result = sqliteDatabase.rawQuery("SELECT *, count(*) as count " +
                "FROM location WHERE del = 0 ORDER BY time DESC LIMIT 1", null);
            result.moveToFirst();

            if (result != null && result.getLong(result.getColumnIndexOrThrow(COLUMN.COUNT)) > 0) {
                data = getSingleLocationItemFromCursor(result);
            }

            result.close();
        } catch (SQLiteException e) {

        }

        return data;
    }

    // @todo reformat this into ArrayList
    public LocationGroup getValvedData() {
        LocationGroup locationGroup = new LocationGroup();
        Cursor cursor;

        try {
            cursor = sqliteDatabase.rawQuery(
                "SELECT DISTINCT latitude, longitude, speed, bearing, altitude, accuracy, time " +
                    " from location WHERE del = " + STATUS_NORMAL + " ORDER BY time DESC", null);

            for (cursor.moveToFirst(); cursor.moveToNext(); ) {
                locationGroup.add(getSingleLocationItemFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {

        }

        return locationGroup;
    }

    // @todo move into Location
    public int markAllAsDelete() {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN.DELETE, STATUS_DELETED);
            return sqliteDatabase.update("location", contentValues, "", null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }

    public void close() {
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
        }
    }
}
