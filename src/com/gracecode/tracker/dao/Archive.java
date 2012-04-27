package com.gracecode.tracker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import com.gracecode.tracker.util.Logger;

import java.io.File;
import java.util.ArrayList;

public class Archive {
    public static final int MODE_READ_ONLY = 0x001;
    public static final int MODE_READ_WRITE = 0x002;
    public static final String TABLE_NAME = "records";
    private static final String NEVER_USED_LOCATION_PROVIDER = "";


    public final static class DATABASE_COLUMN {
        static final String ID = "id";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String SPEED = "speed";
        static final String BEARING = "bearing";
        static final String ALTITUDE = "altitude";
        static final String ACCURACY = "accuracy";
        static final String TIME = "time";
        static final String COUNT = "count";
        static final String META_NAME = "meta";
        static final String META_VALUE = "value";
    }

    protected class ArchiveDatabaseHelper extends SQLiteOpenHelper {

        private static final String SQL_CREATE_LOCATION_TABLE =
            "create table " + TABLE_NAME + " ("
                + "id integer primary key autoincrement, "
                + "latitude double not null, "
                + "longitude double not null,"
                + "speed double not null, "
                + "bearing float not null,"
                + "altitude double not null,"
                + "accuracy float not null,"
                + "time long not null"
                + ");";

        private static final String SQL_CREATE_META_TABLE =
            "create table " + ArchiveMeta.TABLE_NAME + " ("
                + "id integer primary key autoincrement, "
                + "meta string not null unique,"
                + "value string default null"
                + ");";

        private static final int DB_VERSION = 1;

        public ArchiveDatabaseHelper(Context context, String name) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            try {
                database.execSQL(SQL_CREATE_META_TABLE);
                database.execSQL(SQL_CREATE_LOCATION_TABLE);
            } catch (SQLException e) {
                Logger.e(e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int i, int i1) {

        }
    }

    protected String name;
    private ArrayList<Location> locations;
    private ArchiveMeta meta;
    protected ArchiveDatabaseHelper databaseHelper = null;
    protected SQLiteDatabase database;
    protected Context context;
    protected int mode = MODE_READ_ONLY;

    public Archive(Context context) {
        this.context = context;
        this.locations = new ArrayList<Location>();
    }

    public Archive(Context context, String name) {
        this.context = context;
        this.locations = new ArrayList<Location>();
        this.open(name, MODE_READ_ONLY);
    }

    public Archive(Context context, String name, int mode) {
        this.context = context;
        this.locations = new ArrayList<Location>();
        this.mode = mode;
        this.open(name, this.mode);
    }

    public String getName() {
        return name;
    }

    public void open(String name, int mode) {
        // 防止重复打开数据库
        if (databaseHelper != null) {
            close();
        }
        this.name = name;
        this.mode = mode;
        this.databaseHelper = new ArchiveDatabaseHelper(context, name);

        this.reopen(this.mode);
        this.meta = new ArchiveMeta(this);
    }

    public void open(String name) {
        open(name, MODE_READ_ONLY);
    }

    public SQLiteDatabase reopen(int mode) {
        switch (mode) {
            case MODE_READ_ONLY:
                database = databaseHelper.getReadableDatabase();
                break;
            case MODE_READ_WRITE:
            default:
                database = databaseHelper.getWritableDatabase();
                break;
        }

        return database;
    }

    public boolean delete() {
        if (databaseHelper != null) {
            close();
        }
        return (new File(name)).delete();
    }


    public boolean exists() {
        return (new File(name)).exists();
    }

    public ArchiveMeta getMeta() {
        return meta;
    }

    public boolean add(Location point) {
        ContentValues values = new ContentValues();

        values.put(DATABASE_COLUMN.LATITUDE, point.getLatitude());
        values.put(DATABASE_COLUMN.LONGITUDE, point.getLongitude());
        values.put(DATABASE_COLUMN.SPEED, point.getSpeed());
        values.put(DATABASE_COLUMN.BEARING, point.getBearing());
        values.put(DATABASE_COLUMN.ALTITUDE, point.getAltitude());
        values.put(DATABASE_COLUMN.ACCURACY, point.getAccuracy());
        values.put(DATABASE_COLUMN.TIME, System.currentTimeMillis());

        try {
            return database.insert(TABLE_NAME, null, values) > 0 ? true : false;
        } catch (SQLException e) {
            Logger.e(e.getMessage());
        }

        return false;
    }

    /**
     * 获取最后个已记录的位置
     *
     * @return
     */
    public Location getLastRecord() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                + " ORDER BY time DESC LIMIT 1", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                return getLocationFromCursor(cursor);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        return null;
    }

    private Location getLocationFromCursor(Cursor cursor) {
        Location location = new Location(NEVER_USED_LOCATION_PROVIDER);

        location.setLatitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LONGITUDE)));
        location.setBearing(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.BEARING)));
        location.setAltitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.ALTITUDE)));
        location.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.ACCURACY)));
        location.setSpeed(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.SPEED)));
        location.setTime(cursor.getLong(cursor.getColumnIndex(DATABASE_COLUMN.TIME)));

        return location;
    }

    public ArrayList<Location> fetchAll() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY time ASC", null);

            locations.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                locations.add(getLocationFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Logger.e(e.getMessage());
        }

        return locations;
    }

    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }
}
