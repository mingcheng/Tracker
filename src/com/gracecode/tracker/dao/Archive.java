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

import java.util.ArrayList;

public class Archive {

    private static final String NEVER_USED_LOCATION_PROVIDER = "";
    protected String archiveName;

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

    public static final String TABLE_NAME = "records";

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

    private ArrayList<Location> geoPoints;
    private ArchiveMeta archiveMeta = null;
    protected ArchiveDatabaseHelper databaseHelper = null;
    protected SQLiteDatabase database = null;
    protected Context context;

    public Archive(Context context) {
        this.context = context;
        geoPoints = new ArrayList<Location>();
    }

    public Archive(Context context, String name) {
        this.context = context;
        geoPoints = new ArrayList<Location>();

        this.open(name);
    }

    public String getArchiveFileName() {
        return archiveName;
    }

    public void open(String name) {
        this.archiveName = name;
        databaseHelper = new ArchiveDatabaseHelper(context, name);
        database = databaseHelper.getWritableDatabase();
        archiveMeta = new ArchiveMeta(this);
    }

    public ArchiveMeta getArchiveMeta() {
        return archiveMeta;
    }

    public boolean add(Location point) {
        ContentValues values = new ContentValues();

        values.put(DATABASE_COLUMN.LATITUDE, point.getLatitude());
        values.put(DATABASE_COLUMN.LONGITUDE, point.getLongitude());
        values.put(DATABASE_COLUMN.SPEED, point.getSpeed());
        values.put(DATABASE_COLUMN.BEARING, point.getBearing());
        values.put(DATABASE_COLUMN.ALTITUDE, point.getAltitude());
        values.put(DATABASE_COLUMN.ACCURACY, point.getAccuracy());
        values.put(DATABASE_COLUMN.TIME, point.getTime());

        try {
            return database.insert(TABLE_NAME, null, values) > 0 ? true : false;
        } catch (SQLException e) {
            Logger.e(e.getMessage());
        }

        return false;
    }

    public Location getLastRecord() {
        Cursor cursor = null;
        Location point = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                + " ORDER BY time DESC LIMIT 1", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                point = getLocationFromCursor(cursor);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        }

        return point;
    }

    private Location getLocationFromCursor(Cursor cursor) {
        Location points = new Location(NEVER_USED_LOCATION_PROVIDER);

        points.setLatitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LATITUDE)));
        points.setLongitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LONGITUDE)));
        points.setBearing(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.BEARING)));
        points.setAltitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.ALTITUDE)));
        points.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.ACCURACY)));
        points.setSpeed(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.SPEED)));
        points.setTime(cursor.getLong(cursor.getColumnIndex(DATABASE_COLUMN.TIME)));

        return points;
    }

    public ArrayList<Location> fetchAll() {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY time DESC", null);

            geoPoints.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                geoPoints.add(getLocationFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {
            Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Logger.e(e.getMessage());
        }

        return geoPoints;
    }

    public void close() {
        databaseHelper.close();
    }
}
