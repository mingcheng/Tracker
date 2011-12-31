package com.gracecode.gpsrecorder.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.gracecode.gpsrecorder.R;
import com.gracecode.gpsrecorder.util.Environment;

import java.io.File;
import java.util.Date;

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
        "create table location ("
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

    private Context context;
    private static SQLiteDatabase sqliteDatabase;

    private static GPSDatabase instance = null;

    public static GPSDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new GPSDatabase(context);
        }
        return instance;
    }

    public GPSDatabase(Context context) {
        this.context = context;
        openDatabase(getDatabaseFile());
    }

    public GPSDatabase(Context context, File file) throws SQLiteException {
        this.context = context;
        openDatabase(file);
    }

    public File getDatabaseFile() {
        return getDatabaseFile(new Date());
    }

    protected SQLiteDatabase getSqliteDatabase() {
        return sqliteDatabase;
    }


    /**
     * @param now
     * @return
     */
    public File getStorageDirectory(Date now) {
        String storageDirectory = Environment.getExternalStoragePath() + File.separator
            + getString(R.string.app_database_store_path);
        storageDirectory += File.separator + new java.text.SimpleDateFormat("yyyyMM").format(now);

        return new File(storageDirectory);
    }


    /**
     * @param now
     * @return
     */
    protected File getDatabaseFile(Date now) {

        String DatabaseFileName = new java.text.SimpleDateFormat("yyyyMMdd").format(now) + ".sqlite";

        File storageDirectory = getStorageDirectory(now);
        if (!storageDirectory.exists()) {
            Log.w(TAG, String.format("The database file: %s is not exists, build parent directory first.",
                storageDirectory.getAbsolutePath()));
            storageDirectory.mkdirs();
        }

        return new File(storageDirectory.getAbsolutePath() + File.separator + DatabaseFileName);
    }

    private String getString(int resId) {
        return context.getString(resId);
    }

    /**
     * @param databaseFile
     */
    protected void openDatabase(File databaseFile) {
        boolean isNeedCreateTable = false;
        if (!databaseFile.exists()) {
            isNeedCreateTable = true;
        }

        sqliteDatabase = sqliteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.CREATE_IF_NECESSARY);

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
        Cursor result = null;

        try {
            result = sqliteDatabase.rawQuery("SELECT count(id) AS count FROM location WHERE del = 0 LIMIT 1", null);
            result.moveToFirst();

            count = result.getLong(result.getColumnIndex(COLUMN.COUNT));
            result.close();
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


    public LocationItem getLastRecords() {
        Cursor result;
        LocationItem data = new LocationItem();

        data.setTime((new Date()).getTime());
        try {
            result = sqliteDatabase.rawQuery("SELECT *, count(*) as count " +
                "FROM location WHERE del = 0 ORDER BY time DESC LIMIT 1", null);
            result.moveToFirst();

            if (result != null && result.getLong(result.getColumnIndexOrThrow(COLUMN.COUNT)) > 0) {
                data.setLatitude(result.getDouble(result.getColumnIndex(COLUMN.LATITUDE)));
                data.setLongitude(result.getDouble(result.getColumnIndex(COLUMN.LONGITUDE)));
                data.setBearing(result.getFloat(result.getColumnIndex(COLUMN.BEARING)));
                data.setAltitude(result.getDouble(result.getColumnIndex(COLUMN.ALTITUDE)));
                data.setAccuracy(result.getFloat(result.getColumnIndex(COLUMN.ACCURACY)));
                data.setSpeed(result.getFloat(result.getColumnIndex(COLUMN.SPEED)));

                data.setTime(result.getLong(result.getColumnIndex(COLUMN.TIME)));
                data.setCount(result.getInt(result.getColumnIndex(COLUMN.COUNT)));
            }

            result.close();
        } catch (SQLiteException e) {

        }

        return data;
    }

    // @todo reformat this into ArrayList
    public Cursor getValvedData() {
        Cursor result = null;

        try {
            result = sqliteDatabase.rawQuery(
                "SELECT DISTINCT latitude, longitude, speed, bearing, altitude, accuracy, time " +
                    " from location WHERE del = 0 ORDER BY time DESC", null);

            result.moveToFirst();
        } catch (SQLiteException e) {

        }

        return result;
    }

    // @todo move into Locaiton
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

    public void reopen() {
        if (sqliteDatabase != null) {
            sqliteDatabase.close();
        }
        openDatabase(getDatabaseFile());
    }

    public void close() {
        sqliteDatabase.close();
    }
}
