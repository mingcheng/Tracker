package com.gracecode.gpsrecorder.util;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.gracecode.gpsrecorder.R;

import java.io.File;
import java.util.Date;

public class Database {
    private final String TAG = Database.class.getName();

    protected static class OpenHelper extends SQLiteOpenHelper {
        private final static int DATABASE_VERSION = 1;
        private Context context;

        private static final String DATABASE_CREATE =
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

        public OpenHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
            this.context = context.getApplicationContext();
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        }
    }

    protected static OpenHelper helper = null;
    private Context context;


    public String getDatabasePath() {
        return getDatabasePath(new Date());
    }


    public String getDatabasePath(Date now) {
        String path = String.format("%s%s.sqlite", context.getString(R.string.app_database_store_path),
            new java.text.SimpleDateFormat("yyyyMM/yyyyMMdd").format(now));

        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            Log.w(TAG, String.format("The database file: %s is not exists, build parent directory first.",
                file.getAbsolutePath()));
            File parent = new File(file.getParent());
            parent.mkdirs();
        }

        return file.getAbsolutePath();
    }


    public Database(Context context) {
        this.context = context;
        this.helper = new OpenHelper(context, getDatabasePath());
    }


    public SQLiteDatabase getReadableDatabase() {
        return helper.getReadableDatabase();
    }


    public SQLiteDatabase getWritableDatabase() {
        return helper.getWritableDatabase();
    }


    public long getValvedCount(SQLiteDatabase db) {
        long count = 0;
        Cursor result = null;

        try {
            result = db.rawQuery(
                "SELECT count(id) AS count FROM location WHERE del = 0 LIMIT 1", null);
            result.moveToFirst();

            count = result.getLong(result.getColumnIndex("count"));
        } catch (SQLiteException e) {

        }
        return count;
    }


    public long getValvedCount() {
        return getValvedCount(getReadableDatabase());
    }


    public Cursor getValvedData(SQLiteDatabase db) {
        Cursor result = null;

        try {
            result = db.rawQuery(
                "SELECT DISTINCT latitude, longitude, speed, bearing, altitude, accuracy, time " +
                    " from location WHERE del = 0", null);

            result.moveToFirst();
        } catch (SQLiteException e) {

        }

        return result;
    }


    public Cursor getValvedData() {
        return getValvedData(getReadableDatabase());
    }


    public void close() {
        if (helper != null) {
            helper.close();
        }
    }
}
