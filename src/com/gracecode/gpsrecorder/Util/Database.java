package com.gracecode.gpsrecorder.util;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private final String TAG = Database.class.getName();

    private final static int DATABASE_VERSION = 1;
    private Context context;


    private static final String DATABASE_CREATE = "create table location "
        + "(id integer primary key autoincrement, "
        + "latitude double not null, "
        + "longitude double not null,"
        + "speed double not null, "
        + "bearing float not null,"
        + "altitude double not null,"
        + "accuracy float not null,"
        + "time long not null,"
        + "del boolean default false"
        + ");";

    public Database(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
