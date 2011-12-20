package com.gracecode.gpsrecorder.util;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.gracecode.gpsrecorder.R;

public class Database {
    private final String TAG = Database.class.getName();

    public class OpenHelper extends SQLiteOpenHelper {
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
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    protected static OpenHelper helper = null;
    private Context context;

    public Database(Context context) {
        this.context = context;
        helper = new OpenHelper(this.context, context.getString(R.string.app_database_name));
    }

    public SQLiteDatabase getReadableDatabase() {
        return helper.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return helper.getWritableDatabase();
    }

    public long getValvedCount() {
        long count = 0;
        Cursor result = null;

        try {
            result = getReadableDatabase().rawQuery(
                "SELECT count(id) AS count FROM location WHERE del = 0 LIMIT 1", null);
            result.moveToFirst();

            count = result.getLong(result.getColumnIndex("count"));
        } catch (SQLiteException e) {

        }
        return count;
    }

    public void close() {
        helper.close();
    }
}
