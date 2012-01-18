package com.gracecode.gpsrecorder.dao;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

interface Meta {
    public static final String TITLE = "TITLE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String STOP_TIME = "STOP_TIME";
    public static final String START_TIME = "START_TIME";
    public static final String RESUME_TIME = "RESUME_TIME";

    public Date getStartTime();

    public Date getStopTime();

    public Points getLastRecord();

    public String getTitle();

    public String getDescription();

    public boolean addOrUpdateDescription(String description);

    public boolean addOrUpdateTitle(String title);
}


public class GPSDatabase {
    private final String TAG = GPSDatabase.class.getName();

    private final int STATUS_DELETED = 0x1;
    private final int STATUS_NORMAL = 0x0;
    private GPSDatabase.Meta meta;
    private File databaseFile;


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
        static final String META_NAME = "name";
        static final String META_VALUE = "value";
    }

    private static final String TABLE_NAME = "location";
    private static final String META_TABLE_NAME = "meta";

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
        "create table " + META_TABLE_NAME + " ("
            + "id integer primary key autoincrement, "
            + "name string not null unique,"
            + "value string default null"
            + ");";

    private SQLiteDatabase sqliteDatabase;

    public class Meta implements com.gracecode.gpsrecorder.dao.Meta {
        public boolean isMetaExists(String tag) {
            int number = 0;
            try {
                String sql = "SELECT COUNT(" + COLUMN.META_NAME + ") as " + COLUMN.COUNT + " FROM "
                    + META_TABLE_NAME + " WHERE name = '" + tag + "'";

                Cursor cursor = sqliteDatabase.rawQuery(sql, null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    number = cursor.getInt(cursor.getColumnIndex(COLUMN.COUNT));
                }
                cursor.close();
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, "The name which marked as value: " + tag
                    + " is already exists, pls use updateMeta method for update this item.");
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }

            return number > 0 ? true : false;
        }

        public boolean updateMeta(String tag, String value) {
            int effectRows = 0;
            ContentValues values = new ContentValues();
            values.put(COLUMN.META_VALUE, value);

            try {
                effectRows = sqliteDatabase.update(META_TABLE_NAME, values, COLUMN.META_NAME + " = '" + tag + "'", null);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }
            return effectRows > 0 ? true : false;
        }

        public boolean addMeta(String tag, String value) {
            ContentValues values = new ContentValues();

            values.put(COLUMN.META_NAME, tag);
            values.put(COLUMN.META_VALUE, value);

            try {
                return sqliteDatabase.insert(META_TABLE_NAME, null, values) > 0 ? true : false;
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, "The name which marked as value: " + tag
                    + " is already exists, pls use updateMeta method for update.");
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }

            return false;
        }

        private String getMeta(String tag) {
            String result = "";
            try {
                String sql = "SELECT " + COLUMN.META_VALUE + " FROM " + META_TABLE_NAME
                    + " WHERE " + COLUMN.META_NAME + " = '" + tag + "' LIMIT 1";
                Cursor cursor = sqliteDatabase.rawQuery(sql, null);
                cursor.moveToFirst();

                if (cursor.getCount() > 0) {
                    result = cursor.getString(cursor.getColumnIndex(COLUMN.META_VALUE));
                }
                cursor.close();
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }

            return result.trim();
        }

        private Date getDateField(String tag) {
            String date = getMeta(tag);
            if (date.length() > 0) {
                try {
                    return (new Date(Long.parseLong(date)));
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            return null;
        }

        public boolean addOrUpdateMeta(String tag, String value) {
            if (isMetaExists(tag)) {
                return updateMeta(tag, value);
            } else {
                return addMeta(tag, value);
            }
        }

        @Override
        public Date getStartTime() {
            return getDateField(START_TIME);
        }

        @Override
        public Date getStopTime() {
            return getDateField(STOP_TIME);
        }

        @Override
        public Points getLastRecord() {
            return getLastRecord();
        }

        @Override
        public String getTitle() {
            return getMeta(TITLE);
        }

        @Override
        public String getDescription() {
            return getMeta(DESCRIPTION);
        }

        @Override
        public boolean addOrUpdateDescription(String description) {
            return addOrUpdateMeta(DESCRIPTION, description);
        }

        @Override
        public boolean addOrUpdateTitle(String title) {
            return addOrUpdateMeta(TITLE, title);
        }
    }

    public GPSDatabase(File file) throws SQLiteException {
        openDatabase(file);
        meta = new Meta();
    }

    /**
     * @param databaseFile
     */
    protected void openDatabase(File databaseFile) throws SQLException {
        boolean isNeedCreateTable = false;
        if (!databaseFile.exists()) {
            isNeedCreateTable = true;
        }

        sqliteDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        this.databaseFile = databaseFile;

        if (isNeedCreateTable) {
            sqliteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
            sqliteDatabase.execSQL(SQL_CREATE_META_TABLE);
        }

    }

    public File getFile() {
        return databaseFile;
    }


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
            Log.e(TAG, e.getMessage());
        }
        return count;
    }

    public long insert(Points location) {
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


    private Points getSingleLocationItemFromCursor(Cursor cursor) {
        Points points = new Points();

        points.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.LATITUDE)));
        points.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.LONGITUDE)));
        points.setBearing(cursor.getFloat(cursor.getColumnIndex(COLUMN.BEARING)));
        points.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN.ALTITUDE)));
        points.setAccuracy(cursor.getFloat(cursor.getColumnIndex(COLUMN.ACCURACY)));
        points.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN.SPEED)));
        points.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN.TIME)));
        points.setCount(cursor.getInt(cursor.getColumnIndex(COLUMN.COUNT)));

        return points;
    }

    public Points getLastRecord() {
        Cursor result;
        Points data = new Points();
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
            Log.e(TAG, "Read SQLite Database Error, Maybe The Database NOT Ready?");
        }

        return data;
    }

    // @todo reformat this into ArrayList
    public ArrayList<Points> getValvedData() {
        ArrayList<Points> pointsGroup = new ArrayList<Points>();
        Cursor cursor;

        try {
            cursor = sqliteDatabase.rawQuery(
                "SELECT DISTINCT latitude, longitude, speed, bearing, altitude, accuracy, time " +
                    " from location WHERE del = " + STATUS_NORMAL + " ORDER BY time DESC", null);

            for (cursor.moveToFirst(); cursor.moveToNext(); ) {
                pointsGroup.add(getSingleLocationItemFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {

        }

        return pointsGroup;
    }

    public Meta getMeta() {
        return meta;
    }

    public boolean addMeta(String tag, String value) {
        return meta.addMeta(tag, value);
    }

//    // @todo move into Location
//    public int markAllAsDelete() {
//        try {
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(COLUMN.DELETE, STATUS_DELETED);
//            return sqliteDatabase.update("location", contentValues, "", null);
//        } catch (SQLiteException e) {
//            Log.e(TAG, e.getMessage());
//        }
//        return 0;
//    }

    public void close() {
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
            sqliteDatabase = null;
        }
    }
}
