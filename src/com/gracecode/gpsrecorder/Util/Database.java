package com.gracecode.gpsrecorder.Util;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "/mnt/sdcard/location.db";
    private static final String TAG = Database.class.getName();
    private static final int DATABASE_VERSION = 1;

    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String DATABASE_CREATE = "create table location "
        + "(id integer primary key autoincrement, "
        + "latitude double not null, "
        + "longitude double not null,"
        + "speed double not null, "
        + "bearing float not null,"
        + "altitude double not null,"
        + "accuracy float not null,"
        + "time long not null"
        + ");";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
